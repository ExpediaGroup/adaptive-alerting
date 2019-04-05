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
package com.expedia.adaptivealerting.anomdetect.comp;

import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelResource;
import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdParams;
import com.expedia.adaptivealerting.anomdetect.detector.CusumParams;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.detector.DetectorParams;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Builds detectors based on detector configurations.
 */
@RequiredArgsConstructor
@Slf4j
public class DetectorFactory {

    @NonNull
    private DetectorLookup detectorLookup;

    // TODO Currently we use a legacy process to find the detector. The legacy process couples point forecast algos
    //  with interval forecast algos. We will decouple these shortly. [WLW]
    @Deprecated
    public Detector createLegacyDetector(UUID uuid, ModelResource modelResource) {
        notNull(uuid, "uuid can't be null");
        notNull(modelResource, "modelResource can't be null");

        val detectorType = modelResource.getDetectorType().getKey();
        val detectorClass = detectorLookup.getDetector(detectorType);
        val detector = ReflectionUtil.newInstance(detectorClass);
        val paramsClass = detector.getParamsClass();
        val params = (DetectorParams) new ObjectMapper().convertValue(modelResource.getParams(), paramsClass);
        val anomalyType = doLegacyGetAnomalyType(params);

        detector.init(uuid, params, anomalyType);
        log.info("Created detector: {}", detector);
        return detector;
    }

    @Deprecated
    private AnomalyType doLegacyGetAnomalyType(DetectorParams params) {
        val paramsClass = params.getClass();

        // TODO For now we simply reproduce current behavior, which is that only certain detectors support tails. Soon
        //  we'll remove these hardcodes since all detectors will support tails.
        if (ConstantThresholdParams.class.equals(paramsClass)) {
            return ((ConstantThresholdParams) params).getType();
        } else if (CusumParams.class.equals(paramsClass)) {
            return ((CusumParams) params).getType();
        } else {
            return AnomalyType.TWO_TAILED;
        }
    }
}
