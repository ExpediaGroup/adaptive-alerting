package com.expedia.adaptivealerting.modelservice.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
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
    private ElasticSearchProperties properties;

    @Bean
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {
        RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200)));
        return esClient;
    }
}
