package com.expedia.adaptivealerting.anomdetect.source.data.metrictank;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.util.TimeConstantsUtil;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MetrictankSourceTest {

    @Mock
    private MetrictankClient metrictankClient;

    private MetrictankSource sourceUnderTest;
    private List<MetrictankResult> metrictankResults = new ArrayList<>();
    private List<MetrictankResult> metrictankResults_null = new ArrayList<>();
    private int intervalLength = 5 * TimeConstantsUtil.SECONDS_PER_MIN;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new MetrictankSource(metrictankClient);
    }

    @Test
    public void testGetMetricData() {
        val dataSourceResults = buildDataSourceResults(7);
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:09:55Z"), stringToEpochSeconds("2018-04-08T01:09:55Z"), intervalLength, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_time_window_less_than_day() {
        val dataSourceResults = buildDataSourceResults(1);
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:09:55Z"), stringToEpochSeconds("2018-04-02T00:09:55Z"), intervalLength, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_value");
        val dataSourceResult = buildDataSourceResult(MetrictankSource.MISSING_VALUE, stringToEpochSeconds("2018-04-01T01:05:00Z"));
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        dataSourceResults.add(dataSourceResult);
        assertEquals(dataSourceResults, actual);
    }

    private void initTestObjects() {
        metrictankResults.add(buildMetrictankResult());
        metrictankResults_null.add(buildNullValueMetrictankResult());
    }

    private void initDependencies() {
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-02T01:05:00Z"), stringToEpochSeconds("2018-04-03T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-03T01:05:00Z"), stringToEpochSeconds("2018-04-04T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-04T01:05:00Z"), stringToEpochSeconds("2018-04-05T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-05T01:05:00Z"), stringToEpochSeconds("2018-04-06T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-06T01:05:00Z"), stringToEpochSeconds("2018-04-07T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-07T01:05:00Z"), stringToEpochSeconds("2018-04-08T01:05:00Z"), intervalLength, "metric_name")).thenReturn(metrictankResults);

        when(metrictankClient.getData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_metric")).thenReturn(new ArrayList<>());
        when(metrictankClient.getData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_value")).thenReturn(metrictankResults_null);
    }

    private MetrictankResult buildMetrictankResult() {
        MetrictankResult metricTankResult = new MetrictankResult();
        String[][] dataPoints = {
                {"1", "1522544700"},
                {"3", "1523144900"},
                {"5", "1523149500"}
        };
        metricTankResult.setDatapoints(dataPoints);
        return metricTankResult;
    }

    private MetrictankResult buildNullValueMetrictankResult() {
        MetrictankResult metricTankResult = new MetrictankResult();
        String[][] dataPoints = {
                {null, "1522544700"},
                {null, "1523144900"}
        };
        metricTankResult.setDatapoints(dataPoints);
        return metricTankResult;
    }

    private List<DataSourceResult> buildDataSourceResults(int noOfResults) {

        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        for (int i = 0; i < noOfResults; i++) {
            dataSourceResults.add(buildDataSourceResult(1.0, 1522544700));
            dataSourceResults.add(buildDataSourceResult(3.0, 1523144900));
        }
        return dataSourceResults;
    }

    private DataSourceResult buildDataSourceResult(Double value, long epochSecs) {
        DataSourceResult dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }

    private long stringToEpochSeconds(String time) {
        return Instant.parse(time).getEpochSecond();
    }

}