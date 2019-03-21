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

import com.expedia.adaptivealerting.anomdetect.core.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.lib.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.lib.CusumDetector;
import com.expedia.adaptivealerting.anomdetect.lib.EwmaDetector;
import com.expedia.adaptivealerting.anomdetect.lib.holtwinters.HoltWintersDetector;
import com.expedia.adaptivealerting.anomdetect.lib.IndividualsControlChartDetector;
import com.expedia.adaptivealerting.anomdetect.lib.PewmaDetector;
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
        detectorMap.put("constant-detector", ConstantThresholdDetector.class);
        detectorMap.put("cusum-detector", CusumDetector.class);
        detectorMap.put("ewma-detector", EwmaDetector.class);
        detectorMap.put("holtwinters-detector", HoltWintersDetector.class);
        detectorMap.put("individuals-detector", IndividualsControlChartDetector.class);
        detectorMap.put("pewma-detector", PewmaDetector.class);
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
