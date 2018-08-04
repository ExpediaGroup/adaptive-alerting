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
package com.expedia.adaptivealerting.dataconnect;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class S3DataConnector implements DataConnector {
    private static final Logger log = LoggerFactory.getLogger(S3DataConnector.class);
    
    private AmazonS3 s3;
    private String bucketName;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        this.s3 = AmazonS3ClientBuilder.standard()
                .withRegion(config.getString("region"))
                .build();
        this.bucketName = config.getString("bucket.name");
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    @Override
    public MetricFrame load(Metric metric, String path) {
        notNull(metric, "metric can't be null");
        notNull(path, "path can't be null");
        
        final S3Object s3Obj = s3.getObject(bucketName, path);
        
        try {
            return MetricFrameLoader.loadCsv(metric, s3Obj.getObjectContent(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
