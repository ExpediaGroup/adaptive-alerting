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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.forecast.RmsePointForecastEvaluator;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersSeasonalityType;
import com.expedia.adaptivealerting.samples.util.DetectorUtil;
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

import static com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersSeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersSeasonalityType.MULTIPLICATIVE;
import static com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersTrainingMethod.NONE;
import static com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersTrainingMethod.SIMPLE;
import static com.expedia.adaptivealerting.samples.util.MetricUtil.buildMetricFrameMetricSource;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Data for samples/austourists.csv was taken from austourists-tests-holtwinters-multiplicative/additive.csv files in anomdetect.
 * The first row was converted to the LEVEL, BASE, and M/A_SEASONAL constants below.
 * The "y" values from the remaining rows became the "observed" values in sample/austourists.csv
 */
public class CsvTrafficHoltWintersVariants {
    public static final double ALPHA = 0.441;
    public static final double ALPHA_LOW = 0.004;
    public static final double BETA = 0.030;
    public static final double GAMMA = 0.002;
    public static final double LEVEL = 25.5275345;
    public static final double BASE = 1.06587325;
    public static final double[] M_SEASONAL = new double[]{1.17725873605224, 0.750111453184012, 0.991779758440832, 1.08085005232291};
    public static final double[] A_SEASONAL = new double[]{4.5249785, -6.3790385, -0.209842500000001, 2.0639025};
    public static final HoltWintersSeasonalityType M = MULTIPLICATIVE;
    public static final HoltWintersSeasonalityType A = ADDITIVE;

    public static void main(String[] args) throws Exception {
        MetricFrameMetricSource source = buildMetricFrameMetricSource("samples/austourists.csv", 200L);

        List<JFreeChart> charts = new ArrayList<>();

        charts.add(buildChartWithInitEstimates(source, A, A_SEASONAL, ALPHA, BETA, GAMMA).getChart());
        charts.add(buildChartWithInitTrainings(source, A, ALPHA, BETA, GAMMA).getChart());
        charts.add(buildChartWithInitEstimates(source, M, M_SEASONAL, ALPHA, BETA, GAMMA).getChart());
        charts.add(buildChartWithInitTrainings(source, M, ALPHA, BETA, GAMMA).getChart());

//        charts.add(buildChartWithInitTrainings(source, M, ALPHA,     BETA, GAMMA,      "No Estimates").getChart());
//        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA,     BETA, GAMMA,      "No Estimates").getChart());

        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA_LOW, BETA, GAMMA, "No Estimates", "Low Alpha").getChart());
        charts.add(buildChartWithInitTrainings(source, M, ALPHA_LOW, BETA, GAMMA, "Low Alpha").getChart());

//        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA_LOW, BETA, GAMMA_HIGH, "No Estimates", "High Gamma").getChart());
//        charts.add(buildChartWithoutInitEstimates(source, M, ALPHA_LOW, BETA, GAMMA_HIGH, "No Estimates", "Low Alpha", "High Gamma").getChart());
//        charts.add(buildChartWithInitTrainings(source, M, ALPHA_LOW, BETA, GAMMA_HIGH, "No Estimates", "High Gamma").getChart());

        showChartFrame(createChartFrame("Australian Tourists", charts.toArray(new JFreeChart[]{})));
        source.start();
    }

    private static AnomalyChartSink buildChartWithInitEstimates(
            MetricFrameMetricSource source,
            HoltWintersSeasonalityType seasonalityType,
            double[] seasonal,
            double alpha,
            double beta,
            double gamma,
            String... titleSuffix) {

        val detector = DetectorUtil.buildHoltWintersDetector(seasonalityType, LEVEL, BASE, seasonal, alpha, beta, gamma, NONE);
        val suffix = ArrayUtils.addAll(titleSuffix, "Init Estimates: ", "l=" + LEVEL, "b=" + BASE,
                String.format("s=%s", Arrays.asList(ArrayUtils.toObject(seasonal))));
        return buildChart(source, seasonalityType, detector, alpha, beta, gamma, suffix);
    }

    private static AnomalyChartSink buildChartWithoutInitEstimates(
            MetricFrameMetricSource source,
            HoltWintersSeasonalityType seasonalityType,
            double alpha,
            double beta,
            double gamma,
            String... titleSuffix) {

        val detector = DetectorUtil.buildHoltWintersDetector(seasonalityType, LEVEL, BASE, new double[0], alpha, beta, gamma, NONE);
        val suffix = ArrayUtils.addAll(titleSuffix, "No Init Estimates");
        return buildChart(source, seasonalityType, detector, alpha, beta, gamma, suffix);
    }

    private static AnomalyChartSink buildChartWithInitTrainings(
            MetricFrameMetricSource source,
            HoltWintersSeasonalityType seasonalityType,
            double alpha,
            double beta,
            double gamma,
            String... titleSuffix) {

        val detector = DetectorUtil.buildHoltWintersDetector(seasonalityType, LEVEL, BASE, new double[0], alpha, beta, gamma, SIMPLE);
        val suffix = ArrayUtils.addAll(titleSuffix, "With Training");
        return buildChart(source, seasonalityType, detector, alpha, beta, gamma, suffix);
    }

    private static AnomalyChartSink buildChart(
            MetricFrameMetricSource source,
            HoltWintersSeasonalityType seasonalityType,
            Detector detector,
            double alpha,
            double beta,
            double gammaLow,
            String... titleSuffix) {

        val detectorFilter = new DetectorFilter(detector);
        val evalFilter = new EvaluatorFilter(new RmsePointForecastEvaluator());
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
