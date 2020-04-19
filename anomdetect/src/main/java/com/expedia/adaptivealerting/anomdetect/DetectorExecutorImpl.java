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

import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.expedia.metrics.MetricData;
import lombok.NonNull;
import lombok.val;

public class DetectorExecutorImpl implements DetectorExecutor {

    @Override
    public DetectorResult doDetection(DetectorContainer detectorContainer, @NonNull MetricData metricData) {
        val detectionFilterChain = new DetectionFilterChain(detectorContainer);

        val request = new DetectorRequest(metricData);
        val response = new DetectorResponse();

        detectionFilterChain.doFilter(request, response);
        return response.getDetectorResult();
    }

}