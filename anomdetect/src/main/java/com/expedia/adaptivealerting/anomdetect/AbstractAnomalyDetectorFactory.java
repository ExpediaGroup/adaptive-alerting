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

import com.typesafe.config.Config;
import lombok.Getter;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for implementing {@link AnomalyDetectorFactory}.
 *
 * @author Willie Wheeler
 */
public abstract class AbstractAnomalyDetectorFactory<T extends AnomalyDetector> implements AnomalyDetectorFactory<T> {
    
    @Getter
    private String type;
    
    @Getter
    private String region;
    
    @Getter
    private String bucket;
    
    public void init(String type, Config factoriesConfig) {
        notNull(type, "type can't be null");
        notNull(factoriesConfig, "factoriesConfig can't be null");
        
        this.type = type;
        this.region = factoriesConfig.getString("region");
        this.bucket = factoriesConfig.getString("bucket");
    }
}
