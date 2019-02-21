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
package com.expedia.adaptivealerting.tools.pipeline.sink;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.adaptivealerting.core.evaluator.ModelEvaluation;
import com.expedia.adaptivealerting.tools.pipeline.util.AnomalyResultSubscriber;
import com.expedia.adaptivealerting.tools.pipeline.util.ModelEvaluationSubscriber;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import java.text.DecimalFormat;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.STRONG;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.WEAK;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.toSecond;

public final class AnomalyChartSink implements AnomalyResultSubscriber, ModelEvaluationSubscriber {
    
    @Getter
    private final JFreeChart chart;
    private final ChartSeries chartSeries;
    private final String baseTitle;
    private final DecimalFormat format = new DecimalFormat(".###");
    
    public AnomalyChartSink(JFreeChart chart, ChartSeries chartSeries) {
        notNull(chart, "chart can't be null");
        notNull(chartSeries, "chartSeries can't be null");
        
        this.chart = chart;
        this.chartSeries = chartSeries;
        this.baseTitle = chart.getTitle().getText();
    }
    
    @Override
    public void next(AnomalyResult result) {
        notNull(result, "result can't be null");
        
        final MetricData metricData = result.getMetricData();
        final long epochSecond = metricData.getTimestamp();
        final double observed = metricData.getValue();
        final AnomalyLevel level = result.getAnomalyLevel();
        final AnomalyThresholds thresholds = result.getThresholds();
    
        final Second second = toSecond(epochSecond);
        chartSeries.getObserved().add(second, observed);
        
        addValue(chartSeries.getPredicted(), second, result.getPredicted());
        
        // FIXME Hacky check
        if (thresholds != null) {
            addValue(chartSeries.getWeakThresholdUpper(), second, thresholds.getUpperWeak());
            addValue(chartSeries.getWeakThresoldLower(), second, thresholds.getLowerWeak());
            addValue(chartSeries.getStrongThresholdUpper(), second, thresholds.getUpperStrong());
            addValue(chartSeries.getStrongThresholdLower(), second, thresholds.getLowerStrong());
        }
        
        if (level == STRONG) {
            chartSeries.getStrongOutlier().add(second, observed);
        } else if (level == WEAK) {
            chartSeries.getWeakOutlier().add(second, observed);
        }
    }
    
    @Override
    public void next(ModelEvaluation evaluation) {
        notNull(evaluation, "evaluation can't be null");
        
        final String title = new StringBuilder(baseTitle)
                .append(" (")
                .append(evaluation.getEvaluatorMethod())
                .append("=")
                .append(format.format(evaluation.getEvaluatorScore()))
                .append(")")
                .toString();
        chart.setTitle(title);
    }
    
    private void addValue(TimeSeries timeSeries, Second second, Double value) {
        if (value != null) {
            timeSeries.add(second, value);
        }
    }
}
