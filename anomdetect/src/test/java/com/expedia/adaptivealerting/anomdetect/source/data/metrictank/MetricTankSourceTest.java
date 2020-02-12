package com.expedia.adaptivealerting.anomdetect.source.data.metrictank;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.util.TimeConstantsUtil;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MetricTankSourceTest {

    @Mock
    private MetricTankClient metricTankClient;

    private MetricTankSource sourceUnderTest;
    private List<MetricTankResult> metricTankResults = new ArrayList<>();
    private List<MetricTankResult> metricTankResults_null = new ArrayList<>();
    private int intervalLength = 5 * TimeConstantsUtil.SECONDS_PER_MIN;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new MetricTankSource(metricTankClient);
    }

    @Test
    public void testGetMetricData() {
        val dataSourceResults = buildDataSourceResults(7);
        val actual = sourceUnderTest.getMetricData(1580297095, 1580901895, intervalLength, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_time_window_less_than_day() {
        val dataSourceResults = buildDataSourceResults(1);
        val actual = sourceUnderTest.getMetricData(1580297095, 1580340295, intervalLength, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        val actual = sourceUnderTest.getMetricData(1580815495, 1580901895, intervalLength, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        val actual = sourceUnderTest.getMetricData(1580815495, 1580901895, intervalLength, "null_value");
        val dataSourceResult = buildDataSourceResult(MetricTankSource.MISSING_VALUE, 1578307488);
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        dataSourceResults.add(dataSourceResult);
        assertEquals(dataSourceResults, actual);
    }

    private void initTestObjects() {
        metricTankResults.add(buildGraphiteResult());
        metricTankResults_null.add(buildNullValueGraphiteResult());
    }

    private void initDependencies() {
        when(metricTankClient.getData(1580297095, 1580383495, "metric_name")).thenReturn(metricTankResults);
        when(metricTankClient.getData(1580383495, 1580469895, "metric_name")).thenReturn(metricTankResults);
        when(metricTankClient.getData(1580469895, 1580556295, "metric_name")).thenReturn(metricTankResults);
        when(metricTankClient.getData(1580556295, 1580642695, "metric_name")).thenReturn(metricTankResults);
        when(metricTankClient.getData(1580642695, 1580729095, "metric_name")).thenReturn(metricTankResults);
        when(metricTankClient.getData(1580729095, 1580815495, "metric_name")).thenReturn(metricTankResults);
        when(metricTankClient.getData(1580815495, 1580901895, "metric_name")).thenReturn(metricTankResults);

        when(metricTankClient.getData(1580297095, 1580383495, "metric_name")).thenReturn(metricTankResults);

        when(metricTankClient.getData(1580815495, 1580901895, "null_metric")).thenReturn(new ArrayList<>());
        when(metricTankClient.getData(1580815495, 1580901895, "null_value")).thenReturn(metricTankResults_null);
    }

    private MetricTankResult buildGraphiteResult() {
        MetricTankResult metricTankResult = new MetricTankResult();
        String[][] dataPoints = {
                {"1", "1578307488"},
                {"3", "1578307489"}
        };
        metricTankResult.setDatapoints(dataPoints);
        return metricTankResult;
    }

    private MetricTankResult buildNullValueGraphiteResult() {
        MetricTankResult metricTankResult = new MetricTankResult();
        String[][] dataPoints = {
                {null, "1578307488"}
        };
        metricTankResult.setDatapoints(dataPoints);
        return metricTankResult;
    }

    private List<DataSourceResult> buildDataSourceResults(int noOfResults) {

        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        for (int i = 0; i < noOfResults; i++) {
            dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
            dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        }
        return dataSourceResults;
    }

    private DataSourceResult buildDataSourceResult(Double value, long epochSecs) {
        DataSourceResult dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }

}
