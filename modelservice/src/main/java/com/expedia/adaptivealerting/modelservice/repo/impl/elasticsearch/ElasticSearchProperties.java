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
package com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.common.base.Supplier;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Component
@ConfigurationProperties(prefix = "datasource-es")
public class ElasticSearchProperties {

    private String indexName;
    private boolean createIndexIfNotFound;
    private String docType;
    private String urls;
    private String detectorIndexName;
    private String detectorDocType;

    @Data
    @Accessors(chain = true)
    public static class Config {
        private int connectionTimeout;
        private int connectionRetryTimeout;
        private int maxTotalConnections;
        private int username;
        private int password;
        private boolean awsIamAuthRequired;
        private String awsRegion;
    }

    private Config config;

    public void addAWSRequestSignerInterceptor(RestClientBuilder clientBuilder) {
        if (config.isAwsIamAuthRequired()) { // this is optional security for elastic search running in AWS
            AWSSigningRequestInterceptor signingInterceptor = getAWSRequestSignerInterceptor();
            clientBuilder.setHttpClientConfigCallback(
                clientConf -> clientConf.addInterceptorLast(signingInterceptor));
        }
    }

    private AWSSigningRequestInterceptor getAWSRequestSignerInterceptor() {
        final Supplier<LocalDateTime> clock = () -> LocalDateTime.now(ZoneOffset.UTC);
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        val awsSigner = new AWSSigner(credentialsProvider, config.getAwsRegion(), "es", clock);
        return new AWSSigningRequestInterceptor(awsSigner);
    }
}
