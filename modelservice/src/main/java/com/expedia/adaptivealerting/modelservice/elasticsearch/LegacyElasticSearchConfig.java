package com.expedia.adaptivealerting.modelservice.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Legacy elastic search config
 * Spring data elastic search doesn't support percolator right now. It should be available in upcoming release.
 * So we need to maintain an another elastic search client till then for percolate queries
 */
@Configuration
@Deprecated
public class LegacyElasticSearchConfig {

    @Bean(name = "legacyRestHighLevelClient")
    public RestHighLevelClient restClientBuilder(ElasticSearchProperties elasticSearchProperties) {
        RestClientBuilder builder = RestClient
                .builder(HttpHost.create(elasticSearchProperties.getUrls()))
                .setRequestConfigCallback(req -> {
                    req.setConnectionRequestTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    req.setConnectTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    req.setSocketTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    return req;
                });
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        return restHighLevelClient;
    }
}
