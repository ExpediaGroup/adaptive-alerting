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

import com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.cusum.CusumAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.individuals.IndividualsControlChartAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Detector lookup table.
 */
public class DetectorLookup {
    private final Map<String, Class<? extends AnomalyDetector>> detectorMap = new HashMap<>();

    public DetectorLookup() {
        detectorMap.put("constant-detector", ConstantThresholdAnomalyDetector.class);
        detectorMap.put("cusum-detector", CusumAnomalyDetector.class);
        detectorMap.put("ewma-detector", EwmaAnomalyDetector.class);
        detectorMap.put("holtwinters-detector", HoltWintersAnomalyDetector.class);
        detectorMap.put("individuals-detector", IndividualsControlChartAnomalyDetector.class);
        detectorMap.put("pewma-detector", PewmaAnomalyDetector.class);
    }

    public Set<String> getDetectorTypes() {
        return detectorMap.keySet();
    }

    public Class<? extends AnomalyDetector> getDetector(String key) {
        notNull(key, "key can't be null");
        val detectorClass = detectorMap.get(key);

        if (detectorClass == null) {
            throw new RuntimeException("No such detector: " + key);
        }

        return detectorClass;
    }
}
