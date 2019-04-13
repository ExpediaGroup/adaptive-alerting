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

import com.expedia.adaptivealerting.anomdetect.comp.legacy.HoltWintersDetector;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.HoltWintersParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.SeasonalityType;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.JFreeChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersTrainingMethod.SIMPLE;
import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.SeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.SeasonalityType.MULTIPLICATIVE;
import static com.expedia.adaptivealerting.samples.MetricGenerationHelper.buildMetricFrameMetricSource;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Data for samples/austourists.csv was taken from austourists-tests-holtwinters-multiplicative/additive.csv files in anomdetect.
 * The first row was converted to the LEVEL, BASE, and M/A_SEASONAL constants below.
 * The "y" values from the remaining rows became the "observed" values in sample/austourists.csv
 */
public class CsvTrafficHoltWintersVariants {
    public static final int PERIOD = 4;
    public static final double ALPHA = 0.441;
    public static final double ALPHA_LOW = 0.004;
    public static final double BETA = 0.030;
    public static final double GAMMA = 0.002;
    public static final double GAMMA_HIGH = 0.4;
    public static final double LEVEL = 25.5275345;
    public static final double BASE = 1.06587325;
    public static final double[] M_SEASONAL = new double[]{1.17725873605224, 0.750111453184012, 0.991779758440832, 1.08085005232291};
    public static final double[] A_SEASONAL = new double[]{4.5249785, -6.3790385, -0.209842500000001, 2.0639025};
    public static final SeasonalityType M = MULTIPLICATIVE;
    public static final SeasonalityType A = ADDITIVE;
    public static final double WEAK_SIGMAS = 2.0;
    public static final double STRONG_SIGMAS = 3.0;

    public static void main(String[] args) throws Exception {
        MetricFrameMetricSource source = buildMetricFrameMetricSource("samples/austourists.csv", 200L);

        List<JFreeChart> charts = new ArrayList<>();

        charts.add(buildChartWithInitialEstimates(source, A, A_SEASONAL, ALPHA, BETA, GAMMA).getChart());
        charts.add(buildChartWithInitialTrainings(source, A, ALPHA, BETA, GAMMA).getChart());
        charts.add(buildChartWithInitialEstimates(source, M, M_SEASONAL, ALPHA, BETA, GAMMA).getChart());
        charts.add(buildChartWithInitialTrainings(source, M, ALPHA, BETA, GAMMA).getChart());

//        charts.add(buildChartWithInitialTrainings(source, M, ALPHA,     BETA, GAMMA,      "No Estimates").getChart());
//        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA,     BETA, GAMMA,      "No Estimates").getChart());

        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA_LOW, BETA, GAMMA, "No Estimates", "Low Alpha").getChart());
        charts.add(buildChartWithInitialTrainings(source, M, ALPHA_LOW, BETA, GAMMA, "Low Alpha").getChart());

//        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA_LOW, BETA, GAMMA_HIGH, "No Estimates", "High Gamma").getChart());
//        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA_LOW, BETA, GAMMA_HIGH, "No Estimates", "Low Alpha", "High Gamma").getChart());
//        charts.add(buildChartWithInitialTrainings(source, M, ALPHA_LOW, BETA, GAMMA_HIGH, "No Estimates", "High Gamma").getChart());


        showChartFrame(createChartFrame("Australian Tourists", charts.toArray(new JFreeChart[]{})));
        source.start();
    }

    private static AnomalyChartSink buildChartWithInitialEstimates(MetricFrameMetricSource source, SeasonalityType seasonalityType,
                                                                   double[] seasonal, double alpha, double beta, double gamma, String... titleSuffix) {
        final HoltWintersParams params = buildHoltWintersParamsWithInitEstimates(seasonalityType, LEVEL, BASE, seasonal,
                alpha, beta, gamma)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS);
        String[] suffix = ArrayUtils.addAll(titleSuffix, "Init Estimates: ", "l=" + LEVEL, "b=" + BASE, String.format("s=%s", Arrays.asList(ArrayUtils.toObject(seasonal))));
        return buildChart(source, seasonalityType, params, alpha, beta, gamma, suffix);
    }

    private static AnomalyChartSink buildChartWithoutInitEstimates(MetricFrameMetricSource source, SeasonalityType seasonalityType,
                                                                   double alpha, double beta, double gamma, String... titleSuffix) {
        final HoltWintersParams params = buildHoltWintersParams(seasonalityType, alpha, beta, gamma)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS);
        String[] suffix = ArrayUtils.addAll(titleSuffix, "No Init Estimates");
        return buildChart(source, seasonalityType, params, alpha, beta, gamma, titleSuffix);
    }

    private static AnomalyChartSink buildChartWithInitialTrainings(MetricFrameMetricSource source, SeasonalityType seasonalityType,
                                                                   double alpha, double beta, double gamma, String... titleSuffix) {
        final HoltWintersParams params = buildHoltWintersParams(seasonalityType, alpha, beta, gamma)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS)
                .setInitTrainingMethod(SIMPLE);
        String[] suffix = ArrayUtils.addAll(titleSuffix, "With Training");
        return buildChart(source, seasonalityType, params, alpha, beta, gamma, suffix);
    }

    private static HoltWintersParams buildHoltWintersParamsWithInitEstimates(SeasonalityType seasonalityType,
                                                                             double level, double base, double[] seasonal,
                                                                             double alpha, double beta, double gamma) {
        return buildHoltWintersParams(seasonalityType, alpha, beta, gamma)
                .setInitLevelEstimate(level)
                .setInitBaseEstimate(base)
                .setInitSeasonalEstimates(seasonal);
    }

    private static HoltWintersParams buildHoltWintersParams(SeasonalityType seasonalityType, double alpha, double beta, double gamma) {
        return new HoltWintersParams()
                .setFrequency(PERIOD)
                .setAlpha(alpha)
                .setBeta(beta)
                .setGamma(gamma)
                .setSeasonalityType(seasonalityType)
                .setWarmUpPeriod(PERIOD);
    }

    private static AnomalyChartSink buildChart(MetricFrameMetricSource source, SeasonalityType seasonalityType, HoltWintersParams params,
                                               double alpha, double beta, double gammaLow, String... titleSuffix) {

        val detector = new HoltWintersDetector(UUID.randomUUID(), new HoltWintersParams());
        val detectorFilter = new DetectorFilter(detector);
        val evalFilter = new EvaluatorFilter(new RmseEvaluator());
        val chartSink = PipelineFactory.createChartSink(String.format(
                "HoltWinters %s: alpha=%s, beta=%s, gamma=%s %s",
                seasonalityType,
                alpha,
                beta,
                gammaLow,
                Arrays.asList(titleSuffix)));

        source.addSubscriber(detectorFilter);
        detectorFilter.addSubscriber(evalFilter);
        detectorFilter.addSubscriber(chartSink);
        evalFilter.addSubscriber(chartSink);
        return chartSink;
    }
}
