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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.evaluator.Evaluator;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_STRONG_SIGMAS;
import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_WEAK_SIGMAS;
import static junit.framework.TestCase.assertEquals;

/**
 * @author Willie Wheeler
 */
public class EwmaAnomalyDetectorTest {
    private static final double WEAK_SIGMAS = DEFAULT_WEAK_SIGMAS;
    private static final double STRONG_SIGMAS = DEFAULT_STRONG_SIGMAS;
    private static final double TOLERANCE = 0.001;
    
    private static List<EwmaTestRow> data;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        readData_calInflow();
    }
    
    @Test
    public void testDefaultConstructor() {
        final EwmaAnomalyDetector detector = new EwmaAnomalyDetector();
        assertEquals(0.15, detector.getAlpha());
        assertEquals(WEAK_SIGMAS, detector.getWeakThresholdSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongThresholdSigmas());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_alphaOutOfRange() {
        new EwmaAnomalyDetector(2.0, 100.0, 150.0, 0.0);
    }
    
    @Test
    public void testEvaluate() {
        
        // Params
        final double alpha = 0.05;
        
        final ListIterator<EwmaTestRow> testRows = data.listIterator();
        final EwmaTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();
        final EwmaAnomalyDetector detector =
                new EwmaAnomalyDetector(alpha, WEAK_SIGMAS, STRONG_SIGMAS, observed0);
    
        // Params
        assertEquals(alpha, detector.getAlpha());
        assertEquals(WEAK_SIGMAS, detector.getWeakThresholdSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongThresholdSigmas());
        
        // Seed observation
        assertEquals(observed0, detector.getMean());
        assertEquals(0.0, detector.getVariance());
        
        while (testRows.hasNext()) {
            final EwmaTestRow testRow = testRows.next();
            final int observed = testRow.getObserved();
            
            // This detector doesn't currently do anything with the instant, so we can just pass now().
            // This may change in the future.
            detector.classify(MetricUtil.metricPoint(Instant.now().getEpochSecond(), observed));
            
            assertApproxEqual(testRow.getKnownMean(), testRow.getMean());
            assertApproxEqual(testRow.getMean(), detector.getMean());
            assertApproxEqual(testRow.getVar(), detector.getVariance());
            // TODO Assert AnomalyLevel
        }
    }

    @Test
    public void testHpo() {
        Double bestAlpha = null;
        Double lowWeakAlpha = null;
        Double lowStrongAlpha = null;
        Double lowAnonAlpha = null;
        long minWeak = Long.MAX_VALUE;
        long minStrong = Long.MAX_VALUE;
        long minAnon = Long.MAX_VALUE;
        double bestRmse = Double.MAX_VALUE;
        int numAlphas = 1000;
        DoubleStream alphas = IntStream.rangeClosed(0, numAlphas).asDoubleStream().map(i -> i/numAlphas);
        for (double alpha : alphas.toArray()) {
            Evaluator evaluator = new RmseEvaluator();
            AnomalyDetector detector = new EwmaAnomalyDetector(
                    alpha, DEFAULT_WEAK_SIGMAS, DEFAULT_STRONG_SIGMAS, data.get(0).getObserved());
            long epochSecond = 0;
            long weak = 0;
            long strong = 0;
            for (EwmaTestRow row : data) {
                Mpoint mpoint = MetricUtil.toMpoint(MetricUtil.metricPoint(epochSecond, row.getObserved()));
                AnomalyResult result = detector.classify(
                        new MappedMpoint(mpoint, UUID.randomUUID(), "")).getAnomalyResult();
                evaluator.update(result.getObserved(), result.getPredicted());
                epochSecond++;
                if (AnomalyLevel.WEAK == result.getAnomalyLevel()) {
                    weak++;
                }
                if (AnomalyLevel.STRONG == result.getAnomalyLevel()) {
                    strong++;
                }
            }
            double rmse = evaluator.evaluate().getEvaluatorScore();
            if (rmse < bestRmse) {
                bestAlpha = alpha;
                bestRmse = rmse;
            }
            if (weak < minWeak) {
                lowWeakAlpha = alpha;
                minWeak = weak;
            }
            if (strong < minStrong) {
                lowStrongAlpha = alpha;
                minStrong = strong;
            }
            if ((weak + strong) < minAnon) {
                lowAnonAlpha = alpha;
                minAnon = weak + strong;
            }
            System.out.printf("Alpha=%s, RMSE=%s, Weak=%s, Strong=%s\n", alpha, rmse, weak, strong);
        }
        System.out.printf("Best Alpha=%s, RMSE=%s\n", bestAlpha, bestRmse);
        System.out.printf("Low Weak Alpha=%s, Weak=%s\n", lowWeakAlpha, minWeak);
        System.out.printf("Low Strong Alpha=%s, Strong=%s\n", lowStrongAlpha, minStrong);
        System.out.printf("Low Anon Alpha=%s, Anon=%s\n", lowAnonAlpha, minAnon);
    }
    
    private static void readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-ewma.csv");
        data = new CsvToBeanBuilder<EwmaTestRow>(new InputStreamReader(is))
                .withType(EwmaTestRow.class)
                .build()
                .parse();

    }
    
    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }
}
