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
package com.expedia.adaptivealerting.tools.pipeline.sink;

import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.adaptivealerting.tools.pipeline.MetricSink;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import static com.expedia.adaptivealerting.core.OutlierLevel.STRONG;
import static com.expedia.adaptivealerting.core.OutlierLevel.WEAK;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.core.util.MetricPointTags.*;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.outlierLevel;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.thresholdValue;

public class ChartSink implements MetricSink {
    private final ChartSeries chartSeries;
    
    // FIXME For now, just impose timestamps.
    // Later update to use actual metric point timestamps.
    private Second currentSecond;
    
    public ChartSink(ChartSeries chartSeries) {
        notNull(chartSeries, "chartSeries can't be null");
        this.chartSeries = chartSeries;
    }
    
    @Override
    public void next(MetricPoint metricPoint) {
        this.currentSecond = (currentSecond == null ? new Second() : (Second) currentSecond.next());
        
        final float observed = metricPoint.value();
        final OutlierLevel level = outlierLevel(metricPoint);
        
        chartSeries.getObserved().add(currentSecond, observed);
        
        addPoint(metricPoint, chartSeries.getMidpoint(), PREDICTION);
        addPoint(metricPoint, chartSeries.getStrongThresholdUpper(), STRONG_THRESHOLD_UPPER);
        addPoint(metricPoint, chartSeries.getStrongThresholdLower(), STRONG_THRESHOLD_LOWER);
        addPoint(metricPoint, chartSeries.getWeakThresholdUpper(), WEAK_THRESHOLD_UPPER);
        addPoint(metricPoint, chartSeries.getWeakThresoldLower(), WEAK_THRESHOLD_LOWER);
        
        if (level == STRONG) {
            chartSeries.getStrongOutlier().add(currentSecond, observed);
        } else if (level == WEAK) {
            chartSeries.getWeakOutlier().add(currentSecond, observed);
        }
        
    }
    
    private void addPoint(MetricPoint metricPoint, TimeSeries timeSeries, String tagName) {
        final Float value = thresholdValue(metricPoint, tagName);
        if (value != null) {
            timeSeries.add(currentSecond, value);
        }
    }
}
