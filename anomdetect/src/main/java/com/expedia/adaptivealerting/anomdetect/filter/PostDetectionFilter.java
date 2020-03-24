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
import com.expedia.adaptivealerting.anomdetect.filter.algo.post.MOfNAggregationFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.post.PassThroughPostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PostDetectionFilterChain;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for filtering result of anomaly detection.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MOfNAggregationFilter.class, name = "mOfNAggregationFilter"),
        @JsonSubTypes.Type(value = PassThroughPostDetectionFilter.class, name = "passThroughPostDetectionFilter"),
})
public interface PostDetectionFilter {
    DetectorResult doFilter(DetectorResult result, PostDetectionFilterChain postDetectionFilterChain);
}
