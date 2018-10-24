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

import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Basic anomaly detector abstract class. A basic anomaly detector is one that generally has a single fixed model.
 *
 * @author Willie Wheeler
 */
public abstract class BasicAnomalyDetector<T> implements AnomalyDetector {
    protected abstract Class<T> getParamsClass();
    protected abstract void loadParams(T params);
    
    @Getter
    private UUID uuid;
    
    /**
     * Initializes this detector with the given model.
     *
     * @param modelResource Model resource.
     */
    public void init(ModelResource modelResource) {
        Assert.notNull(modelResource, "modelResource can't be null");
        this.uuid = modelResource.getUuid();
        
        T params = new ObjectMapper().convertValue(modelResource.getParams(), getParamsClass());
        loadParams(params);
    }
    
    protected void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
