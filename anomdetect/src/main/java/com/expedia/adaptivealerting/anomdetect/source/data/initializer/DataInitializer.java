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
package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.SeasonalPointForecaster;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteSource;
import com.expedia.adaptivealerting.anomdetect.source.data.initializer.throttlegate.ThrottleGate;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.MetricUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public class DataInitializer {

    public static final String BASE_URI = "graphite-base-uri";
    public static final String EARLIEST_TIME = "graphite-earliest-time";
    public static final String MAX_DATA_POINTS = "graphite-max-data-points";
    public static final String DATA_RETRIEVAL_TAG_KEY = "graphite-data-retrieval-key";
    public static final String THROTTLE_GATE_LIKELIHOOD = "throttle-gate-likelihood";

    private String earliestTime;
    private Integer maxDataPoints;
    private String dataRetrievalTagKey;
    private final DataSource dataSource;
    private final ThrottleGate throttleGate;

    public DataInitializer(Config config, ThrottleGate throttleGate, DataSource dataSource) {
        this.throttleGate = throttleGate;
        this.dataSource = dataSource;
        this.earliestTime = config.getString(EARLIEST_TIME);
        this.maxDataPoints = config.getInt(MAX_DATA_POINTS);
        this.dataRetrievalTagKey = config.getString(DATA_RETRIEVAL_TAG_KEY);
    }

    public void initializeDetector(MappedMetricData mappedMetricData, Detector detector) {
        // TODO: Forecasting Detector initialisation is currently limited to Seasonal Naive detector and assumes Graphite source
        if (isSeasonalNaiveDetector(detector)) {
            if (throttleGate.isOpen()) {
                log.info("Throttle gate is open, initializing data and creating a detector");
            val forecastingDetector = (ForecastingDetector) detector;
            initializeForecastingDetector(mappedMetricData, forecastingDetector);
            } else {
                throw new DetectorDataInitializationThrottledException(
                        "Throttle gate is closed, skipping initializing data and detector creation.");
            }
        }
    }

    private boolean isSeasonalNaiveDetector(Detector detector) {
        return detector instanceof ForecastingDetector && "seasonalnaive".equals(detector.getName());
    }

    private void initializeForecastingDetector(MappedMetricData mappedMetricData, ForecastingDetector forecastingDetector) {
        log.info("Initializing detector data");
        val data = getHistoricalData(mappedMetricData, forecastingDetector);
        log.info("Fetched total of {} historical data points for buffer", data.size());
        val metricDefinition = mappedMetricData.getMetricData().getMetricDefinition();
        populateForecastingDetectorWithHistoricalData(forecastingDetector, data, metricDefinition);
        log.info("Replayed {} historical data points for {}", data.size(), forecastingDetector.getClass().getSimpleName());
    }

    private List<DataSourceResult> getHistoricalData(MappedMetricData mappedMetricData, ForecastingDetector forecastingDetector) {
        val target = MetricUtil.getDataRetrievalValueOrMetricKey(mappedMetricData, dataRetrievalTagKey);
        PointForecaster pointForecaster = forecastingDetector.getPointForecaster();
        if (pointForecaster instanceof SeasonalPointForecaster) {
            val seasonalPointForecaster = ((SeasonalPointForecaster) pointForecaster);
            val cycleLength = seasonalPointForecaster.getCycleLength();
            val intervalLength = seasonalPointForecaster.getIntervalLength();
            val fullWindow = cycleLength * intervalLength;
            val latestTime = mappedMetricData.getMetricData().getTimestamp();
            val earliestTime = latestTime - fullWindow;
            return dataSource.getMetricData(earliestTime, latestTime, intervalLength, target);
        } else {
            val message = "No seasonal point forecaster found for forecasting detector " + forecastingDetector.getUuid();
            throw new RuntimeException(message);
        }
    }

    //TODO. Using one-line methods for object creation to support unit testing. We can replace this with factories later on.
    // https://github.com/mockito/mockito/wiki/Mocking-Object-Creation#pattern-1---using-one-line-methods-for-object-creation

    GraphiteClient makeClient(String graphiteBaseUri) {
        return new GraphiteClient(graphiteBaseUri, new HttpClientWrapper(), new ObjectMapper());
    }

    DataSource makeSource(GraphiteClient client) {
        return new GraphiteSource(client);
    }

    private void populateForecastingDetectorWithHistoricalData(ForecastingDetector forecastingDetector, List<DataSourceResult> data, MetricDefinition metricDefinition) {
        for (DataSourceResult dataSourceResult : data) {
            val metricData = dataSourceResultToMetricData(dataSourceResult, metricDefinition);
            forecastingDetector.getPointForecaster().forecast(metricData);
        }
    }

    private MetricData dataSourceResultToMetricData(DataSourceResult dataSourceResult, MetricDefinition metricDefinition) {
        val dataPoint = dataSourceResult.getDataPoint();
        val epochSecond = dataSourceResult.getEpochSecond();
        return new MetricData(metricDefinition, dataPoint, epochSecond);
    }
}
