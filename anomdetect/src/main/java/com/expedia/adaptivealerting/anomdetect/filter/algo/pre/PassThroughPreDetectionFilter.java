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
package com.expedia.adaptivealerting.anomdetect.filter.algo.pre;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PreDetectionFilterChain;
import com.expedia.metrics.MetricData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * "PreDetectionFilter" that simply passes along the {@link MetricData}.
 */
@Data
@NoArgsConstructor
@Setter(AccessLevel.NONE)
public class PassThroughPreDetectionFilter implements PreDetectionFilter {

    @Override
    public DetectorResult doFilter(MetricData metricData, PreDetectionFilterChain chain) {
        notNull(metricData, "metricData can't be null");
        notNull(chain, "chain can't be null");
        return chain.doFilter(metricData);
    }
}
