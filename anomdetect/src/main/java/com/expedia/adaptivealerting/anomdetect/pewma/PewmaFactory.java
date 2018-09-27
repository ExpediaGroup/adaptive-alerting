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
package com.expedia.adaptivealerting.anomdetect.pewma;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetectorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
@Slf4j
public final class PewmaFactory extends AbstractAnomalyDetectorFactory<PewmaAnomalyDetector> {
    
    // TODO Move this AWS-specific code out of this factory.
    // The actual param load code will go in modelservice-s3. [WLW]
    private AmazonS3 s3;
    private String folder;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void init(String type, Config config) {
        super.init(type, config);
    
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(getRegion())
                .build();
        this.folder = type;
    
        log.info("Initialized PewmaFactory: folder={}", folder);
    }
    
    @Override
    public PewmaAnomalyDetector create(UUID uuid) {
        notNull(uuid, "uuid can't be null");
    
        final String path = folder + "/" + uuid.toString() + ".json";
        final S3Object s3Obj = s3.getObject(getBucket(), path);
        final InputStream is = s3Obj.getObjectContent();
    
        PewmaModel model;
        try {
            log.info("Loading model for detectorUuid={} from S3: bucket={}, path={}", uuid, getBucket(), path);
            model = objectMapper.readValue(is, PewmaModel.class);
        } catch (Exception e) {
            log.error("{} while loading model for detectorUuid={}: {}", e.getClass().getName(), uuid, e.getMessage());
            return null;
        }
        log.info("Loaded model: {}", model);
        return new PewmaAnomalyDetector(uuid, model.getParams());
    }
}
