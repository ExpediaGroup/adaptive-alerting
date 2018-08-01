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

import lombok.Data;

import java.time.Instant;

/**
 * @author kashah
 *
 */
@Data
public class ModelDto {

    private String modelUUID;
    private Object hyperParams;
    private Object thresholds;
    private boolean toRebuild;
    private Instant buildTimestamp;

    public ModelDto(String modelUUID, Object hyperParams, Object thresholds, boolean toRebuild,
            Instant buildTimestamp) {
        this.modelUUID = modelUUID;
        this.hyperParams = hyperParams;
        this.thresholds = thresholds;
        this.toRebuild = toRebuild;
        this.buildTimestamp = buildTimestamp;
    }
}
