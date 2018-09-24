/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.cusum;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO DRY up this code, which is duplicated in the ConstantThresholdFactory. [WLW]

/**
 * @author Willie Wheeler
 */
@Slf4j
public final class CusumFactory implements AnomalyDetectorFactory<CusumAnomalyDetector> {
    
    // TODO Move this AWS-specific code out of this factory.
    // The actual param load code will go in modelservice-s3. [WLW]
    private AmazonS3 s3;
    private String folder;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void init(String type, Config config) {
        notNull(type, "type can't be null");
        notNull(config, "config can't be null");
    
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(REGION)
                .build();
        this.folder = type;
        
        log.info("Initialized CusumFactory: folder={}", folder);
    }
    
    @Override
    public CusumAnomalyDetector create(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        
        final String path = folder + "/" + uuid.toString() + ".json";
        final S3Object s3Obj = s3.getObject(BUCKET, path);
        final InputStream is = s3Obj.getObjectContent();
        
        CusumModel model;
        try {
            model = objectMapper.readValue(is, CusumModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        log.info("Loaded model: {}", model);
        return new CusumAnomalyDetector(uuid, model.getParams());
    }
}
