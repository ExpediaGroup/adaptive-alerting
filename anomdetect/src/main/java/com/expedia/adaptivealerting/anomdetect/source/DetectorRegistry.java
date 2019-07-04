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

import com.expedia.adaptivealerting.anomdetect.source.factory.ConstantThresholdFactory;
import com.expedia.adaptivealerting.anomdetect.source.factory.CusumFactory;
import com.expedia.adaptivealerting.anomdetect.source.factory.EdmxFactory;
import com.expedia.adaptivealerting.anomdetect.source.factory.IndividualsFactory;
import com.expedia.adaptivealerting.anomdetect.source.factory.LegacyEwmaFactory;
import com.expedia.adaptivealerting.anomdetect.source.factory.LegacyHoltWintersFactory;
import com.expedia.adaptivealerting.anomdetect.source.factory.LegacyPewmaFactory;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class DetectorRegistry {
    private final Map<String, DetectorFactoryBuilder> builders = new HashMap<>();

    public DetectorRegistry() {
        builders.put("constant-detector", document -> new ConstantThresholdFactory(document));
        builders.put("cusum-detector", document -> new CusumFactory(document));
        builders.put("edmx-detector", document -> new EdmxFactory(document));
        builders.put("individuals-detector", document -> new IndividualsFactory(document));

        // Legacy
        builders.put("ewma-detector", document -> new LegacyEwmaFactory(document));
        builders.put("holtwinters-detector", document -> new LegacyHoltWintersFactory(document));
        builders.put("pewma-detector", document -> new LegacyPewmaFactory(document));
    }

    public DetectorFactory getDetectorFactory(DetectorDocument document) {
        notNull(document, "document can't be null");
        val type = document.getType();
        val builder = builders.get(type);
        if (builder == null) {
            throw new DetectorException("Illegal detector type: " + type);
        }
        return builder.build(document);
    }

    private interface DetectorFactoryBuilder {
        DetectorFactory build(DetectorDocument document);
    }
}
