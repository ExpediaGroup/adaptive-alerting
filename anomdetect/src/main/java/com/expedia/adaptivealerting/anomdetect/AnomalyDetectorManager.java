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

import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Component that manages a given set of anomaly detectors.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
public final class AnomalyDetectorManager {
    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectorManager.class);
    
    /**
     * Factories that know how to produce anomaly detectors on demand.
     */
    private final Map<String, AnomalyDetectorFactory> detectorFactories;
    
    /**
     * The managed detectors.
     */
    private final Map<UUID, AnomalyDetector> detectors = new HashMap<>();
    
    public AnomalyDetectorManager(Config factoryConfig) {
        notNull(factoryConfig, "factoryConfig can't be null");
        this.detectorFactories = new HashMap<>();
        factoryConfig.entrySet().forEach(entry -> {
            final String className = entry.getValue().unwrapped().toString();
            final AnomalyDetectorFactory factory = (AnomalyDetectorFactory) ReflectionUtil.newInstance(className);
            factory.init(factoryConfig);
            detectorFactories.put(entry.getKey(), factory);
        });
    }
    
    /**
     * Creates a new anomaly detector manager.
     *
     * @param detectorFactories Mapping of detector types to their corresponding factories.
     */
    public AnomalyDetectorManager(Map<String, AnomalyDetectorFactory> detectorFactories) {
        notNull(detectorFactories, "detectorFactories can't be null");
        this.detectorFactories = detectorFactories;
    }
    
    /**
     * Gets the anomaly detector for the given metric point, creating it if absent. Returns {@code null} if there's no
     * {@link AnomalyDetectorFactory} defined for the mapped mpoint's detector type.
     *
     * @param mappedMpoint Mapped metric point.
     * @return Anomaly detector for the given metric point, or {@code null} if there's no registered detector factory
     * for the mapped mpoint's detector type.
     */
    public AnomalyDetector detectorFor(MappedMpoint mappedMpoint) {
        notNull(mappedMpoint, "mappedMpoint can't be null");
        final UUID detectorUuid = mappedMpoint.getDetectorUuid();
        AnomalyDetector detector = detectors.get(detectorUuid);
        if (detector == null) {
            final String detectorType = mappedMpoint.getDetectorType();
            final AnomalyDetectorFactory factory = detectorFactories.get(detectorType);
            if (factory == null) {
                log.warn("No AnomalyDetectorFactory registered for detectorType={}", detectorType);
            } else {
                detector = factory.create(detectorUuid);
                detectors.put(detectorUuid, detector);
            }
        }
        return detector;
    }
    
    /**
     * <p>
     * Convenience method to classify the mapped metric point, performing detector lookup behind the scenes. Note that
     * this method has a side-effect in that it updates the passed mapped metric point itself.
     * </p>
     * <p>
     * Returns {@code null} if there's no detector defined for the given mapped mpoint.
     * </p>
     *
     * @param mappedMpoint Mapped metric point.
     * @return The mapped metric point, or {@code null} if there's no associated detector.
     */
    public MappedMpoint classify(MappedMpoint mappedMpoint) {
        notNull(mappedMpoint, "mappedMpoint can't be null");
        final AnomalyDetector detector = detectorFor(mappedMpoint);
        return detector == null ? null : detector.classify(mappedMpoint);
    }
}
