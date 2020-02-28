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
package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteSource;
import com.expedia.adaptivealerting.anomdetect.source.data.initializer.throttlegate.RandomThrottleGate;
import com.expedia.adaptivealerting.anomdetect.source.data.initializer.throttlegate.ThrottleGate;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.source.data.initializer.DataInitializer.BASE_URI;
import static com.expedia.adaptivealerting.anomdetect.source.data.initializer.DataInitializer.THROTTLE_GATE_LIKELIHOOD;

public class DataInitializerFactory {

    public static DataInitializer buildDataInitializer(Config config) {
        val throttleGate = tryCreateThrottleGate(config);
        val baseUri = config.getString(BASE_URI);
        val graphiteClient = new GraphiteClient(baseUri, new HttpClientWrapper(), new ObjectMapper());
        val dataSource = new GraphiteSource(graphiteClient);
        return new DataInitializer(config, throttleGate, dataSource);
    }

    private static ThrottleGate tryCreateThrottleGate(Config config) {
        try {
            val throttleGateLikelihood = config.getDouble(THROTTLE_GATE_LIKELIHOOD);
            return new RandomThrottleGate(throttleGateLikelihood);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Could not load '%s' from config.", THROTTLE_GATE_LIKELIHOOD));
        }
    }
}
