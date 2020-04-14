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

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;


@Slf4j
public class ElasticSearchService implements Runnable {

    private static String HOST = "localhost";
    private static int PORT = 9200;
    private static String SCHEME = "http";
    private static String METHOD = "POST";
    private static String END_POINT = "/anomalies/doc";
    private Object object;

    public ElasticSearchService(Object Object) {
        this.object = object;
    }

    public Response execute() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT, SCHEME));
        RestClient restClient = builder.build();
        Response response = null;
        Request request = new Request(METHOD, END_POINT);

        request.setEntity(new NStringEntity(Utility.convertToJson(object), ContentType.APPLICATION_JSON));
        try {
            response = restClient.performRequest(request);
        }
        catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        log.info("response " + response.getStatusLine());
        return response;
    }

    @Override
    public void run() {
        execute();
    }
}
