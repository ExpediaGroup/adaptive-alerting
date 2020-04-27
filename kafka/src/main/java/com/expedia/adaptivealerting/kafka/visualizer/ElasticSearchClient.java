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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

@Slf4j
public class ElasticSearchClient {

    private static int PORT1 = 9200;
    private static int PORT2 = 9201;
    private static String SCHEME = "http";
    private static String HOST = "host";
    private static String ELASTIC_SEARCH_CONFIG = "elastic-search";

    private RestHighLevelClient client;

    public ElasticSearchClient() {
        this.client = restClientBuilder(VisualizerUtility.getConfig(ELASTIC_SEARCH_CONFIG));
    }

    public BulkResponse bulk(BulkRequest bulkRequest, RequestOptions requestOptions) throws IOException {
        return client.bulk(bulkRequest, requestOptions);
    }

    protected RestHighLevelClient restClientBuilder(Config elasticSearchConfig) {
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

    public RestHighLevelClient getClient() {
        return client;
    }

    public boolean close() {
        try {
            client.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(),e);
        }
        return true;
    }

}
