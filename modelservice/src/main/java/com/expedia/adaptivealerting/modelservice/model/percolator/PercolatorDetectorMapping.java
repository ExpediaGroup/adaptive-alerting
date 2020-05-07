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
package com.expedia.adaptivealerting.modelservice.model.percolator;

import com.expedia.adaptivealerting.modelservice.model.mapping.DetectorConsumerInfo;
import com.expedia.adaptivealerting.modelservice.model.mapping.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PercolatorDetectorMapping {
    // Prefixing variable names with 'aa_' to reserve these fields to be used in ES mappings.
    public static final String AA_PREFIX = "aa_";
    public static final String USER_KEYWORD = AA_PREFIX + "user";
    public static final String USER_ID_KEYWORD = "id";
    public static final String DETECTOR_KEYWORD = AA_PREFIX + "detector";
    public static final String DETECTOR_ID_KEYWORD = "uuid";
    public static final String QUERY_KEYWORD = AA_PREFIX + "query";
    public static final String ENABLED = AA_PREFIX + "enabled";
    public static final String LAST_MOD_TIME_KEYWORD = AA_PREFIX + "lastModifiedTime";
    public static final String CREATE_TIME_KEYWORD = AA_PREFIX + "createdTime";

    public static final String DETECTOR_MOD_TIME = "lastUpdateTimestamp";
    public static final String DETECTOR_CREATED_TIME = "dateCreated";
    public static final String DETECTOR_TYPE = "type";
    public static final String DETECTOR_CREATED_BY = "createdBy";
    public static final String DETECTOR_CONFIG = "detectorConfig";

    @JsonProperty(USER_KEYWORD)
    private User user;
    @JsonProperty(DETECTOR_KEYWORD)
    private DetectorConsumerInfo detectorConsumerInfo;
    @JsonProperty(QUERY_KEYWORD)
    private Query query;
    @JsonProperty(ENABLED)
    private boolean enabled;
    @JsonProperty(LAST_MOD_TIME_KEYWORD)
    private long lastModifiedTimeInMillis;
    @JsonProperty(CREATE_TIME_KEYWORD)
    private long createdTimeInMillis;
}
