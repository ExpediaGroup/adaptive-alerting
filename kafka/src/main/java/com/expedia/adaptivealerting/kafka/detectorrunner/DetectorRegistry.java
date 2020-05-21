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

package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class DetectorRegistry {

    private Config config = new TypesafeConfigLoader("detector-runner").loadMergedConfig();
    private Detector detector;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    public DetectorRegistry(ListableBeanFactory beanFactory) {
        Collection<Detector> detectorBeans = beanFactory.getBeansOfType(Detector.class).values();
        this.detector = detectorBeans.stream().filter(d -> config.getString("detector").equalsIgnoreCase(d.getName()))
                .findFirst().get();
        if(detector != null) {
            log.info("Detector in use is : " + detector.getName());
        } else {
            log.error("No valid detector configured.");
        }
    }

    public Detector getDetector() {
        return detector;
    }
}
