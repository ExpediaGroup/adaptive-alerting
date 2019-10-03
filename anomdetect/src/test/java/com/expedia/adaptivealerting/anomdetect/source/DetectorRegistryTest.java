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

import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.cusum.CusumDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx.EdmxDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.individuals.IndividualsDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacyEwmaDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacyHoltWintersDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.LegacyPewmaDetectorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DetectorRegistryTest {
    private DetectorRegistry registryUnderTest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        this.registryUnderTest = new DetectorRegistry();
    }

    @Test
    public void testGetDetectorFactory_constantThreshold() {
        testGetDetectorFactory("constant-threshold", ConstantThresholdDetectorFactory.class);
    }

    @Test
    public void testGetDetectorFactory_cusum() {
        testGetDetectorFactory("cusum", CusumDetectorFactory.class);
    }

    @Test
    public void testGetDetectorFactory_edmx() {
        testGetDetectorFactory("edmx", EdmxDetectorFactory.class);
    }

    @Test
    public void testGetDetectorFactory_individuals() {
        testGetDetectorFactory("individuals", IndividualsDetectorFactory.class);
    }

    @Test
    @Deprecated // underlying factory is deprecated
    public void testGetDetectorFactory_legacyEwma() {
        testGetDetectorFactory("ewma", LegacyEwmaDetectorFactory.class);
    }

    @Test
    @Deprecated // underlying factory is deprecated
    public void testGetDetectorFactory_legacyHoltWinters() {
        testGetDetectorFactory("holt-winters", LegacyHoltWintersDetectorFactory.class);
    }

    @Test
    @Deprecated // underlying factory is deprecated
    public void testGetDetectorFactory_legacyPewma() {
        testGetDetectorFactory("pewma", LegacyPewmaDetectorFactory.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDetectorFactory_nullDocument() {
        registryUnderTest.getDetectorFactory(null);
    }

    @Test(expected = DetectorException.class)
    public void testGetDetectorFactory_illegalType() {
        val document = readDocument("invalid-type");
        registryUnderTest.getDetectorFactory(document);
    }

    private void testGetDetectorFactory(String documentName, Class factoryClass) {
        val document = readDocument(documentName);
        val factory = registryUnderTest.getDetectorFactory(document);
        assertEquals(factoryClass, factory.getClass());
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
