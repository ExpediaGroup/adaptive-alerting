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
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//TODO Rename this to ElasticsearchProperties
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

    //TODO Update the elastic search config to use host and port instead of storing whole URL as a string
    @Data
    @Accessors(chain = true)
    public static class Url {
        private String host;
        private int port;
    }

    public static Url extractHostAndPortFromUrl(String urls) {
        if (urls == null) {
            throw new MissingSystemPropertyException("Elastic search URL not set in config");
        }
        String[] arrOfUrl = urls.split(":");
        if (arrOfUrl.length <= 1) {
            throw new MissingSystemPropertyException("Use host:port format to set URL in the config");
        }

        Url url = new Url();
        url.setHost(arrOfUrl[0]);
        url.setPort(Integer.parseInt(arrOfUrl[1]));
        return url;
    }
}
