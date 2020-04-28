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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;
import lombok.NonNull;
import lombok.val;

import java.util.ListIterator;

public class DetectionFilterChain {
    @NonNull
    private ListIterator<DetectionFilter> filtersIterator;
    @NonNull
    private Detector detector;

    public DetectionFilterChain(DetectorContainer detectorContainer) {
        this.filtersIterator = detectorContainer.getFilters().listIterator();
        this.detector = detectorContainer.getDetector();
    }

    /**
     * Calls the next PreDetectionFilter in the chain, or else the Detector, if this is the final filter in the chain.
     * The Filter may decide to terminate the chain, by not calling this method.
     * In this case, the filter <b>must</b> return a valid DetectorResult (because the Detector will not be invoked).
     */
    public void doFilter(@NonNull DetectorRequest request, @NonNull DetectorResponse response) {
        if (!filtersIterator.hasNext()) {
            val result = detector.detect(request.getMetricData());
            response.setDetectorResult(result);
        } else {
            DetectionFilter f = filtersIterator.next();
            f.doFilter(request, response, this);
        }
    }
}
