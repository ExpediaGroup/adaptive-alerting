/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.aquila.core.model;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.Mpoint;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class ClassifierTest {
    private double WEAK_THRESHOLD_SIGMAS = 3.0;
    private double STRONG_THRESHOLD_SIGMAS = 4.0;
    private double TOLERANCE = 0.001;
    
    // Class under test
    private Classifier positiveClassifier = createClassifier(Classifier.AnomalyType.POSITIVE);
    private Classifier negativeClassifier = createClassifier(Classifier.AnomalyType.NEGATIVE);
    private Classifier bothClassifier = createClassifier(Classifier.AnomalyType.BOTH);
    
    private Metric metric;
    private long epochSecond;
    private Prediction pred;
    
    @Before
    public void setUp() {
        this.metric = new Metric();
        this.epochSecond = Instant.now().getEpochSecond();
        this.pred = new Prediction(100.0, 10.0);
    }
    
    @Test
    public void testNormal() {
        testNormalMpoint(AnomalyLevel.NORMAL, bothClassifier);
        testNormalMpoint(AnomalyLevel.NORMAL, positiveClassifier);
        testNormalMpoint(AnomalyLevel.NORMAL, negativeClassifier);
    }
    
    @Test
    public void testStrongPositive() {
        testStrongPositiveMpoint(AnomalyLevel.STRONG, bothClassifier);
        testStrongPositiveMpoint(AnomalyLevel.STRONG, positiveClassifier);
        testStrongPositiveMpoint(AnomalyLevel.NORMAL, negativeClassifier);
    }
    
    @Test
    public void testStrongNegative() {
        testStrongNegativeMpoint(AnomalyLevel.STRONG, bothClassifier);
        testStrongNegativeMpoint(AnomalyLevel.NORMAL, positiveClassifier);
        testStrongNegativeMpoint(AnomalyLevel.STRONG, negativeClassifier);
    }
    
    @Test
    public void testWeakPositive() {
        testWeakPositiveMpoint(AnomalyLevel.WEAK, bothClassifier);
        testWeakPositiveMpoint(AnomalyLevel.WEAK, positiveClassifier);
        testWeakPositiveMpoint(AnomalyLevel.NORMAL, negativeClassifier);
    }
    
    @Test
    public void testWeakNegative() {
        testWeakNegativeMpoint(AnomalyLevel.WEAK, bothClassifier);
        testWeakNegativeMpoint(AnomalyLevel.NORMAL, positiveClassifier);
        testWeakNegativeMpoint(AnomalyLevel.WEAK, negativeClassifier);
    }
    
    private Classifier createClassifier(Classifier.AnomalyType anomalyType) {
        return new Classifier(anomalyType, WEAK_THRESHOLD_SIGMAS, STRONG_THRESHOLD_SIGMAS);
    }
    
    private void testNormalMpoint(AnomalyLevel expectedLevel, Classifier classifier) {
        final Mpoint mpoint = mpoint(metric, epochSecond, 110.0);
        final Classification result = classifier.classify(mpoint, pred);
        assertEquals(10.0, result.getAnomalyScore(), TOLERANCE);
        assertEquals(expectedLevel, result.getAnomalyLevel());
    }
    
    private void testStrongPositiveMpoint(AnomalyLevel expectedLevel, Classifier classifier) {
        final Mpoint mpoint = mpoint(metric, epochSecond, 145.0);
        final Classification result = classifier.classify(mpoint, pred);
        assertEquals(45.0, result.getAnomalyScore(), TOLERANCE);
        assertEquals(expectedLevel, result.getAnomalyLevel());
    }
    
    private void testStrongNegativeMpoint(AnomalyLevel expectedLevel, Classifier classifier) {
        final Mpoint mpoint = mpoint(metric, epochSecond, 55.0);
        final Classification result = classifier.classify(mpoint, pred);
        assertEquals(-45.0, result.getAnomalyScore(), TOLERANCE);
        assertEquals(expectedLevel, result.getAnomalyLevel());
    }
    
    private void testWeakPositiveMpoint(AnomalyLevel expectedLevel, Classifier classifier) {
        final Mpoint mpoint = mpoint(metric, epochSecond, 135.0);
        final Classification result = classifier.classify(mpoint, pred);
        assertEquals(35.0, result.getAnomalyScore(), TOLERANCE);
        assertEquals(expectedLevel, result.getAnomalyLevel());
    }
    
    private void testWeakNegativeMpoint(AnomalyLevel expectedLevel, Classifier classifier) {
        final Mpoint mpoint = mpoint(metric, epochSecond, 65.0);
        final Classification result = classifier.classify(mpoint, pred);
        assertEquals(-35.0, result.getAnomalyScore(), TOLERANCE);
        assertEquals(expectedLevel, result.getAnomalyLevel());
    }
    
    private Mpoint mpoint(Metric metric, long epochSecond, double value) {
        final Mpoint mpoint = new Mpoint();
        mpoint.setMetric(metric);
        mpoint.setEpochTimeInSeconds(epochSecond);
        mpoint.setValue((float) value);
        return mpoint;
    }
}
