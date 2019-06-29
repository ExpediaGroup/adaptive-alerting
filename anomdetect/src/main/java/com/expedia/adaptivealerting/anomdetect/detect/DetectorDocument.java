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
package com.expedia.adaptivealerting.anomdetect.detect;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detector document, which we maintain in the persistent store. This isn't the detector itself, but rather a
 * specification for detector builders to consume.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectorDocument {

    /**
     * Detector UUID.
     */
    private UUID uuid;

    /**
     * Detector type.
     */
    private String type;

    /**
     * Detector configuration.
     */
    private Map<String, Object> detectorConfig = new HashMap<>();

    /**
     * Indicates whether the detector is enabled. The Detector Manager ignores disabled detectors.
     */
    private boolean enabled;

    /**
     * Indicates who created this detector.
     */
    private String createdBy;

    /**
     * Indicates when the detector was created.
     */
    private Date dateCreated;

    /**
     * Indicates when the detector was last updated.
     */
    // TODO Use ISO 8601. Also we want the time zone. [WLW]
    // TODO Rename to dateUpdated. [WLW]
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTimestamp;


}
