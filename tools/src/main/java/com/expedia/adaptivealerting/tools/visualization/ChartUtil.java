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
package com.expedia.adaptivealerting.tools.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.Instant;
import java.util.Date;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Chart utilities.
 */
public class ChartUtil {
    private static final Color STRONG_OUTLIER_COLOR = Color.RED;
    private static final Color WEAK_OUTLIER_COLOR = new Color(255, 194, 0);
    private static final Color OBSERVED_COLOR = Color.BLUE;
    private static final Color MIDPOINT_COLOR = Color.DARK_GRAY;
    private static final Color THRESHOLD_COLOR = new Color(204, 204, 204, 80);
    
    public static JFreeChart createChart(String title, ChartSeries chartSeries) {
        notNull(title, "title can't be null");
        notNull(chartSeries, "chartSeries can't be null");
        
        final TimeSeriesCollection strongOutlier = new TimeSeriesCollection(chartSeries.getStrongOutlier());
        final TimeSeriesCollection weakOutlier = new TimeSeriesCollection(chartSeries.getWeakOutlier());
        
        final TimeSeriesCollection observed = new TimeSeriesCollection(chartSeries.getObserved());
        final TimeSeriesCollection midpoint = new TimeSeriesCollection(chartSeries.getPredicted());
    
        final TimeSeriesCollection weakThreshold = new TimeSeriesCollection();
        weakThreshold.addSeries(chartSeries.getWeakThresholdUpper());
        weakThreshold.addSeries(chartSeries.getWeakThresoldLower());
    
        final TimeSeriesCollection strongThreshold = new TimeSeriesCollection();
        strongThreshold.addSeries(chartSeries.getStrongThresholdUpper());
        strongThreshold.addSeries(chartSeries.getStrongThresholdLower());
        
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                "Value",
                strongOutlier,
                false,
                false,
                false);
        
        final XYPlot plot = chart.getXYPlot();
        
        plot.setDataset(1, weakOutlier);
        plot.setDataset(2, observed);
        plot.setDataset(3, midpoint);
        plot.setDataset(4, weakThreshold);
        plot.setDataset(5, strongThreshold);
        
        final XYLineAndShapeRenderer strongOutlierRenderer = new XYLineAndShapeRenderer(false, true);
        strongOutlierRenderer.setSeriesPaint(0, STRONG_OUTLIER_COLOR);
        
        final XYLineAndShapeRenderer weakOutlierRenderer = new XYLineAndShapeRenderer(false, true);
        weakOutlierRenderer.setSeriesPaint(0, WEAK_OUTLIER_COLOR);
        
        final XYLineAndShapeRenderer observedRenderer = new XYLineAndShapeRenderer(true, false);
        observedRenderer.setSeriesPaint(0, OBSERVED_COLOR);
        
        final XYLineAndShapeRenderer midpointRenderer = new XYLineAndShapeRenderer(true, false);
        midpointRenderer.setSeriesPaint(0, MIDPOINT_COLOR);
        
        final XYDifferenceRenderer weakThresholdRenderer = new XYDifferenceRenderer();
        weakThresholdRenderer.setPositivePaint(THRESHOLD_COLOR);
        weakThresholdRenderer.setSeriesPaint(0, THRESHOLD_COLOR);
        weakThresholdRenderer.setSeriesPaint(1, THRESHOLD_COLOR);
    
        final XYDifferenceRenderer strongThresholdRenderer = new XYDifferenceRenderer();
        strongThresholdRenderer.setPositivePaint(THRESHOLD_COLOR);
        strongThresholdRenderer.setSeriesPaint(0, THRESHOLD_COLOR);
        strongThresholdRenderer.setSeriesPaint(1, THRESHOLD_COLOR);
        
        plot.setRenderer(0, strongOutlierRenderer);
        plot.setRenderer(1, weakOutlierRenderer);
        plot.setRenderer(2, observedRenderer);
        plot.setRenderer(3, midpointRenderer);
        plot.setRenderer(4, weakThresholdRenderer);
        plot.setRenderer(5, strongThresholdRenderer);
    
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }
    
    public static ApplicationFrame createChartFrame(String title, JFreeChart... charts) {
        ApplicationFrame chartFrame = new ApplicationFrame(title);
    
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        
        for (JFreeChart chart : charts) {
            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 300));
            chartPanel.setMouseZoomable(true, false);
            panel.add(chartPanel);
        }
        chartFrame.setContentPane(panel);
        
        return chartFrame;
    }
    
    public static void showChartFrame(ApplicationFrame chartFrame) {
        chartFrame.pack();
        RefineryUtilities.positionFrameRandomly(chartFrame);
        chartFrame.setVisible(true);
    }
    
    public static Second toSecond(long epochSecond) {
        return new Second(Date.from(Instant.ofEpochSecond(epochSecond)));
    }
}
