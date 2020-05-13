/*
 * Copyright 2018-2019 Expedia Group, Inc.
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

package com.expedia.adaptivealerting.kafka.visualizer;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

import java.io.IOException;

@Slf4j
public class ElasticSearchDataCleaner implements Runnable {

    private static String INDEX = "anomalies";
    private static String FIELD = "timestamp";
    private static String RETENTION_PERIOD_DAYS = "7";

    public ElasticSearchDataCleaner() {
    }

    @Override
    public void run() {
        ElasticSearchClient elasticSearchClient = new ElasticSearchClient();
        deleteData(elasticSearchClient);
    }

    public BulkByScrollResponse deleteData(ElasticSearchClient elasticSearchClient) {
        log.info("deleting old data");
        BulkByScrollResponse bulkByScrollResponse = null;
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(INDEX);
        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder(FIELD).lt("now-" + RETENTION_PERIOD_DAYS + "d/d");
        deleteByQueryRequest.setQuery(rangeQueryBuilder);
        try {
            bulkByScrollResponse = elasticSearchClient
                    .deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
            log.info(bulkByScrollResponse.toString());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(),e);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(),e);
        }
        finally {
            elasticSearchClient.close();
        }
        return bulkByScrollResponse;
    }
}
