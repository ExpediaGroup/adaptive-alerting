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
package com.expedia.adaptivealerting.anomdetect.filter.algo;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.expedia.metrics.MetricData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * "PreDetectionFilter" that simply passes along the {@link MetricData}.
 */
@Data
@NoArgsConstructor
@Setter(AccessLevel.NONE)
public class PassThroughDetectionFilter implements DetectionFilter {

    @Override
    public void doFilter(@NonNull DetectorRequest detectorRequest,
                         @NonNull DetectorResponse detectorResponse,
                         @NonNull DetectionFilterChain chain) {
        chain.doFilter(detectorRequest, detectorResponse);
    }
}
