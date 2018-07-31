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

import java.util.Date;

/**
 * @author kashah
 *
 */
public class ModelDto {

    private String modelUUID;
    private boolean toRebuild;
    private Date buildTimestamp;

    public ModelDto(String modelUUID, boolean toRebuild) {
        this.modelUUID = modelUUID;
        this.toRebuild = toRebuild;
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
    public Date getBuildTimestamp() {
        return buildTimestamp;
    }

    /**
     * @param buildTimestamp
     *            the buildTimestamp to set
     */
    public void setBuildTimestamp(Date buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }
}
