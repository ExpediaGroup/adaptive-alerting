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
package com.expedia.adaptivealerting.modelservice;

import com.codahale.metrics.MetricRegistry;
import com.expedia.adaptivealerting.modelservice.util.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PreDestroy;
import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties
public class ModelServiceApp {

    @Autowired
    private DatabaseSettings settings;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public static void main(String[] args) {
        SpringApplication.run(ModelServiceApp.class, args);
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    //Adding a custom data source bean to avoid conflicting dataSource bean error. [KS]
    @Bean(name = "customDataSource")
    public DataSource dataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName(settings.getDriverClassName());
        dataSource.setUrl(settings.getUrl());
        dataSource.setUsername(settings.getUsername());
        dataSource.setPassword(settings.getPassword());
        return dataSource;
    }

    @Bean
    public RestHighLevelClient restClientBuilder(ElasticsearchProperties elasticSearchProperties) {
        RestClientBuilder builder = RestClient
                .builder(HttpHost.create(elasticSearchProperties.getUrls()))
                .setRequestConfigCallback(req -> {
                    req.setConnectionRequestTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    req.setConnectTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    req.setSocketTimeout(elasticSearchProperties.getConfig().getConnectionTimeout());
                    return req;
                }).setMaxRetryTimeoutMillis(elasticSearchProperties.getConfig().getConnectionRetryTimeout())
                .setHttpClientConfigCallback(req -> {
                    req.setMaxConnTotal(elasticSearchProperties.getConfig().getMaxTotalConnections());
                    req.setMaxConnPerRoute(500);
                    return req;
                });
        restHighLevelClient = new RestHighLevelClient(builder);
        return restHighLevelClient;
    }

    @PreDestroy
    public void destroy() throws IOException {
        restHighLevelClient.close();
    }
}
