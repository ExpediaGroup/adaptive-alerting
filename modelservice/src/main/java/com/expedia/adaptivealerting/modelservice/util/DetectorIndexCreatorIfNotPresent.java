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

import com.expedia.adaptivealerting.modelservice.elasticsearch.LegacyElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchProperties;
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

import static com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping.DETECTOR_CONFIG;
import static com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping.DETECTOR_CREATED_BY;
import static com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping.DETECTOR_CREATED_TIME;
import static com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping.DETECTOR_ID_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping.DETECTOR_MOD_TIME;
import static com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping.DETECTOR_TYPE;

/**
 * Util class to create index with mappings if not found.
 */
@Component
@Slf4j
public class DetectorIndexCreatorIfNotPresent implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ElasticSearchProperties properties;

    @Autowired
    private LegacyElasticSearchClient client;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (properties.isCreateIndexIfNotFound() && properties.getDetectorIndexName() != null && properties.getDetectorDocType() != null) {
            try {
                boolean isPresent = client.indices().exists(getIndexRequest(), RequestOptions.DEFAULT);
                if (!isPresent) {
                    val response = client.indices().create(createIndexRequest(), RequestOptions.DEFAULT);
                    if (!response.isAcknowledged()) {
                        throw new RuntimeException("Index creation failed");
                    }
                    log.info("Successfully created index: {}", properties.getDetectorIndexName());
                }
            } catch (IOException e) {
                log.error("Index creation failed", e);
                throw new RuntimeException(e);
            }
        }
    }

    private GetIndexRequest getIndexRequest() {
        val request = new GetIndexRequest();
        request.indices(properties.getDetectorIndexName());
        return request;
    }

    private CreateIndexRequest createIndexRequest() {
        val docObject = new JsonObject();
        docObject.addProperty("dynamic", "false");
        docObject.add("properties", buildMappingsJson());

        val mapObject = new JsonObject();
        mapObject.add(properties.getDetectorDocType(), docObject);

        val request = new CreateIndexRequest(properties.getDetectorIndexName());
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 3)
        );
        request.mapping(properties.getDetectorDocType(), mapObject.toString(), XContentType.JSON);
        return request;
    }

    private JsonObject buildMappingsJson() {
        val dynamicType = new JsonObject();
        dynamicType.addProperty("type", "nested");
        dynamicType.addProperty("dynamic", "true");

        val keywordType = new JsonObject();
        keywordType.addProperty("type", "keyword");

        val textType = new JsonObject();
        textType.addProperty("type", "text");

        val dateType = new JsonObject();
        dateType.addProperty("type", "date");

        val formattedDateType = new JsonObject();
        formattedDateType.addProperty("type", "date");
        formattedDateType.addProperty("format", "yyyy-MM-dd HH:mm:ss");

        val propObject = new JsonObject();
        propObject.add(DETECTOR_CONFIG, dynamicType);
        propObject.add(DETECTOR_TYPE, textType);
        propObject.add(DETECTOR_ID_KEYWORD, keywordType);
        propObject.add(DETECTOR_MOD_TIME, formattedDateType);
        propObject.add(DETECTOR_CREATED_TIME, dateType);
        propObject.add(DETECTOR_CREATED_BY, keywordType);
        return propObject;
    }
}
