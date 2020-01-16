package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecaster;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteSource;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.PropertyValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;

import java.io.IOException;

public class GraphiteDataInitializer implements DataInitializer {

    private static final String EARLIEST_TIME = "7d";
    private static final Integer MAX_DATA_POINTS = 2016;

    public void initializeDetector(MappedMetricData metricData, Detector detector) {
        if (detector != null && detector.getClass().equals("seasonalnaive")) {
            val forecastingDetector = (ForecastingDetector) detector;
            val seasonalNaivePointForecaster = (SeasonalNaivePointForecaster) forecastingDetector.getPointForecaster();
            double[] data = getHistoricalData(metricData);
            seasonalNaivePointForecaster.getBuffer().initBuffer(data);
        }
    }

    private double[] getHistoricalData(MappedMetricData mappedMetricData) {
        try {
            val propValues = new PropertyValues().getPropValues();
            val graphiteClient = new GraphiteClient(propValues.getProperty("graphite.baseUri"), new HttpClientWrapper(), new ObjectMapper());
            val dataSource = new GraphiteSource(graphiteClient);
            val target = getTarget(mappedMetricData);
            val results = dataSource.getMetricData(EARLIEST_TIME, MAX_DATA_POINTS, target);
            val resultsSize = results.size();
            double[] data = new double[resultsSize];
            for (int i = 0; i < resultsSize; i++) {
                data[i] = results.get(i).getDataPoint();
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Reading properties from the config file failed", e);
        }
    }

    private String getTarget(MappedMetricData mappedMetricData) {
        val metricData = mappedMetricData.getMetricData();
        val metricDefinition = metricData.getMetricDefinition();
        val metricTags = metricDefinition.getTags();
        val metricFunction = metricTags != null && metricTags.getKv() != null
                ? metricTags.getKv().get("function") : null;
        return metricFunction != null
                ? metricFunction : metricDefinition.getKey();
    }
}
