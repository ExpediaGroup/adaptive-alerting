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
package com.expedia.adaptivealerting.dataservice.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.core.data.io.MetricFileInfo;
import com.expedia.adaptivealerting.dataservice.AbstractDataService;
import com.expedia.adaptivealerting.dataservice.DataService;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * S3-based {@link DataService} implementation.
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class S3DataService extends AbstractDataService {
    private AmazonS3 s3;
    private String bucketName;
    
    @Override
    public void init(Config config) {
        super.init(config);
        
        final String region = config.getString("region");
        final String bucketName = config.getString("bucketName");
        
        notNull(region, "Property region must be defined");
        notNull(bucketName, "Property bucketName must be defined");
        
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
        this.bucketName = bucketName;
        
        log.info("Initialized S3DataService with bucketName={}", this.bucketName);
    }
    
    @Override
    protected InputStream toInputStream(MetricFileInfo meta, Instant date) throws IOException {
        final String path = meta.getLocation().toMetricFilePath(date);
        final S3Object s3Obj = s3.getObject(bucketName, path);
        return s3Obj.getObjectContent();
    }
}
