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

package com.expedia.adaptivealerting.modelservice.dto;

import java.time.Instant;
import java.util.Map;

/**
 * @author kashah
 *
 */
public class ModelDto {

    private String modelUUID;
    /**
     * @return the hyperParams
     */
    public Object getHyperParams() {
        return hyperParams;
    }

    /**
     * @param hyperParams the hyperParams to set
     */
    public void setHyperParams(Object hyperParams) {
        this.hyperParams = hyperParams;
    }

    /**
     * @return the thresholds
     */
    public Object getThresholds() {
        return thresholds;
    }

    /**
     * @param thresholds the thresholds to set
     */
    public void setThresholds(Object thresholds) {
        this.thresholds = thresholds;
    }

    private Object hyperParams;
    private Object thresholds;
    private boolean toRebuild;
    private Instant buildTimestamp;

    public ModelDto(String modelUUID, Object hyperParams, Object thresholds,
            boolean toRebuild, Instant buildTimestamp) {
        this.modelUUID = modelUUID;
        this.hyperParams = hyperParams;
        this.thresholds = thresholds;
        this.toRebuild = toRebuild;
        this.buildTimestamp = buildTimestamp;
    }

    /**
     * @return the modelUUID
     */
    public String getModelUUID() {
        return modelUUID;
    }

    /**
     * @param modelUUID
     *            the modelUUID to set
     */
    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    /**
     * @return the toRebuild
     */
    public boolean isToRebuild() {
        return toRebuild;
    }

    /**
     * @param toRebuild
     *            the toRebuild to set
     */
    public void setToRebuild(boolean toRebuild) {
        this.toRebuild = toRebuild;
    }

    /**
     * @return the buildTimestamp
     */
    public Instant getBuildTimestamp() {
        return buildTimestamp;
    }

    /**
     * @param buildTimestamp
     *            the buildTimestamp to set
     */
    public void setBuildTimestamp(Instant buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }
}
