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
package com.expedia.aquila.core.repo.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.core.util.jackson.ObjectMapperUtil;
import com.expedia.aquila.core.model.AquilaModel;
import com.expedia.aquila.core.model.AquilaModelMetadata;
import com.expedia.aquila.core.repo.AquilaModelRepo;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Slf4j
public class S3AquilaModelRepo implements AquilaModelRepo {
    private static final String CONFIG_KEY_REGION = "region";
    private static final String CONFIG_KEY_BUCKET = "bucket";
    private static final String CONFIG_KEY_FOLDER = "folder";
    
    private String region;
    private String bucket;
    private String folder;
    
    private AmazonS3 s3;
    private ObjectMapper objectMapper;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
    
        // These throw exceptions if the key is missing.
        this.region = config.getString(CONFIG_KEY_REGION);
        this.bucket = config.getString(CONFIG_KEY_BUCKET);
        this.folder = config.getString(CONFIG_KEY_FOLDER);
    
        log.info("Initializing S3PredictionModelRepo: region={}, bucket={}, folder={}", region, bucket, folder);
    
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new MetricsJavaModule());
    }
    
    @Override
    public void save(AquilaModel model, AquilaModelMetadata metadata) {
        notNull(model, "model can't be null");
        notNull(metadata, "metadata can't be null");
        
        final UUID detectorUuid = model.getDetectorUuid();
        
        log.info("Saving model: detectorUuid={}", detectorUuid);
    
        final String detectorPath = getDetectorPath(detectorUuid);
        final String modelStr = ObjectMapperUtil.writeValueAsString(objectMapper, model);
        s3.putObject(bucket, detectorPath, modelStr);
    
        final String metadataPath = getMetadataPath(detectorUuid);
        final String metadataStr = ObjectMapperUtil.writeValueAsString(objectMapper, metadata);
        s3.putObject(bucket, metadataPath, metadataStr);
    }
    
    @Override
    public AquilaModel load(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");
    
        final String detectorPath = getDetectorPath(detectorUuid);
        final S3Object s3Obj = s3.getObject(bucket, detectorPath);
        final InputStream content = new BufferedInputStream(s3Obj.getObjectContent());
        return ObjectMapperUtil.readValue(objectMapper, content, AquilaModel.class);
    }
    
    private String getDetectorPath(UUID detectorUuid) {
        return getBasePath(detectorUuid) + "/model.json";
    }
    
    private String getMetadataPath(UUID detectorUuid) {
        return getBasePath(detectorUuid) + "/metadata.json";
    }
    
    private String getBasePath(UUID detectorUuid) {
        return folder + "/" + detectorUuid;
    }
}
