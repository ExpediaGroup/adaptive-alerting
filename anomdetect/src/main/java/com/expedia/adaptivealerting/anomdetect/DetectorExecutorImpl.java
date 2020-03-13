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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.FilterableDetector;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PostDetectionFilterChain;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PreDetectionFilterChain;
import com.expedia.metrics.MetricData;
import lombok.NonNull;
import lombok.val;

import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class DetectorExecutorImpl implements DetectorExecutor {

    public DetectorResult doDetectionWithOptionalFiltering(@NonNull Detector detector, @NonNull MetricData metricData) {
        if (detector instanceof FilterableDetector) {
            return detectWithFiltering((FilterableDetector) detector, metricData);
        } else {
            return detector.detect(metricData);
        }
    }

    private DetectorResult detectWithFiltering(@NonNull FilterableDetector filterableDetector, @NonNull MetricData metricData) {
        val preDetectionFilterChain = new PreDetectionFilterChain(getPreDetectionFilters(filterableDetector), filterableDetector);
        val postDetectionFilterChain = new PostDetectionFilterChain(getPostDetectionFilters(filterableDetector));
        val result = preDetectionFilterChain.doFilter(metricData);
        return postDetectionFilterChain.doFilter(result);
    }

    private List<PreDetectionFilter> getPreDetectionFilters(@NonNull FilterableDetector filterableDetector) {
        List<PreDetectionFilter> preDetectionFilters = filterableDetector.getPreDetectionFilters();
        notNull(preDetectionFilters, "FilterableDetector must have non-null list of PreDetectionFilters");
        return preDetectionFilters;
    }

    private List<PostDetectionFilter> getPostDetectionFilters(@NonNull FilterableDetector filterableDetector) {
        List<PostDetectionFilter> postDetectionFilters = filterableDetector.getPostDetectionFilters();
        notNull(postDetectionFilters, "FilterableDetector must have non-null list of PostDetectionFilters");
        return postDetectionFilters;
    }

}