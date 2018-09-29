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
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.AmazonAthenaClientBuilder;
import com.amazonaws.services.athena.model.GetQueryExecutionRequest;
import com.amazonaws.services.athena.model.GetQueryExecutionResult;
import com.amazonaws.services.athena.model.QueryExecutionContext;
import com.amazonaws.services.athena.model.QueryExecutionState;
import com.amazonaws.services.athena.model.ResultConfiguration;
import com.amazonaws.services.athena.model.StartQueryExecutionRequest;
import com.amazonaws.services.athena.model.StartQueryExecutionResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.core.data.io.MetricFileInfo;
import com.expedia.adaptivealerting.dataservice.AbstractDataService;
import com.expedia.adaptivealerting.dataservice.DataService;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * AWS Athena-based {@link DataService} implementation. This implementation queries an Athena database and then writes
 * the query results into a configurable S3 bucket.
 *
 * @author Willie Wheeler
 */
@Slf4j
public class AthenaDataService extends AbstractDataService {
    private static final String CONFIG_KEY_REGION = "region";
    private static final String CONFIG_KEY_DATABASE = "database";
    private static final String CONFIG_KEY_OUTPUT_BUCKET = "outputBucket";
    private static final String CONFIG_KEY_CLIENT_EXECUTION_TIMEOUT = "clientExecutionTimeout";
    
    // FIXME Temporarily using a hardcoded query.
    private static final String QUERY = "SELECT * FROM bookings_test WHERE lob='air' AND pos='expedia.com' AND timestamp BETWEEN 1517443200 AND 1517443200";
    
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
        super.init(config);
    
        this.region = config.getString(CONFIG_KEY_REGION);
        this.database = config.getString(CONFIG_KEY_DATABASE);
        this.outputBucket = config.getString(CONFIG_KEY_OUTPUT_BUCKET);
        
        notNull(region, "Property 'region' must be defined");
        notNull(database, "Property 'database' must be defined");
        notNull(outputBucket, "Property 'outputBucket' must be defined");
        
        if (config.hasPath(CONFIG_KEY_CLIENT_EXECUTION_TIMEOUT)) {
            this.clientExecutionTimeout = config.getInt(CONFIG_KEY_CLIENT_EXECUTION_TIMEOUT);
        }
        
        log.info("Initializing AthenaDataService: region={}, database={}, outputBucket={}, clientExecutionTimeout={}",
                region, database, outputBucket, clientExecutionTimeout);
        
        final ClientConfiguration clientConfig =
                new ClientConfiguration().withClientExecutionTimeout(clientExecutionTimeout);
    
        this.athena = AmazonAthenaClientBuilder.standard()
                .withRegion(region)
//                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withClientConfiguration(clientConfig)
                .build();
        
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
//                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withClientConfiguration(clientConfig)
                .build();
        
        log.info("Successfully initialized AthenaDataService");
    }
    
    public AmazonAthena getAthena() {
        return athena;
    }
    
    @Override
    protected InputStream toInputStream(MetricFileInfo meta, Instant date) throws IOException {
        final String queryExecutionId = submitAthenaQuery();
        
        try {
            waitForQueryToComplete(queryExecutionId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    
//        processResultRows(queryExecutionId);
        return toS3InputStream(queryExecutionId);
    }
    
    private String submitAthenaQuery() {
        final QueryExecutionContext context = new QueryExecutionContext().withDatabase(database);
        final ResultConfiguration conf = new ResultConfiguration()
//                .withEncryptionConfiguration(encryptionConfiguration)
                .withOutputLocation(outputBucket);
        final StartQueryExecutionRequest request = new StartQueryExecutionRequest()
                .withQueryString(QUERY)
                .withQueryExecutionContext(context)
                .withResultConfiguration(conf);
        final StartQueryExecutionResult result = athena.startQueryExecution(request);
        return result.getQueryExecutionId();
    }
    
    private void waitForQueryToComplete(String queryExecutionId) throws InterruptedException {
        final GetQueryExecutionRequest request = new GetQueryExecutionRequest()
                .withQueryExecutionId(queryExecutionId);
        
        GetQueryExecutionResult result = null;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            result = athena.getQueryExecution(request);
            final String queryState = result.getQueryExecution().getStatus().getState();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("Query failed: " +
                        result.getQueryExecution().getStatus().getStateChangeReason());
            } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("Query cancelled.");
            } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            } else {
                Thread.sleep(1000L);
            }
            log.trace("Current query status: {}", queryState);
        }
    }
    
    private InputStream toS3InputStream(String queryExecutionId) {
        // TODO Consider using the defaults here. They are nice because they make it easier to clean up old results.
        // https://docs.aws.amazon.com/athena/latest/ug/querying.html
        final String path = queryExecutionId + ".csv";
        
        // FIXME FIXME FIXME
        // This should be the bucket name not the output location
        // FIXME FIXME FIXME
        final S3Object s3Obj = s3.getObject(outputBucket, path);
        return s3Obj.getObjectContent();
    }
}
