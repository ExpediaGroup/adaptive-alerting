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

import com.expedia.adaptivealerting.core.detector.OutlierResult;
import com.expedia.adaptivealerting.core.detector.OutlierLevel;
import com.expedia.adaptivealerting.tools.pipeline.StreamSubscriber;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import static com.expedia.adaptivealerting.core.detector.OutlierLevel.STRONG;
import static com.expedia.adaptivealerting.core.detector.OutlierLevel.WEAK;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.toSecond;

/**
 * @author Willie Wheeler
 */
public final class OutlierChartStreamSink implements StreamSubscriber<OutlierResult> {
    private final ChartSeries chartSeries;
    
    public OutlierChartStreamSink(ChartSeries chartSeries) {
        notNull(chartSeries, "chartSeries can't be null");
        this.chartSeries = chartSeries;
    }
    
    @Override
    public void next(OutlierResult result) {
        notNull(result, "result can't be null");
        
        final long epochSecond = result.getEpochSecond();
        final double observed = result.getObserved();
        final OutlierLevel level = result.getOutlierLevel();
        final Second second = toSecond(epochSecond);
        
        chartSeries.getObserved().add(second, observed);
        
        addValue(chartSeries.getPredicted(), second, result.getPredicted());
        addValue(chartSeries.getWeakThresholdUpper(), second, result.getWeakThresholdUpper());
        addValue(chartSeries.getWeakThresoldLower(), second, result.getWeakThresholdLower());
        addValue(chartSeries.getStrongThresholdUpper(), second, result.getStrongThresholdUpper());
        addValue(chartSeries.getStrongThresholdLower(), second, result.getStrongThresholdLower());
        
        if (level == STRONG) {
            chartSeries.getStrongOutlier().add(second, observed);
        } else if (level == WEAK) {
            chartSeries.getWeakOutlier().add(second, observed);
        }
    }
    
    private void addValue(TimeSeries timeSeries, Second second, Double value) {
        if (value != null) {
            timeSeries.add(second, value);
        }
    }
}
