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

import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx.EdmxDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.cusum.CusumDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.individuals.IndividualsDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DetectorFactoryTest {
    private DetectorFactory factoryUnderTest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        this.factoryUnderTest = new DetectorFactory();
    }

    @Test
    public void testBuildDetector_constantThreshold() {
        testBuildDetector("constant-threshold", ConstantThresholdDetector.class);
    }

    @Test
    public void testBuildDetector_cusum() {
        testBuildDetector("cusum", CusumDetector.class);
    }

    @Test
    public void testBuildDetector_edmx() {
        testBuildDetector("edmx", EdmxDetector.class);
    }

    @Test
    public void testBuildDetector_individuals() {
        testBuildDetector("individuals", IndividualsDetector.class);
    }

    @Test
    @Deprecated // underlying factory is deprecated
    public void testBuildDetector_legacyEwma() {
        testBuildDetector("ewma", ForecastingDetector.class);
    }

    @Test
    @Deprecated // underlying factory is deprecated
    public void testGetDetectorFactory_legacyHoltWinters() {
        testBuildDetector("holt-winters", ForecastingDetector.class);
    }

    @Test
    @Deprecated // underlying factory is deprecated
    public void testGetDetectorFactory_legacyPewma() {
        testBuildDetector("pewma", ForecastingDetector.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildDetector_nullDocument() {
        factoryUnderTest.buildDetector(null);
    }

    @Test(expected = DetectorException.class)
    public void testBuildDetector_illegalType() {
        val document = readDocument("invalid-type");
        factoryUnderTest.buildDetector(document);
    }

    private void testBuildDetector(String documentName, Class<?> detectorClass) {
        val document = readDocument(documentName);
        val detector = factoryUnderTest.buildDetector(document);
        assertEquals(detectorClass, detector.getClass());
    }

    private DetectorDocument readDocument(String name) {
        val path = "detector-documents/" + name + ".json";
        try {
            return objectMapper.readValue(ClassLoader.getSystemResourceAsStream(path), DetectorDocument.class);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read " + path, e);
        }
    }
}
