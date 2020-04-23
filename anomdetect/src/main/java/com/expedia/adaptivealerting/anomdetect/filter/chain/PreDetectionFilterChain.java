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
package com.expedia.adaptivealerting.anomdetect.filter.chain;

import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.metrics.MetricData;
import lombok.NonNull;

import java.util.List;
import java.util.ListIterator;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class PreDetectionFilterChain {
    @NonNull
    private ListIterator<PreDetectionFilter> filtersIterator;
    @NonNull
    private Detector detector;

    public PreDetectionFilterChain(List<PreDetectionFilter> filters, Detector detector) {
        this.filtersIterator = filters.listIterator();
        this.detector = detector;
    }

    /**
     * Calls the next PreDetectionFilter in the chain, or else the Detector, if this is the final filter in the chain.
     * The Filter may decide to terminate the chain, by not calling this method.
     * In this case, the filter <b>must</b> return a valid DetectorResult (because the Detector will not be invoked).
     *
     * @param metricData the MetricData
     * @return Returns a detector result
     */
    public DetectorResult doFilter(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        if (!filtersIterator.hasNext()) {
            return detector.detect(metricData);
        } else {
            PreDetectionFilter f = filtersIterator.next();
            return f.doFilter(metricData, this);
        }
    }
}
