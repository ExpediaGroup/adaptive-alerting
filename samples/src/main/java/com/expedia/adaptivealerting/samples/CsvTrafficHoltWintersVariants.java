/*
 * Copyright 2019 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersParams;
import com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import org.jfree.chart.JFreeChart;

import java.util.ArrayList;
import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.MULTIPLICATIVE;
import static com.expedia.adaptivealerting.samples.MetricGenerationHelper.buildMetricFrameMetricSource;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * @author Matt Callanan
 */
public class CsvTrafficHoltWintersVariants {
    public static final int AUSTOURISTS_PERIOD = 4;
    public static final double AUSTOURISTS_ALPHA = 0.441;
    public static final double AUSTOURISTS_BETA = 0.030;
    public static final double AUSTOURISTS_GAMMA = 0.002;
    public static final double AUSTOURISTS_LEVEL = 25.5275345;
    public static final double AUSTOURISTS_BASE = 1.06587325;
    public static final double[] AUSTOURISTS_M_SEASONAL = new double[]{1.17725873605224, 0.750111453184012, 0.991779758440832, 1.08085005232291};
    public static final double[] AUSTOURISTS_A_SEASONAL = new double[]{4.5249785, -6.3790385, -0.209842500000001, 2.0639025};
    public static final SeasonalityType M = MULTIPLICATIVE;
    public static final SeasonalityType A = ADDITIVE;

    public static void main(String[] args) throws Exception {
        MetricFrameMetricSource source = buildMetricFrameMetricSource("samples/austourists.csv", 200L);

        List<JFreeChart> charts = new ArrayList<>();

        charts.add(buildChart(source, M, AUSTOURISTS_M_SEASONAL, 2.0, 3.0).getChart());
        charts.add(buildChart(source, A, AUSTOURISTS_A_SEASONAL, 2.0, 3.0).getChart());

        showChartFrame(createChartFrame("Australian Tourists", charts.toArray(new JFreeChart[]{})));
        source.start();
    }

    private static AnomalyChartSink buildChart(MetricFrameMetricSource austouristsSource, SeasonalityType seasonalityType, double[] seasonal,
                                               double weakSigmas, double strongSigmas) {
        final HoltWintersParams params1 = buildHoltWintersParams(seasonalityType, AUSTOURISTS_LEVEL, AUSTOURISTS_BASE, seasonal)
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
        final AnomalyDetectorFilter ad1 = new AnomalyDetectorFilter(new HoltWintersAnomalyDetector(params1));
        final EvaluatorFilter eval1 = new EvaluatorFilter(new RmseEvaluator());
        final AnomalyChartSink chart1 = PipelineFactory.createChartSink(String.format("HoltWinters %s: alpha=%s, beta=%s, gamma=%s", seasonalityType,
                AUSTOURISTS_ALPHA, AUSTOURISTS_BETA, AUSTOURISTS_GAMMA));
        austouristsSource.addSubscriber(ad1);
        ad1.addSubscriber(eval1);
        ad1.addSubscriber(chart1);
        eval1.addSubscriber(chart1);
        return chart1;
    }

    private static HoltWintersParams buildHoltWintersParams(SeasonalityType seasonalityType, double level, double base, double[] seasonal) {
        return new HoltWintersParams()
                .setPeriod(AUSTOURISTS_PERIOD)
                .setAlpha(AUSTOURISTS_ALPHA)
                .setBeta(AUSTOURISTS_BETA)
                .setGamma(AUSTOURISTS_GAMMA)
                .setSeasonalityType(seasonalityType)
                .setWarmUpPeriod(AUSTOURISTS_PERIOD)
                .setInitLevelEstimate(level)
                .setInitBaseEstimate(base)
                .setInitSeasonalEstimates(seasonal);
    }
}
