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
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaDetector;
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
import static org.mockito.Mockito.doReturn;

public class LegacyDetectorFactoryTest {
    public static final String CONSTANT_THRESHOLD = "constant-detector";
    public static final String CUSUM = "cusum-detector";
    public static final String EWMA = "ewma-detector";

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
    public void testCreateLegacyDetector_nullUuid() {
        val params = new HashMap<String, Object>();
        val modelResource = buildModelResource(EWMA, params);
        factoryUnderTest.createLegacyDetector(null, modelResource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLegacyDetector_nullModelResource() {
        factoryUnderTest.createLegacyDetector(UUID.randomUUID(), null);
    }

    @Test
    public void testCreateLegacyDetector_constantThreshold() {
        val params = new HashMap<String, Object>();
        params.put("type", AnomalyType.TWO_TAILED);
        params.put("thresholds", new AnomalyThresholds(100.0, 90.0, 20.0, 10.0));

        val detector = buildDetector(CONSTANT_THRESHOLD, params);

        assertEquals(ConstantThresholdDetector.class, detector.getClass());
    }

    @Test
    public void testCreateLegacyDetector_cusum() {
        val params = new HashMap<String, Object>();
        params.put("type", AnomalyType.LEFT_TAILED);

        val detector = buildDetector(CUSUM, params);

        assertEquals(CusumDetector.class, detector.getClass());
    }

    @Test
    public void testCreateLegacyDetector_ewma() {
        val params = new HashMap<String, Object>();
        val detector = buildDetector(EWMA, params);
        assertEquals(EwmaDetector.class, detector.getClass());
    }

    private void initDependencies() {
        // https://dzone.com/articles/mocking-method-with-wildcard-generic-return-type
        doReturn(ConstantThresholdDetector.class)
                .when(detectorLookup)
                .getDetector(CONSTANT_THRESHOLD);
        doReturn(CusumDetector.class)
                .when(detectorLookup)
                .getDetector(CUSUM);
        doReturn(EwmaDetector.class)
                .when(detectorLookup)
                .getDetector(EWMA);
    }

    private Detector buildDetector(String type, Map<String, Object> params) {
        val modelResource = buildModelResource(type, params);
        return factoryUnderTest.createLegacyDetector(UUID.randomUUID(), modelResource);
    }

    private ModelResource buildModelResource(String type, Map<String, Object> params) {
        val model = new ModelResource();
        model.setDetectorType(new ModelTypeResource(type));
        model.setParams(params);
        model.setDateCreated(new Date());
        return model;
    }
}
