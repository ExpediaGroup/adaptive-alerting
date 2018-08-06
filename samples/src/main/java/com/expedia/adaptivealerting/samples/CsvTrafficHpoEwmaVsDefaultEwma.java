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
package com.expedia.adaptivealerting.samples;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.evaluator.Evaluator;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.core.io.MetricFrameLoader;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import org.jfree.chart.JFreeChart;

import java.io.InputStream;
import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_STRONG_SIGMAS;
import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_WEAK_SIGMAS;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * @author Willie Wheeler
 */
public final class CsvTrafficHpoEwmaVsDefaultEwma {

    public static void main(String[] args) throws Exception {
        final InputStream is = ClassLoader.getSystemResourceAsStream(
//                "samples/cal-inflow.csv"
//                "samples/grafana_data_export.csv"
                "samples/latency_spike.csv"
        );
        final MetricFrame frame = MetricFrameLoader.loadCsv(new Metric(), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 2L);

        //return new double[] {bestAlpha, lowWeakAlpha, lowStrongAlpha, lowAnonAlpha};
        final double[] alphas = calculateBestAlpha(frame, 100);
        float initialValue = frame.getMetricPoint(0).getValue();

        showChartFrame(createChartFrame(
                "Cal Inflow",
                makeSink(alphas[0], "low RMSE", initialValue, source),
//                makeSink(alphas[1], "low Weak", initialValue, source),
                makeSink(alphas[2], "low Strong", initialValue, source),
                makeSink(alphas[3], "low Anomaly ", initialValue, source)
        ));
        source.start();
    }

    private static JFreeChart makeSink(
            double alpha, String prefix, double initialValue, MetricFrameMetricSource source) {
        final AnomalyDetectorFilter ewmaAD = new AnomalyDetectorFilter(new EwmaAnomalyDetector(
                alpha, DEFAULT_WEAK_SIGMAS, DEFAULT_STRONG_SIGMAS, initialValue));
        final EvaluatorFilter ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        final AnomalyChartSink ewmaChart = PipelineFactory.createChartSink(prefix + " α=" + alpha);
        source.addSubscriber(ewmaAD);
        ewmaAD.addSubscriber(ewmaEval);
        ewmaAD.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);
        return ewmaChart.getChart();
    }


    private static double[] calculateBestAlpha(MetricFrame frame, int numAlphas) {
        float initialValue = frame.getMetricPoint(0).getValue();

        /*Double bestAlpha = 0.0;
        double bestRmse = Double.MAX_VALUE;
        int numAlphas = 20;//100;
        DoubleStream alphas = IntStream.rangeClosed(0, numAlphas).asDoubleStream().map(i -> i/numAlphas);
        for (double alpha : alphas.toArray()) {
            Evaluator evaluator = new RmseEvaluator();
            AnomalyDetector detector = new EwmaAnomalyDetector(
                    alpha, DEFAULT_WEAK_SIGMAS, DEFAULT_STRONG_SIGMAS, initialValue);
            for (Mpoint mpoint : frame.getMpoints()) {
                AnomalyResult result = detector.classify(
                        new MappedMpoint(mpoint, UUID.randomUUID(), "")).getAnomalyResult();
                evaluator.update(result.getObserved(), result.getPredicted());
            }
            double rmse = evaluator.evaluate().getEvaluatorScore();
            if (rmse < bestRmse) {
                bestAlpha = alpha;
                bestRmse = rmse;
            }
            System.out.printf("Alpha=%s, RMSE=%s\n", alpha, rmse);
        }
        System.out.printf("Best Alpha=%s, RMSE=%s\n", bestAlpha, bestRmse);
        return bestAlpha;*/
        Double bestAlpha = null;
        Double lowWeakAlpha = null;
        Double lowStrongAlpha = null;
        Double lowAnomAlpha = null;
        long minWeak = Long.MAX_VALUE;
        long minStrong = Long.MAX_VALUE;
        long minAnom = Long.MAX_VALUE;
        double bestRmse = Double.MAX_VALUE;

        DoubleStream alphas = IntStream.rangeClosed(0, numAlphas).asDoubleStream().map(i -> i/numAlphas);
        for (double alpha : alphas.toArray()) {
            Evaluator evaluator = new RmseEvaluator();
            AnomalyDetector detector = new EwmaAnomalyDetector(
                    alpha, DEFAULT_WEAK_SIGMAS, DEFAULT_STRONG_SIGMAS, initialValue);
            long weak = 0;
            long strong = 0;
            for (Mpoint mpoint : frame.getMpoints()) {
                AnomalyResult result = detector.classify(
                        new MappedMpoint(mpoint, UUID.randomUUID(), "")).getAnomalyResult();
                evaluator.update(result.getObserved(), result.getPredicted());
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
            if ((weak + strong) < minAnom) {
                lowAnomAlpha = alpha;
                minAnom = weak + strong;
            }
            System.out.printf("Alpha=%s, RMSE=%s, Weak=%s, Strong=%s\n", alpha, rmse, weak, strong);
        }
        System.out.printf("Best Alpha=%s, RMSE=%s\n", bestAlpha, bestRmse);
        System.out.printf("Low Weak Alpha=%s, Weak=%s\n", lowWeakAlpha, minWeak);
        System.out.printf("Low Strong Alpha=%s, Strong=%s\n", lowStrongAlpha, minStrong);
        System.out.printf("Low Anon Alpha=%s, Anon=%s\n", lowAnomAlpha, minAnom);
        return new double[] {bestAlpha, lowWeakAlpha, lowStrongAlpha, lowAnomAlpha};
    }
}
