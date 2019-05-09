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
package com.expedia.adaptivealerting.modelservice.dao.es;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.common.base.Supplier;
import lombok.Data;
import lombok.Generated;
import lombok.val;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Configuration
@Data
public class ElasticSearchConfig {

    @Value("${datasource-es.index.name}")
    private String indexName;
    @Value("${datasource-createIndexIfNotFound:false}")
    private boolean createIndexIfNotFound;
    @Value("${datasource-es.doctype}")
    private String docType;
    @Value("${datasource-es.urls}")
    private String urls;
    @Value("${datasource-es.config.connection.timeout}")
    private int connectionTimeout;
    @Value("${datasource-es.config.connection.retry.timeout}")
    private int retryTimeout;
    @Value("${datasource-es.config.max.total.connection}")
    private int maxTotalConnection;
    @Value("${datasource-es.config.username:@null}")
    private String username;
    @Value("${datasource-es.config.password:@null}")
    private String password;
    @Value("${datasource-es.config.aws-iam-auth-required:false}")
    private boolean needsAWSIAMAuth;
    @Value("${datasource-es.config.aws-region:@null}")
    private String awsRegion;

    private RestHighLevelClient client;

    @PostConstruct
    @Generated // (excluding from code coverage)
    public void init() {
        RestClientBuilder builder  = RestClient
                .builder(HttpHost.create(urls))
                .setRequestConfigCallback( req -> {
                    req.setConnectionRequestTimeout(connectionTimeout);
                    req.setConnectTimeout(connectionTimeout);
                    req.setSocketTimeout(connectionTimeout);
                    return req;
                }).setMaxRetryTimeoutMillis(retryTimeout)
                .setHttpClientConfigCallback(req -> {
                            req.setMaxConnTotal(maxTotalConnection);
                            req.setMaxConnPerRoute(500);
                            return req;
                });
        addAWSRequestSignerInterceptor(builder);
        client = new RestHighLevelClient(builder);
    }
    
    private void addAWSRequestSignerInterceptor(RestClientBuilder clientBuilder) {
        if (needsAWSIAMAuth) {
            AWSSigningRequestInterceptor signingInterceptor = getAWSRequestSignerInterceptor();
            clientBuilder.setHttpClientConfigCallback(
                    clientConf -> clientConf.addInterceptorLast(signingInterceptor));
        }
    }
    
    private AWSSigningRequestInterceptor getAWSRequestSignerInterceptor() {
        final Supplier<LocalDateTime> clock = () -> LocalDateTime.now(ZoneOffset.UTC);
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        val awsSigner = new AWSSigner(credentialsProvider, awsRegion, "es", clock);
        return new AWSSigningRequestInterceptor(awsSigner);
    }

    @PreDestroy
    public void destroy() throws IOException {
        client.close();
    }

    @Bean
    public RestHighLevelClient getClient() {
        return client;
    }
}
