package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient.DEFAULT_FROM_VALUE;
import static com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient.DEFAULT_MAX_DATA_POINTS;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

public class GraphiteSourceTest {
    @Mock
    private GraphiteClient graphiteClient;

    private GraphiteSource sourceUnderTest;
    private GraphiteResult[] graphiteResults = new GraphiteResult[2];
    private GraphiteResult[] graphiteResults_null = new GraphiteResult[2];

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new GraphiteSource(graphiteClient);
    }

    @Test
    public void testGetMetricData() {
        val dataSourceResult = getDataSourceResult(1.0, 1578307488);
        DataSourceResult dataSourceResult1 = getDataSourceResult(3.0, 1578307489);

        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        dataSourceResults.add(dataSourceResult);
        dataSourceResults.add(dataSourceResult1);

        val actual = sourceUnderTest.getMetricData(DEFAULT_FROM_VALUE, DEFAULT_MAX_DATA_POINTS, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        val actual = sourceUnderTest.getMetricData(DEFAULT_FROM_VALUE, DEFAULT_MAX_DATA_POINTS, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        val actual = sourceUnderTest.getMetricData(DEFAULT_FROM_VALUE, DEFAULT_MAX_DATA_POINTS, "null_value");
        val dataSourceResult = getDataSourceResult(Double.NEGATIVE_INFINITY, 1578307488);
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        dataSourceResults.add(dataSourceResult);
        assertEquals(dataSourceResults, actual);
    }

    private void initTestObjects() {
        graphiteResults[0] = getResult();
        graphiteResults_null[0] = getNullValueResult();
    }

    private void initDependencies() {
        when(graphiteClient.getMetricData(DEFAULT_FROM_VALUE, DEFAULT_MAX_DATA_POINTS, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getMetricData(DEFAULT_FROM_VALUE, DEFAULT_MAX_DATA_POINTS, "null_metric")).thenReturn(new GraphiteResult[0]);
        when(graphiteClient.getMetricData(DEFAULT_FROM_VALUE, DEFAULT_MAX_DATA_POINTS, "null_value")).thenReturn(graphiteResults_null);
    }

    private GraphiteResult getResult() {
        GraphiteResult graphiteResult = new GraphiteResult();
        String[][] dataPoints = new String[2][2];
        dataPoints[0][0] = "1";
        dataPoints[0][1] = "1578307488";
        dataPoints[1][0] = "3";
        dataPoints[1][1] = "1578307489";
        graphiteResult.setDatapoints(dataPoints);
        return graphiteResult;
    }

    private GraphiteResult getNullValueResult() {
        GraphiteResult graphiteResult = new GraphiteResult();
        String[][] dataPoints = new String[1][2];
        dataPoints[0][0] = null;
        dataPoints[0][1] = "1578307488";
        graphiteResult.setDatapoints(dataPoints);
        return graphiteResult;
    }

    private DataSourceResult getDataSourceResult(Double value, long epochSecs) {
        DataSourceResult dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }
}
