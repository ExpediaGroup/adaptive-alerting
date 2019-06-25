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
package com.expedia.adaptivealerting.anomdetect.outlier.legacy;

import com.expedia.adaptivealerting.anomdetect.Detector;
import com.expedia.adaptivealerting.anomdetect.detectorclient.DetectorResource;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.outlier.ConstantThresholdOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.CusumOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.ForecastingOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.IndividualsOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.HoltWintersForecaster;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.PewmaPointForecaster;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LegacyDetectorFactoryTest {
    private LegacyDetectorFactory factoryUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.factoryUnderTest = new LegacyDetectorFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_nullUuid() {
        val params = new HashMap<String, Object>();
        val detectorResource = buildLegacyDetectorConfig(LegacyDetectorFactory.EWMA, params);
        factoryUnderTest.createDetector(null, detectorResource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_nullDetectorResource() {
        factoryUnderTest.createDetector(UUID.randomUUID(), null);
    }

    @Test
    public void testCreateDetector_constantThreshold() {
        val params = new HashMap<String, Object>();
        // Disabling this, because the legacy schema doesn't include this field. [WLW]
//        params.put("@type", "constant-threshold");
        params.put("type", AnomalyType.TWO_TAILED);
        params.put("thresholds", new AnomalyThresholds(100.0, 90.0, 20.0, 10.0));
        val detector = buildDetector(LegacyDetectorFactory.CONSTANT_THRESHOLD, params);
        assertEquals(ConstantThresholdOutlierDetector.class, detector.getClass());
    }

    @Test
    public void testCreateDetector_cusum() {
        val params = new HashMap<String, Object>();
        // Disabling this, because the legacy schema doesn't include this field. [WLW]
//        params.put("@type", "cusum");
        params.put("type", AnomalyType.LEFT_TAILED);
        val detector = buildDetector(LegacyDetectorFactory.CUSUM, params);
        assertEquals(CusumOutlierDetector.class, detector.getClass());
    }

    @Test
    public void testCreateDetector_ewma() {
        val params = new HashMap<String, Object>();
        val detector = (ForecastingOutlierDetector) buildDetector(LegacyDetectorFactory.EWMA, params);
        assertTrue(detector.getPointForecaster() instanceof EwmaPointForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test
    public void testCreateDetector_holtWinters() {
        val params = new HashMap<String, Object>();
        params.put("frequency", 24);
        val detector = (ForecastingOutlierDetector) buildDetector(LegacyDetectorFactory.HOLT_WINTERS, params);
        assertTrue(detector.getPointForecaster() instanceof HoltWintersForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test
    public void testCreateDetector_individuals() {
        val params = new HashMap<String, Object>();
        // TODO
        params.put("@type", "individuals");
        val detector = buildDetector(LegacyDetectorFactory.INDIVIDUALS, params);
        assertTrue(detector instanceof IndividualsOutlierDetector);
    }

    @Test
    public void testCreateDetector_pewma() {
        val params = new HashMap<String, Object>();
        val detector = (ForecastingOutlierDetector) buildDetector(LegacyDetectorFactory.PEWMA, params);
        assertTrue(detector.getPointForecaster() instanceof PewmaPointForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_unknown() {
        buildDetector("some-unknown-detector-type", new HashMap<>());
    }

    private Detector buildDetector(String type, Map<String, Object> params) {
        val legacyDetectorConfig = buildLegacyDetectorConfig(type, params);
        return factoryUnderTest.createDetector(UUID.randomUUID(), legacyDetectorConfig);
    }

    private DetectorResource buildLegacyDetectorConfig(String type, Map<String, Object> detectorParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("params", detectorParams);
        return new DetectorResource()
                .setType(type)
                .setCreatedBy("user")
                .setLastUpdateTimestamp(new Date())
                .setDetectorConfig(params);
    }
}
