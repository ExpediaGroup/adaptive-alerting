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
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.Color;
import java.awt.Dimension;

/**
 * Chart utilities.
 *
 * @author Willie Wheeler
 */
public class ChartUtil {
    private static final Color BAND_COLOR = new Color(204, 204, 204, 80);
    private static final Color OBSERVED_COLOR = Color.BLUE;
    
    public static JFreeChart createChart(String title, XYDataset predicted, XYDataset observed) {
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Seconds",
                "Value",
                predicted,
                false,
                false,
                false);
        
        final XYPlot plot = chart.getXYPlot();
        
        final XYDifferenceRenderer bandRenderer = new XYDifferenceRenderer();
        bandRenderer.setPositivePaint(BAND_COLOR);
        bandRenderer.setSeriesPaint(0, BAND_COLOR);
        bandRenderer.setSeriesPaint(1, BAND_COLOR);
    
        final XYLineAndShapeRenderer observedRenderer = new XYLineAndShapeRenderer(true, false);
        observedRenderer.setSeriesPaint(0, OBSERVED_COLOR);
        
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setBackgroundPaint(Color.WHITE);
    
        plot.setDataset(1, observed);
        plot.setRenderer(0, bandRenderer);
        plot.setRenderer(1, observedRenderer);
        
        return chart;
    }
    
    public static ApplicationFrame createChartFrame(JFreeChart chart) {
        ApplicationFrame chartFrame = new ApplicationFrame(chart.getTitle().getText());
        
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setMouseZoomable(true, false);
        
        chartFrame.setContentPane(chartPanel);
        return chartFrame;
    }
    
    public static void showChartFrame(ApplicationFrame chartFrame) {
        chartFrame.pack();
        RefineryUtilities.positionFrameRandomly(chartFrame);
        chartFrame.setVisible(true);
    }
}
