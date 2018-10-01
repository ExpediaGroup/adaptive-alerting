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
import com.amazonaws.services.athena.model.QueryExecutionContext;
import com.amazonaws.services.athena.model.QueryExecutionState;
import com.amazonaws.services.athena.model.QueryExecutionStatus;
import com.amazonaws.services.athena.model.ResultConfiguration;
import com.amazonaws.services.athena.model.StartQueryExecutionRequest;
import com.amazonaws.services.athena.model.StartQueryExecutionResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.io.MetricFrameLoader;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.adaptivealerting.core.util.ThreadUtil;
import com.expedia.adaptivealerting.dataservice.DataService;
import com.expedia.metrics.MetricDefinition;
import com.typesafe.config.Config;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
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
    
    private static final int ATHENA_MAX_RESULTS = 1000;
    
    // FIXME Temporarily using a hardcoded query.
    private static final String POS_QUERY_TEMPLATE =
            "SELECT timestamp, value" +
            " FROM bookings_lob_pos" +
            " WHERE lob='%s'" +
            " AND pos='%s'" +
            " AND timestamp BETWEEN %d AND %d" +
            " ORDER BY timestamp";
    
    private AmazonAthena athena;
    private AmazonS3 s3;
    
    private String region;
    private String database;
    private String outputBucket;
    private int clientExecutionTimeout = 0;
    
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
        
        // TODO Athena returns a maximum of 1,000 query results. So in general we need to make multiple queries.
        // The metrics are currently stored every minute. At some point this will likely change (it will be different
        // for different metrics), but until then we can assume it's one per minute. [WLW]
        
        long endEpochSecond = endDate.getEpochSecond();
        long currEpochSecond = startDate.getEpochSecond();
        
        // Each result represents 1 minute, so we need to move 1 minute (60 seconds) forward for each one.
        int incrSecond = 60 * ATHENA_MAX_RESULTS;
        
        final List<MetricFrame> frames = new ArrayList<>();
        while (currEpochSecond < endEpochSecond) {
            long nextEpochSecond = currEpochSecond + incrSecond;
            
            // TODO Run these in parallel
            final String query = buildAthenaQuery(metricDefinition, currEpochSecond, nextEpochSecond);
            final String queryExecutionId = submitAthenaQuery(query);
            waitForQueryToComplete(queryExecutionId);
            
            // Need to capture the results as part of the loop too.
            try (final InputStream in = toS3InputStream(queryExecutionId)) {
                frames.add(MetricFrameLoader.loadCsv(metricDefinition, in, true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            currEpochSecond = nextEpochSecond;
        }
        
        return MetricUtil.merge(frames);
    }
    
    private String buildAthenaQuery(MetricDefinition metricDefinition, long startSecond, long endSecond) {
        // FIXME Remove hardcodes
        final String lob = metricDefinition.getTags().getKv().get("lob");
        final String pos = metricDefinition.getTags().getKv().get("pos");
        return String.format(POS_QUERY_TEMPLATE, lob, pos, startSecond, endSecond);
    }
    
    private String submitAthenaQuery(String query) {
        log.info("Executing Athena query: {}", query);
        final QueryExecutionContext context = new QueryExecutionContext().withDatabase(database);
        final ResultConfiguration conf = new ResultConfiguration()
//                .withEncryptionConfiguration(encryptionConfiguration)
                .withOutputLocation("s3://" + outputBucket);
        final StartQueryExecutionRequest request = new StartQueryExecutionRequest()
                .withQueryString(query)
                .withQueryExecutionContext(context)
                .withResultConfiguration(conf);
        final StartQueryExecutionResult result = athena.startQueryExecution(request);
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
    }
    
    private InputStream toS3InputStream(String queryExecutionId) {
        // TODO Consider using the defaults here. They are nice because they make it easier to clean up old results.
        // https://docs.aws.amazon.com/athena/latest/ug/querying.html
        final String path = queryExecutionId + ".csv";
        final S3Object s3Obj = s3.getObject(outputBucket, path);
        return s3Obj.getObjectContent();
    }
}
