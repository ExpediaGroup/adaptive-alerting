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
package com.expedia.adaptivealerting.core.data;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * Wraps an endpoint with a representation that includes anomaly detection information.
 * </p>
 * <p>
 * By contract the {@link Mpoint} must be set.
 * </p>
 *
 * @author Willie Wheeler
 */
public final class MappedMpoint {
    private Mpoint mpoint;
    private UUID detectorUuid;
    private String detectorType;
    private AnomalyResult anomalyResult;
    
    /**
     * To support serialization.
     */
    public MappedMpoint() {
    }
    
    public MappedMpoint(Mpoint mpoint, UUID detectorUuid, String detectorType) {
        notNull(mpoint, "mpoint can't be null");
        notNull(detectorUuid, "detectorUuid can't be null");
        notNull(detectorType, "detectorType can't be null");
        this.mpoint = mpoint;
        this.detectorUuid = detectorUuid;
        this.detectorType = detectorType;
    }
    
    public Mpoint getMpoint() {
        return mpoint;
    }
    
    public void setMpoint(Mpoint mpoint) {
        this.mpoint = mpoint;
    }
    
    public UUID getDetectorUuid() {
        return detectorUuid;
    }
    
    public void setDetectorUuid(UUID detectorUuid) {
        this.detectorUuid = detectorUuid;
    }
    
    public String getDetectorType() {
        return detectorType;
    }
    
    public void setDetectorType(String detectorType) {
        this.detectorType = detectorType;
    }
    
    public AnomalyResult getAnomalyResult() {
        return anomalyResult;
    }
    
    public void setAnomalyResult(AnomalyResult anomalyResult) {
        this.anomalyResult = anomalyResult;
    }
    
    @Override
    public String toString() {
        return "MappedMpoint{" +
                "mpoint=" + mpoint +
                ", detectorUuid=" + detectorUuid +
                ", detectorType='" + detectorType + '\'' +
                ", anomalyResult=" + anomalyResult +
                '}';
    }
}
