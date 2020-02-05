package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.util.C;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class GraphiteSourceTest {

    @Mock
    private GraphiteClient graphiteClient;

    private GraphiteSource sourceUnderTest;
    private List<GraphiteResult> graphiteResults = new ArrayList<>();
    private List<GraphiteResult> graphiteResults_null = new ArrayList<>();
    private int binSizeInSecs = 5 * C.SECS_PER_MIN;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new GraphiteSource(graphiteClient);
    }

    @Test
    public void testGetMetricData() {
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));

        val actual = sourceUnderTest.getMetricData(C.SECONDS_PER_DAY * 7, binSizeInSecs, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        val actual = sourceUnderTest.getMetricData(C.SECONDS_PER_DAY, binSizeInSecs, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        val actual = sourceUnderTest.getMetricData(C.SECONDS_PER_DAY, binSizeInSecs, "null_value");
        val dataSourceResult = buildDataSourceResult(GraphiteSource.MISSING_VALUE, 1578307488);
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        dataSourceResults.add(dataSourceResult);
        assertEquals(dataSourceResults, actual);
    }

    private void initTestObjects() {
        graphiteResults.add(buildGraphiteResult());
        graphiteResults_null.add(buildNullValueGraphiteResult());
    }

    private void initDependencies() {
        when(graphiteClient.getData(C.SECONDS_PER_DAY * 7, C.SECONDS_PER_DAY * 6, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY * 6, C.SECONDS_PER_DAY * 5, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY * 5, C.SECONDS_PER_DAY * 4, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY * 4, C.SECONDS_PER_DAY * 3, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY * 3, C.SECONDS_PER_DAY * 2, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY * 2, C.SECONDS_PER_DAY, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY, 0, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(C.SECONDS_PER_DAY, 0, 288, "null_metric")).thenReturn(new ArrayList<>());
        when(graphiteClient.getData(C.SECONDS_PER_DAY, 0, 288, "null_value")).thenReturn(graphiteResults_null);
    }

    private GraphiteResult buildGraphiteResult() {
        GraphiteResult graphiteResult = new GraphiteResult();
        String[][] dataPoints = {
                {"1", "1578307488"},
                {"3", "1578307489"}
        };
        graphiteResult.setDatapoints(dataPoints);
        return graphiteResult;
    }

    private GraphiteResult buildNullValueGraphiteResult() {
        GraphiteResult graphiteResult = new GraphiteResult();
        String[][] dataPoints = {
                {null, "1578307488"}
        };
        graphiteResult.setDatapoints(dataPoints);
        return graphiteResult;
    }

    private DataSourceResult buildDataSourceResult(Double value, long epochSecs) {
        DataSourceResult dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }

}
