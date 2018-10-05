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
package com.expedia.adaptivealerting.anomdetect;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.expedia.adaptivealerting.core.util.jackson.ObjectMapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * A basic anomaly detector factory that reads anomaly detector models as JSON from Amazon S3.
 * </p>
 * <p>
 * At some point we'll decouple this from Amazon, but for now this is fine.
 * </p>
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class BasicAnomalyDetectorFactory<T extends BasicAnomalyDetector> implements AnomalyDetectorFactory<T> {
    private Class<T> detectorClass;
    private String region;
    private String bucket;
    private String folder;
    
    private AmazonS3 s3;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
    
        this.detectorClass = ReflectionUtil.classForName(config.getString("detectorClass"));
        this.region = config.getString("region");
        this.bucket = config.getString("bucket");
        this.folder = config.getString("folder");
        
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
        
        log.info("Initialized factory: detectorClass={}, region={}, bucket={}, folder={}",
                detectorClass, region, bucket, folder);
    }
    
    @Override
    public T create(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        
        final String path = folder + "/" + uuid.toString() + ".json";
        log.info("Loading model: path={}", path);
        final S3Object s3Obj = s3.getObject(bucket, path);
        final InputStream is = s3Obj.getObjectContent();
        final AnomalyDetectorModel model = ObjectMapperUtil.readValue(objectMapper, is, AnomalyDetectorModel.class);
        log.info("Loaded model: {}", model);
        
        T detector = ReflectionUtil.newInstance(detectorClass);
        detector.init(model);
        return detector;
    }
}
