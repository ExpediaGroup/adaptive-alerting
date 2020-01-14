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
package com.expedia.adaptivealerting.anomdetect.source;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx.EdmxDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.cusum.CusumDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacyEwmaDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacyHoltWintersDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacyPewmaDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacySeasonalNaiveDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.individuals.IndividualsDetectorFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Builds a detector from a detector document.
 */
@Slf4j
public class DetectorFactory {
    private final Map<String, DetectorFactoryProvider> providers = new HashMap<>();

    public DetectorFactory() {
        providers.put("constant-detector", new ConstantThresholdDetectorFactoryProvider());
        providers.put("cusum-detector", new CusumDetectorFactoryProvider());
        providers.put("edmx-detector", new EdmxDetectorFactoryProvider());
        providers.put("individuals-detector", new IndividualsDetectorFactoryProvider());

        // Legacy
        providers.put("ewma-detector", new LegacyEwmaDetectorFactoryProvider());
        providers.put("holtwinters-detector", new LegacyHoltWintersDetectorFactoryProvider());
        providers.put("pewma-detector", new LegacyPewmaDetectorFactoryProvider());
        providers.put("seasonalnaive-detector", new LegacySeasonalNaiveDetectorFactoryProvider());
    }

    public Detector buildDetector(DetectorDocument document) {
        notNull(document, "document can't be null");
        val type = document.getType();
        val factory = providers.get(type);
        if (factory == null) {
            throw new DetectorException("Illegal detector type: " + type);
        }
        return factory.buildDetector(document);
    }
}
