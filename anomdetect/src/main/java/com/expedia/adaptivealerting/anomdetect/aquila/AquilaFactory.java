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
package com.expedia.adaptivealerting.anomdetect.aquila;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetectorFactory;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO Move this class to the Aquila repo. [WLW]

/**
 * @author Willie Wheeler
 */
@Slf4j
public class AquilaFactory extends AbstractAnomalyDetectorFactory<AquilaAnomalyDetector> {
    
    @Override
    public void init(String type, Config config) {
        super.init(type, config);
        
        // TODO
        log.info("Initialized AquilaFactory");
    }
    
    @Override
    public AquilaAnomalyDetector create(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        final AquilaAnomalyDetector detector = new AquilaAnomalyDetector(uuid);
        return detector;
    }
}
