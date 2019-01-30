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
package com.expedia.adaptivealerting.anomdetect.source;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * A basic anomaly detector factory that reads anomaly detector models as JSON.
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class DefaultDetectorFactory<T extends AbstractAnomalyDetector> implements DetectorFactory<T> {
    private Class<T> detectorClass;
    
    // TODO Currently require a specific implementation because this factory class is the one that contains the detector
    // creation logic, rather than the DefaultDetectorSource itself containing it. So we just load the backing model
    // using the DefaultDetectorSource. But we want to push the detector creation logic back to the
    // DefaultDetectorSource. [WLW]
    private DefaultDetectorSource detectorSource;

    @Override
    public void init(Config config, DetectorSource detectorSource) {
        notNull(config, "config can't be null");
        notNull(detectorSource, "detectorSource can't be null");
        
        if (!(detectorSource instanceof DefaultDetectorSource)) {
            throw new IllegalArgumentException("Currently, detectorSource must be a DefaultDetectorSource");
        }
        
        this.detectorClass = ReflectionUtil.classForName(config.getString("detectorClass"));
        this.detectorSource = (DefaultDetectorSource) detectorSource;
        
        log.info("Initialized factory: detectorClass={}", detectorClass.getName());
    }

    @Override
    public T create(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");
        
        val model = detectorSource.findModelByDetectorUuid(detectorUuid);
        
        if (model == null) {
            log.error("No model found for detectorUuid={}", detectorUuid);
            
            // TODO Is this how we want to handle this? [WLW]
            return null;
        }
    
        log.info("Loaded model for detectorUuid={}: {}", detectorUuid, model);
        
        T detector = ReflectionUtil.newInstance(detectorClass);
        detector.init(model);
        return detector;
    }
}
