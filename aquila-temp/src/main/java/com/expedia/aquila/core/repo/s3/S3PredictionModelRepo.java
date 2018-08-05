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
package com.expedia.aquila.core.repo.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.aquila.core.model.PredictionModel;
import com.expedia.aquila.core.repo.PredictionModelRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * S3-based repository for Aquila prediction models.
 *
 * @author Willie Wheeler
 */
public class S3PredictionModelRepo implements PredictionModelRepo {
    private AmazonS3 s3;
    private String bucketName;
    private ObjectMapper objectMapper;
    
    public S3PredictionModelRepo(Config config) {
        notNull(config, "config can't be null");
        
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(config.getString("region"))
                .build();
        this.bucketName = config.getString("bucketName");
        this.objectMapper = new ObjectMapper();
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    @Override
    public void save(UUID detectorUuid, PredictionModel predModel) {
        notNull(detectorUuid, "detectorUuid can't be null");
        notNull(predModel, "predModel can't be null");
        
        final String objPath = getObjectPath(detectorUuid);
        
        // TODO Add metadata.
        // See https://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html
        try {
            final String predModelStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(predModel);
            s3.putObject(bucketName, objPath, predModelStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public PredictionModel load(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");
        
        final String objPath = getObjectPath(detectorUuid);
        final S3Object s3Obj = s3.getObject(bucketName, objPath);
        try {
            return objectMapper.readValue(new BufferedInputStream(s3Obj.getObjectContent()), PredictionModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getObjectPath(UUID detectorUuid) {
        return detectorUuid.toString() + "/model.json";
    }
}
