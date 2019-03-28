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
package com.expedia.adaptivealerting.anomdetect.detector;

import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import lombok.Getter;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for anomaly detector implementations. {@link #init(UUID, DetectorParams, AnomalyType)} method
 * supports reflection-based instantiation.
 */
public abstract class AbstractDetector<T extends DetectorParams> implements Detector<T> {

    @Getter
    private final Class<T> paramsClass;

    @Getter
    private UUID uuid;

    @Getter
    private T params;

    @Getter
    private AnomalyType anomalyType;

    protected AbstractDetector(Class<T> paramsClass) {
        notNull(paramsClass, "paramsClass can't be null");
        this.paramsClass = paramsClass;
    }

    @Override
    public void init(UUID uuid, T params, AnomalyType anomalyType) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        notNull(anomalyType, "anomalyType can't be null");

        params.validate();
        this.uuid = uuid;
        this.params = params;
        this.anomalyType = anomalyType;

        initComponents(params);
        initState(params);
    }

    /**
     * Initialize components. Subclasses can implement this as needed.
     *
     * @param params detector params
     */
    protected void initComponents(T params) {
        // Override as desired
    }

    /**
     * Subclasses can implement this to initialize implementation-specific state.
     *
     * @param params detector params
     */
    protected void initState(T params) {
        // Override as desired
    }
}
