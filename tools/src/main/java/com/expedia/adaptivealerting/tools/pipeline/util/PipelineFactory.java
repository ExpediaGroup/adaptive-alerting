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
package com.expedia.adaptivealerting.tools.pipeline.util;

import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;
import com.expedia.adaptivealerting.tools.visualization.ChartUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

@UtilityClass
public final class PipelineFactory {

    public static AnomalyChartSink createChartSink(String title) {
        notNull(title, "title can't be null");

        val chartSeries = new ChartSeries();
        val chart = ChartUtil.createChart(title, chartSeries);
        return new AnomalyChartSink(chart, chartSeries);
    }
}
