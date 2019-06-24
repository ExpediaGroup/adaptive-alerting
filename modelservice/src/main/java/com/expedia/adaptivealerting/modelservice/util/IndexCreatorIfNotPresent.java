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
package com.expedia.adaptivealerting.modelservice.util;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.expedia.adaptivealerting.modelservice.request.PercolatorDetectorMapping.CREATE_TIME_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.request.PercolatorDetectorMapping.DETECTOR_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.request.PercolatorDetectorMapping.LAST_MOD_TIME_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.request.PercolatorDetectorMapping.QUERY_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.request.PercolatorDetectorMapping.USER_KEYWORD;

/**
 * Util class to create index with mappings if not found.
 */
@Component
@Slf4j
public class IndexCreatorIfNotPresent implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ElasticsearchProperties properties;

    @Autowired
    private ElasticsearchClient client;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (properties.isCreateIndexIfNotFound()) {
            try {
                boolean isPresent = client.indices().exists(getIndexRequest(), RequestOptions.DEFAULT);
                if (!isPresent) {
                    val response = client.indices().create(createIndexRequest(), RequestOptions.DEFAULT);
                    if (!response.isAcknowledged()) {
                        throw new RuntimeException("Index creation failed");
                    }
                    log.info("Successfully created index: {}", properties.getIndexName());
                }
            } catch (IOException e) {
                log.error("Index creation failed", e);
                throw new RuntimeException(e);
            }
        }
    }

    private GetIndexRequest getIndexRequest() {
        val request = new GetIndexRequest();
        request.indices(properties.getIndexName());
        return request;
    }

    private CreateIndexRequest createIndexRequest() {
        val docObject = new JsonObject();
        docObject.addProperty("dynamic", "false");
        docObject.add("properties", buildMappingsJson());

        val mapObject = new JsonObject();
        mapObject.add(properties.getDocType(), docObject);

        val request = new CreateIndexRequest(properties.getIndexName());
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 3)
        );
        request.mapping(properties.getDocType(), mapObject.toString(), XContentType.JSON);
        return request;
    }

    private JsonObject buildMappingsJson() {
        val dynamicType = new JsonObject();
        dynamicType.addProperty("type", "nested");
        dynamicType.addProperty("dynamic", "true");

        val dispatcherType = new JsonObject();
        dispatcherType.addProperty("type", "object");

        val queryType = new JsonObject();
        queryType.addProperty("type", "percolator");

        val timeType = new JsonObject();
        timeType.addProperty("type", "long");

        val propObject = new JsonObject();
        propObject.add(USER_KEYWORD, dynamicType);
        propObject.add(DETECTOR_KEYWORD, dynamicType);
        propObject.add(QUERY_KEYWORD, queryType);
        propObject.add(LAST_MOD_TIME_KEYWORD, timeType);
        propObject.add(CREATE_TIME_KEYWORD, timeType);
        return propObject;
    }
}
