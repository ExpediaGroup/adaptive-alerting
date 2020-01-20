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
import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteSource;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.MetricUtil;
import com.expedia.adaptivealerting.anomdetect.util.PropertiesUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public class DataInitializer {

    public void initializeDetector(MappedMetricData mappedMetricData, Detector detector) {
        if (detector != null) {
            if (detector instanceof ForecastingDetector) {
                val data = getHistoricalData(mappedMetricData);
                val forecastingDetector = (ForecastingDetector) detector;
                val metricDefinition = mappedMetricData.getMetricData().getMetricDefinition();
                populateForecastingDetectorWIthHistoricalData(forecastingDetector, data, metricDefinition);
            }
        }
    }

    private void populateForecastingDetectorWIthHistoricalData(ForecastingDetector forecastingDetector, List<DataSourceResult> data, MetricDefinition metricDefinition) {
        for (DataSourceResult dataSourceResult : data) {
            MetricData metricData = dataSourceResultToMetricData(dataSourceResult, metricDefinition);
            // We throw away the forecast as we only care about letting the forecaster "see" the historical value to update its internal model
            forecastingDetector.getPointForecaster().forecast(metricData);
        }
    }

    private MetricData dataSourceResultToMetricData(DataSourceResult dataSourceResult, MetricDefinition metricDefinition) {
        Double dataPoint = dataSourceResult.getDataPoint();
        long epochSecond = dataSourceResult.getEpochSecond();
        return new MetricData(metricDefinition, dataPoint, epochSecond);
    }

    private List<DataSourceResult> getHistoricalData(MappedMetricData mappedMetricData) {
        val target = MetricUtil.getMetricFunctionOrKey(mappedMetricData);
        val client = getClient();
        val dataSource = makeSource(client);
        val earliest = PropertiesUtil.getValueFromProperty("graphite.earliestTime");
        val maxDataPoints = Integer.parseInt(PropertiesUtil.getValueFromProperty("graphite.maxDataDataPoints"));
        return dataSource.getMetricData(earliest, maxDataPoints, target);
    }

    private GraphiteClient getClient() {
        val graphiteBaseUri = PropertiesUtil.getValueFromProperty("graphite.baseUri");
        return makeClient(graphiteBaseUri);
    }

    //Using one-line methods for object creation to support unit testing
    GraphiteClient makeClient(String graphiteBaseUri) {
        return new GraphiteClient(graphiteBaseUri, new HttpClientWrapper(), new ObjectMapper());
    }

    DataSource makeSource(GraphiteClient client) {
        return new GraphiteSource(client);
    }
}
