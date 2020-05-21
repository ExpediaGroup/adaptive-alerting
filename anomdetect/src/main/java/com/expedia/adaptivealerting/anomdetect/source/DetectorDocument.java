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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

// TODO Add javax.validation (e.g. @NotNull) [WLW]

/**
 * Detector document, which we maintain in the persistent store.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectorDocument {
    private UUID uuid;
    private String type;
    private boolean enabled;
    private boolean trusted;

    @JsonProperty("detectorConfig")
    private Map<String, Object> config;

    private Meta meta;

    @Data
    public static class Meta {

        private String createdBy;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date dateLastAccessed;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date dateLastUpdated;

        // ================================================================================
        // Deprecated.
        // These fields are not being used and will be removed in upcoming releases
        // ================================================================================

        @Deprecated
        private String updatedBy;

        @Deprecated
        private Date dateUpdated;

        @Deprecated
        private Date dateCreated;
    }

    // ================================================================================
    // Deprecated.
    // dateCreated will be removed in upcoming releases
    // lastUpdateTimestamp is part of meta now and has been renamed to dateLastUpdated
    // createdBy has been moved to meta
    // ================================================================================

    @Deprecated
    private Date dateCreated;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Deprecated
    private Date lastUpdateTimestamp;

    @Deprecated
    private String createdBy;
}
