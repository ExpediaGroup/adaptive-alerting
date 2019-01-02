/*
 * Copyright 2018 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.dataservice.athena;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.AmazonAthenaClientBuilder;
import com.amazonaws.services.athena.model.GetQueryExecutionRequest;
import com.amazonaws.services.athena.model.GetQueryExecutionResult;
import com.amazonaws.services.athena.model.GetQueryResultsRequest;
import com.amazonaws.services.athena.model.GetQueryResultsResult;
import com.amazonaws.services.athena.model.QueryExecutionContext;
import com.amazonaws.services.athena.model.QueryExecutionState;
import com.amazonaws.services.athena.model.QueryExecutionStatus;
import com.amazonaws.services.athena.model.ResultConfiguration;
import com.amazonaws.services.athena.model.Row;
import com.amazonaws.services.athena.model.StartQueryExecutionRequest;
import com.amazonaws.services.athena.model.StartQueryExecutionResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.util.ThreadUtil;
import com.expedia.adaptivealerting.dataservice.DataService;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.typesafe.config.Config;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * AWS Athena-based {@link DataService} implementation. This implementation queries an Athena database and then writes
 * the query results into a configurable S3 bucket.
 *
 * @author Willie Wheeler
 */
@Slf4j
public class AthenaDataService implements DataService {
    private static final String CONFIG_KEY_REGION = "region";
    private static final String CONFIG_KEY_DATABASE = "database";
    private static final String CONFIG_KEY_OUTPUT_BUCKET = "outputBucket";
    private static final String CONFIG_KEY_CLIENT_EXECUTION_TIMEOUT = "clientExecutionTimeout";
    
    // FIXME Temporarily using a hardcoded query.
    private static final String POS_QUERY_TEMPLATE =
            "SELECT timestamp, value" +
            " FROM aa_datasets.aa_metrics" +
            " WHERE \"$path\" LIKE '%%%s%%'" +
            " AND timestamp BETWEEN %d AND %d" +
            " ORDER BY timestamp";
    
    private AmazonAthena athena;
    private AmazonS3 s3;
    
    private String region;
    private String database;
    private String outputBucket;
    private int clientExecutionTimeout = 0;
    private MetricTankIdFactory idFactory = new MetricTankIdFactory();
    
    /**
     * Initializes the data service. Required configuration properties:
     *
     * <ul>
     *     <li>region: AWS region</li>
     *     <li>database: Athena database name</li>
     *     <li>outputBucket: S3 bucket into which to write the query results</li>
     * </ul>
     *
     * Optional configuration properties:
     *
     * <ul>
     *     <li>clientExecutionTimeout: Total number of milliseconds for a request/response including the time to execute
     *     the request handlers, the round-trip to AWS, and the time to execute the response handlers. Captured on a per
     *     request-type level. (Default 0)</li>
     * </ul>
     *
     * @param config Data service configuration.
     */
    @Override
    public void init(Config config) {
        
        // These generate exceptions for missing properties.
        this.region = config.getString(CONFIG_KEY_REGION);
        this.database = config.getString(CONFIG_KEY_DATABASE);
        this.outputBucket = config.getString(CONFIG_KEY_OUTPUT_BUCKET);
        
        if (config.hasPath(CONFIG_KEY_CLIENT_EXECUTION_TIMEOUT)) {
            this.clientExecutionTimeout = config.getInt(CONFIG_KEY_CLIENT_EXECUTION_TIMEOUT);
        }
        
        log.info("Initializing AthenaDataService: region={}, database={}, outputBucket={}, clientExecutionTimeout={}",
                region, database, outputBucket, clientExecutionTimeout);
        
        final ClientConfiguration clientConfig =
                new ClientConfiguration().withClientExecutionTimeout(clientExecutionTimeout);
    
        this.athena = AmazonAthenaClientBuilder.standard()
                .withRegion(region)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withClientConfiguration(clientConfig)
                .build();
        
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withClientConfiguration(clientConfig)
                .build();
        
        log.info("Successfully initialized AthenaDataService");
    }
    
    public AmazonAthena getAthena() {
        return athena;
    }
    
    @Override
    public MetricFrame getMetricFrame(MetricDefinition metricDefinition, Instant startDate, Instant endDate) {
        notNull(metricDefinition, "metricDefinition can't be null");
        notNull(startDate, "startDate can't be null");
        notNull(endDate, "endDate can't be null");
        isTrue(!startDate.isAfter(endDate), "startDate cannot be after endDate");

        final String query = buildAthenaQuery(metricDefinition, startDate.getEpochSecond(), endDate.getEpochSecond());
        final String queryExecutionId = submitAthenaQuery(query);

        waitForQueryToComplete(queryExecutionId);
        return fetchResults(queryExecutionId, metricDefinition);
    }
    
    private String buildAthenaQuery(MetricDefinition metricDefinition, long startSecond, long endSecond) {
        return String.format(POS_QUERY_TEMPLATE, idFactory.getId(metricDefinition), startSecond, endSecond);
    }
    
    private String submitAthenaQuery(String query) {
        final QueryExecutionContext context = new QueryExecutionContext().withDatabase(database);
        final ResultConfiguration conf = new ResultConfiguration()
//                .withEncryptionConfiguration(encryptionConfiguration)
                .withOutputLocation("s3://" + outputBucket);
        final StartQueryExecutionRequest request = new StartQueryExecutionRequest()
                .withQueryString(query)
                .withQueryExecutionContext(context)
                .withResultConfiguration(conf);
        final StartQueryExecutionResult result = athena.startQueryExecution(request);
        log.trace("Executing Athena query {}: {}", result.getQueryExecutionId(), query);
        return result.getQueryExecutionId();
    }
    
    @SneakyThrows
    private void waitForQueryToComplete(String queryExecutionId) {
        final GetQueryExecutionRequest request = new GetQueryExecutionRequest().withQueryExecutionId(queryExecutionId);
        
        GetQueryExecutionResult result = null;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            result = athena.getQueryExecution(request);
            final QueryExecutionStatus status = result.getQueryExecution().getStatus();
            final String state = status.getState();
            if (state.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("Query failed: " + status.getStateChangeReason());
            } else if (state.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("Query cancelled.");
            } else if (state.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            } else {
                ThreadUtil.sleep(1000L);
            }
            log.trace("Current query state: {}", state);
        }
        log.trace("Athena query complete: {}", queryExecutionId);
    }

    private MetricFrame fetchResults(String queryExecutionId, MetricDefinition metricDefinition) {
        GetQueryResultsRequest getQueryResultsRequest = new GetQueryResultsRequest()
                .withQueryExecutionId(queryExecutionId);

        GetQueryResultsResult getQueryResultsResult = athena.getQueryResults(getQueryResultsRequest);

        boolean first = true;
        List<MetricData> data = new ArrayList<>();
        while (true) {
            for (Row row : getQueryResultsResult.getResultSet().getRows()) {

                // The first row of the first page holds the column names.
                if (first) {
                    first = false;
                } else {
                    data.add(extractData(row, metricDefinition));
                }
            }

            // If nextToken is null, there are no more pages to read. Break out of the loop.
            if (getQueryResultsResult.getNextToken() == null) {
                break;
            }
            getQueryResultsResult = athena.getQueryResults(
                    getQueryResultsRequest.withNextToken(getQueryResultsResult.getNextToken()));
        }
        return new MetricFrame(data.toArray(new MetricData[0]));
    }

    private MetricData extractData(Row row, MetricDefinition metricDefinition) {
        long epochSeconds = Long.parseLong(row.getData().get(0).getVarCharValue());
        double value = Double.parseDouble(row.getData().get(1).getVarCharValue());
        return new MetricData(metricDefinition, value, epochSeconds);
    }
}
