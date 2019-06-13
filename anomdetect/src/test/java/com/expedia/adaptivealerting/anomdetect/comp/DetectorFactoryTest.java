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

import com.expedia.adaptivealerting.anomdetect.detector.DetectorConfig;
import com.expedia.adaptivealerting.anomdetect.detector.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.AdditiveIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.detector.AnomalyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public final class DetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DetectorFactory factory;

    @Before
    public void setUp() {
        this.factory = new DetectorFactory();
    }

    @Test
    public void testCreateDetector_constantThreshold() {

    }

    @Test
    public void testCreateDetector_ewmaAdditive() {
        val uuid = UUID.randomUUID();
        val config = readConfig("forecasting-ewma-additive-001");
        val detector = factory.createDetector(uuid, config);

        assertEquals(uuid, detector.getUuid());
        assertTrue(detector instanceof ForecastingDetector);
        val forecastingDetector = (ForecastingDetector) detector;

        val pointForecaster = forecastingDetector.getPointForecaster();
        assertTrue(pointForecaster instanceof EwmaPointForecaster);
        val ewmaPointForecaster = (EwmaPointForecaster) pointForecaster;
        val ewmaParams = ewmaPointForecaster.getParams();
        assertEquals(0.15, ewmaParams.getAlpha(), TOLERANCE);
        assertEquals(100.0, ewmaParams.getInitMeanEstimate(), TOLERANCE);

        val intervalForecaster = forecastingDetector.getIntervalForecaster();
        assertTrue(intervalForecaster instanceof AdditiveIntervalForecaster);
        val additiveIntervalForecaster = (AdditiveIntervalForecaster) intervalForecaster;
        val additiveParams = additiveIntervalForecaster.getParams();
        assertEquals(10.0, additiveParams.getWeakValue(), TOLERANCE);
        assertEquals(20.0, additiveParams.getStrongValue(), TOLERANCE);

        assertEquals(AnomalyType.RIGHT_TAILED, forecastingDetector.getAnomalyType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_nullUuid() {
        val config = readConfig("forecasting-ewma-additive-001");
        factory.createDetector(null, config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_nullConfig() {
        new DetectorFactory().createDetector(UUID.randomUUID(), null);
    }

    @SneakyThrows
    private DetectorConfig readConfig(String name) {
        val path = "config/" + name + ".json";
        return objectMapper.readValue(ClassLoader.getSystemResourceAsStream(path), DetectorConfig.class);
    }
}
