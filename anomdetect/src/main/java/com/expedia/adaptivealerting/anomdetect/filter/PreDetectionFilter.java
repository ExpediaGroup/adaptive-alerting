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

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.HourOfDayDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.PassThroughPreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PreDetectionFilterChain;
import com.expedia.metrics.MetricData;
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
        @JsonSubTypes.Type(value = PassThroughPreDetectionFilter.class, name = "passThroughPreDetectionFilter"),
})
public interface PreDetectionFilter {

    DetectorResult doFilter(MetricData metricData, PreDetectionFilterChain chain);
}
