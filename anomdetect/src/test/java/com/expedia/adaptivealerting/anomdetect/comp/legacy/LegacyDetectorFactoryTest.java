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
package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelResource;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelTypeResource;
import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detector.CusumDetector;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.forecast.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PewmaPointForecaster;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

public class LegacyDetectorFactoryTest {
    private LegacyDetectorFactory factoryUnderTest;

    @Mock
    private DetectorLookup detectorLookup;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initDependencies();
        this.factoryUnderTest = new LegacyDetectorFactory(detectorLookup);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_nullUuid() {
        val params = new HashMap<String, Object>();
        val modelResource = buildModelResource(LegacyDetectorTypes.EWMA, params);
        factoryUnderTest.createDetector(null, modelResource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_nullModelResource() {
        factoryUnderTest.createDetector(UUID.randomUUID(), null);
    }

    @Test
    public void testCreateDetector_constantThreshold() {
        val params = new HashMap<String, Object>();
        params.put("type", AnomalyType.TWO_TAILED);
        params.put("thresholds", new AnomalyThresholds(100.0, 90.0, 20.0, 10.0));
        val detector = buildDetector(LegacyDetectorTypes.CONSTANT_THRESHOLD, params);
        assertEquals(ConstantThresholdDetector.class, detector.getClass());
    }

    @Test
    public void testCreateDetector_cusum() {
        val params = new HashMap<String, Object>();
        params.put("type", AnomalyType.LEFT_TAILED);
        val detector = buildDetector(LegacyDetectorTypes.CUSUM, params);
        assertEquals(CusumDetector.class, detector.getClass());
    }

    @Test
    public void testCreateDetector_ewma() {
        val params = new HashMap<String, Object>();
        val detector = (ForecastingDetector) buildDetector(LegacyDetectorTypes.EWMA, params);
        assertTrue(detector.getPointForecaster() instanceof EwmaPointForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test
    public void testCreateEwmaDetector_noArg() {
        val detector = (ForecastingDetector) factoryUnderTest.createEwmaDetector();
        assertTrue(detector.getPointForecaster() instanceof EwmaPointForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test
    public void testCreateDetector_pewma() {
        val params = new HashMap<String, Object>();
        val detector = (ForecastingDetector) buildDetector(LegacyDetectorTypes.PEWMA, params);
        assertTrue(detector.getPointForecaster() instanceof PewmaPointForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test
    public void testCreatePewmaDetector_noArg() {
        val detector = (ForecastingDetector) factoryUnderTest.createPewmaDetector();
        assertTrue(detector.getPointForecaster() instanceof PewmaPointForecaster);
        assertTrue(detector.getIntervalForecaster() instanceof ExponentialWelfordIntervalForecaster);
    }

    @Test
    public void testCreateDetector_individuals() {
        val params = new HashMap<String, Object>();
        val detector = buildDetector(LegacyDetectorTypes.INDIVIDUALS, params);
        assertTrue(detector instanceof IndividualsControlChartDetector);
    }

    private void initDependencies() {
        // https://dzone.com/articles/mocking-method-with-wildcard-generic-return-type
        doReturn(ConstantThresholdDetector.class)
                .when(detectorLookup)
                .getDetector(LegacyDetectorTypes.CONSTANT_THRESHOLD);
        doReturn(CusumDetector.class)
                .when(detectorLookup)
                .getDetector(LegacyDetectorTypes.CUSUM);
        doReturn(IndividualsControlChartDetector.class)
                .when(detectorLookup)
                .getDetector(LegacyDetectorTypes.INDIVIDUALS);
    }

    private Detector buildDetector(String type, Map<String, Object> params) {
        val modelResource = buildModelResource(type, params);
        return factoryUnderTest.createDetector(UUID.randomUUID(), modelResource);
    }

    private ModelResource buildModelResource(String type, Map<String, Object> params) {
        val model = new ModelResource();
        model.setDetectorType(new ModelTypeResource(type));
        model.setParams(params);
        model.setDateCreated(new Date());
        return model;
    }
}
