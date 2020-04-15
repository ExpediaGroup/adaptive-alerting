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

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ElasticSearchBulkService implements Runnable{

    private static int PORT1 = 9200;
    private static int PORT2 = 9201;
    private static String SCHEME = "http";
    private static String INDEX = "anomalies";
    private static String TYPE = "doc";
    private static String HOST = "host";

    private Config elasticSearchConfig = VisualizerUtility.getConfig("elastic-search");

    private RestHighLevelClient client = restClientBuilder();

    private List<AnomalyModel> anomalyModels;

    public ElasticSearchBulkService(List<AnomalyModel> anomalyModels) {
        this.anomalyModels = anomalyModels;
    }

    private void execute() {
        BulkResponse bulkResponse;
        BulkRequest bulkRequest = buildBulkRequest(this.anomalyModels);
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                log.error(bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(),e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private RestHighLevelClient restClientBuilder() {

        RestHighLevelClient client = null;
        try {
            client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(elasticSearchConfig.getString(HOST), PORT1, SCHEME),
                            new HttpHost(elasticSearchConfig.getString(HOST), PORT2, SCHEME)));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return client;
    }

    private BulkRequest buildBulkRequest(List<AnomalyModel> anomalyModels){
        BulkRequest bulkRequest = new BulkRequest();
        for (AnomalyModel anomalyModel : anomalyModels) {
            String json = VisualizerUtility.convertToJson(anomalyModel);
            bulkRequest.add(buildIndexRequest(json));
        }
        return bulkRequest;
    }

    private IndexRequest buildIndexRequest(String json) {
        IndexRequest request = new IndexRequest(INDEX, TYPE);
        request.source(json, XContentType.JSON);
        return request;
    }

    public void run(){
        execute();
    }
}
