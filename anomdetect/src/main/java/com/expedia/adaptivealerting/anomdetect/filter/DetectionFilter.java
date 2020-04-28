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
package com.expedia.adaptivealerting.anomdetect.filter;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.filter.algo.MOfNAggregationFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.HourOfDayDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.PassThroughDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for filtering anomaly detection before it occurs.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HourOfDayDetectionFilter.class, name = "hourOfDayPreDetectionFilter"),
        @JsonSubTypes.Type(value = MOfNAggregationFilter.class, name = "mOfNAggregationFilter"),
        @JsonSubTypes.Type(value = PassThroughDetectionFilter.class, name = "passThroughDetectionFilter"),
})
public interface DetectionFilter {
    void doFilter(DetectorRequest detectorRequest, DetectorResponse detectorResponse, DetectionFilterChain chain);
}
