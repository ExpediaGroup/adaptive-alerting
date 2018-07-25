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
package com.expedia.aquila.repo.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.aquila.AquilaAnomalyDetector;
import com.expedia.aquila.model.PredictionModel;
import com.expedia.aquila.repo.DetectorModelRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public class DetectorModelS3Repo implements DetectorModelRepo {
    private static final Logger log = LoggerFactory.getLogger(DetectorModelS3Repo.class);
    
    private AmazonS3 s3;
    private String bucketName;
    private ObjectMapper objectMapper;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(config.getString("region"))
                .build();
        this.bucketName = config.getString("bucket.name");
        this.objectMapper = new ObjectMapper();
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    @Override
    public void save(AquilaAnomalyDetector detector) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public AquilaAnomalyDetector load(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        log.info("Loading AquilaAnomalyDetector: uuid={}", uuid);
        final String path = uuid.toString() + ".json";
        final S3Object s3Obj = s3.getObject(bucketName, path);
        final PredictionModel predModel = toPredictionModel(s3Obj);
        return new AquilaAnomalyDetector(predModel);
    }
    
    private PredictionModel toPredictionModel(S3Object s3Obj) {
        try {
            return objectMapper.readValue(new BufferedInputStream(s3Obj.getObjectContent()), PredictionModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
