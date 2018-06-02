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
package com.expedia.adaptivealerting.tools.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.Dimension;

/**
 * Chart utilities.
 *
 * @author Willie Wheeler
 */
public class ChartUtil {
    
    public static ApplicationFrame createChartFrame(String title, TimeSeries timeSeries) {
        ApplicationFrame chartFrame = new ApplicationFrame("White Noise");
        final XYDataset dataset = new TimeSeriesCollection(timeSeries);
        final JFreeChart chart = createChart(title, dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 480));
        chartPanel.setMouseZoomable(true, false);
        chartFrame.setContentPane(chartPanel);
        return chartFrame;
    }
    
    public static void showChartFrame(ApplicationFrame chartFrame) {
        chartFrame.pack();
        RefineryUtilities.positionFrameRandomly(chartFrame);
        chartFrame.setVisible(true);
    }
    
    private static JFreeChart createChart(String title, final XYDataset dataset) {
        return ChartFactory.createTimeSeriesChart(
                title,
                "Seconds",
                "Value",
                dataset,
                false,
                false,
                false);
    }
}
