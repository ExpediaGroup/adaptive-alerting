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
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elastic search config used by spring data elastic search
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.expedia.adaptivealerting.modelservice.repo")
public class ElasticsearchConfig {

    @Autowired
    private ElasticSearchProperties elasticSearchProperties;

    @Bean
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {
        RestHighLevelClient esClient = new RestHighLevelClient(buildRestClientBuilder());
        return esClient;
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
