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

import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import lombok.NonNull;

import java.util.List;

/**
 * Marker interface for detectors that are able to filter detection attempts.
 */
public interface FilterableDetector extends Detector {

    /**
     * Returns list of PreDetectionFilters that will be applied to the metricData prior to Detector.detect() being called.
     */
    @NonNull List<PreDetectionFilter> getPreDetectionFilters();

    /**
     * Returns list of PostDetectionFilters that will be applied to the DetectorResult after Detector.detect() has been called.
     */
    @NonNull List<PostDetectionFilter> getPostDetectionFilters();
}
