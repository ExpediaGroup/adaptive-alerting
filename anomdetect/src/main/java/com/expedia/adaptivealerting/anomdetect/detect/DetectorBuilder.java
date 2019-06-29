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

import com.expedia.adaptivealerting.anomdetect.detect.algo.ConstantThresholdOutlierDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.CusumOutlierDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.EdmxBreakoutDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.IndividualsOutlierDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.LegacyEwmaOutlierDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.LegacyHoltWintersOutlierDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.LegacyPewmaOutlierDetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.algo.LegacyRcfOutlierDetectorBuilder;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

public class DetectorBuilder {
    private static final Map<String, TypedDetectorBuilder> BUILDERS = new HashMap<>();

    static {
        BUILDERS.put("constant-detector", new ConstantThresholdOutlierDetectorBuilder());
        BUILDERS.put("cusum-detector", new CusumOutlierDetectorBuilder());
        BUILDERS.put("edmx-detector", new EdmxBreakoutDetectorBuilder());
        BUILDERS.put("individuals-detector", new IndividualsOutlierDetectorBuilder());

        // TODO Replace these with a forecasting detector that knows how to build child forecasters.
        //  Originally we treated these as detectors, but now we treat them as point forecasters. [WLW]
        BUILDERS.put("ewma-detector", new LegacyEwmaOutlierDetectorBuilder());
        BUILDERS.put("holtwinters-detector", new LegacyHoltWintersOutlierDetectorBuilder());
        BUILDERS.put("pewma-detector", new LegacyPewmaOutlierDetectorBuilder());

        // TODO Remove this, as RCF is an external detector type.
        BUILDERS.put("rcf-detector", new LegacyRcfOutlierDetectorBuilder());
    }

    public Detector build(DetectorDocument document) {
        val type = document.getType();
        val builder = BUILDERS.get(type);
        if (builder == null) {
            throw new IllegalArgumentException("Illegal detector type: " + type);
        }
        return builder.build(document);
    }
}
