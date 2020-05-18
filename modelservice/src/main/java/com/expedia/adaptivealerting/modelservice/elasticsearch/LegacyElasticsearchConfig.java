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
package com.expedia.adaptivealerting.modelservice.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Deprecated
public class LegacyElasticsearchConfig {

    @Autowired
    private ElasticSearchProperties elasticSearchProperties;

    @Bean(name = "legacyRestHighLevelClient", destroyMethod = "close")
    public RestHighLevelClient buildRestClient() {
        RestHighLevelClient elasticsearchClient = new RestHighLevelClient(buildRestClientBuilder());
        return elasticsearchClient;
    }

    private RestClientBuilder buildRestClientBuilder() {
        return RestClient.builder(
                HttpHost.create(
                        elasticSearchProperties.getUrls()))
                .setRequestConfigCallback(req -> {
                    req.setConnectionRequestTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    req.setConnectTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    req.setSocketTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    return req;
                })
                .setMaxRetryTimeoutMillis(elasticSearchProperties.getConfig().getConnectionRetryTimeout())
                .setHttpClientConfigCallback(req -> {
                    req.setMaxConnTotal(elasticSearchProperties.getConfig().getMaxTotalConnections());
                    req.setMaxConnPerRoute(500);
                    return req;
                });
    }
}
