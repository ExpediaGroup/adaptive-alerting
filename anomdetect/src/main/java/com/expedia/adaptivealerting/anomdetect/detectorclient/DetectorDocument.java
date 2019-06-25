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
package com.expedia.adaptivealerting.anomdetect.detectorclient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Detector document, which we maintain in the persistent store. This isn't the detector itself, but rather a
 * specification that allows the {@link com.expedia.adaptivealerting.anomdetect.DetectorFactory} to create a
 * detector.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectorDocument {
    private String uuid;
    private Boolean enabled;

    /**
     * @deprecated Detectors no longer have a single type. For example a ForecastingDetector
     *  will use one algo for point forecasts, another algo for interval forecasts, and a
     *  third algo for aggregation.
     */
    private String type;

    /**
     * Detector configuration. Currently
     */
    private Map<String, Object> detectorConfig = new HashMap<>();

    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTimestamp;
}
