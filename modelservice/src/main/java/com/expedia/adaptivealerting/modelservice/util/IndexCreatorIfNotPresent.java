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

import com.expedia.adaptivealerting.modelservice.dao.es.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.dao.es.ElasticSearchConfig;
import com.google.gson.JsonObject;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.expedia.adaptivealerting.modelservice.dao.es.DetectorMappingEntity.CREATE_TIME_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.dao.es.DetectorMappingEntity.DETECTOR_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.dao.es.DetectorMappingEntity.LAST_MOD_TIME_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.dao.es.DetectorMappingEntity.QUERY_KEYWORD;
import static com.expedia.adaptivealerting.modelservice.dao.es.DetectorMappingEntity.USER_KEYWORD;

/**
 * Util class to create index with mappings if not found.
 */
@Component
@Slf4j
@Generated //(exclude from code coverage)
public class IndexCreatorIfNotPresent implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    @Autowired
    private ElasticSearchConfig elasticSearchConfig;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (elasticSearchConfig.isCreateIndexIfNotFound()) {
            try {
                GetIndexRequest getIndexRequest = new GetIndexRequest();
                getIndexRequest.indices(elasticSearchConfig.getIndexName());
                boolean isPresent = elasticSearchClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

                if (!isPresent) {
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(elasticSearchConfig.getIndexName());
                    createIndexRequest.settings(Settings.builder()
                            .put("index.number_of_shards", 5)
                            .put("index.number_of_replicas", 3)
                    );

                    JsonObject docObject = new JsonObject();
                    docObject.addProperty("dynamic", "false");
                    docObject.add("properties", buildMappingsJson());
                    JsonObject mapObject = new JsonObject();
                    mapObject.add(elasticSearchConfig.getDocType(), docObject);
                    createIndexRequest.mapping(elasticSearchConfig.getDocType(), mapObject.toString(),
                            XContentType.JSON);
                    CreateIndexResponse response = elasticSearchClient.indices().create(createIndexRequest,
                            RequestOptions.DEFAULT);
                    if (!response.isAcknowledged()) {
                        throw new RuntimeException("Index Creation failed");
                    }
                    log.info("Successfully created index : " + elasticSearchConfig.getIndexName());
                }
            } catch (IOException e) {
                log.error("Index creation failed", e);
                throw new RuntimeException(e);
            }
        }
    }

    private JsonObject buildMappingsJson() {
        JsonObject dynamicTypeObject = new JsonObject();
        dynamicTypeObject.addProperty("type", "nested");
        dynamicTypeObject.addProperty("dynamic", "true");
        JsonObject dispatcherTypeObject = new JsonObject();
        dispatcherTypeObject.addProperty("type", "object");
        JsonObject queryTypeObject = new JsonObject();
        queryTypeObject.addProperty("type", "percolator");
        JsonObject timeTypeObject = new JsonObject();
        timeTypeObject.addProperty("type", "long");

        JsonObject propObject = new JsonObject();
        propObject.add(USER_KEYWORD, dynamicTypeObject);
        propObject.add(DETECTOR_KEYWORD, dynamicTypeObject);
        propObject.add(QUERY_KEYWORD, queryTypeObject);
        propObject.add(LAST_MOD_TIME_KEYWORD, timeTypeObject);
        propObject.add(CREATE_TIME_KEYWORD, timeTypeObject);
        return propObject;
    }
}
