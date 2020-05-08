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

import com.expedia.adaptivealerting.modelservice.exception.MissingSystemPropertyException;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> stringMap = extractHostAndPortFromUrl();
        String host = stringMap.get("host");
        int port = Integer.parseInt(stringMap.get("port"));
        RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port)));
        return esClient;
    }

    private Map<String, String> extractHostAndPortFromUrl() {
        String url = elasticSearchProperties.getUrls();
        if (url == null) {
            throw new MissingSystemPropertyException("Elastic search URL not set in config");
        }
        String[] arrOfUrl = url.split(":");
        if (arrOfUrl.length <= 1) {
            throw new MissingSystemPropertyException("Use host:port format to set URL in the config");
        }

        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("host", arrOfUrl[0]);
        stringMap.put("port", arrOfUrl[1]);
        return stringMap;
    }
}
