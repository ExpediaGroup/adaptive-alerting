package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

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

public class GraphiteSourceTest {

    @Mock
    private GraphiteClient graphiteClient;

    private GraphiteSource sourceUnderTest;
    private List<GraphiteResult> graphiteResults = new ArrayList<>();
    private List<GraphiteResult> graphiteResults_null = new ArrayList<>();
    private int binSizeInSecs = 5 * TimeConstantsUtil.SECONDS_PER_MIN;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new GraphiteSource(graphiteClient);
    }

    @Test
    public void testGetMetricData() {
        val dataSourceResults = buildDataSourceResults();
        val actual = sourceUnderTest.getMetricData(1580297095, 1580901895, binSizeInSecs, "metric_name");
        assertEquals(dataSourceResults, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        val actual = sourceUnderTest.getMetricData(1580815495, 1580901895, binSizeInSecs, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        val actual = sourceUnderTest.getMetricData(1580815495, 1580901895, binSizeInSecs, "null_value");
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
        when(graphiteClient.getData(1580297095, 1580383495, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580383495, 1580469895, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580469895, 1580556295, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580556295, 1580642695, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580642695, 1580729095, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580729095, 1580815495, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580815495, 1580901895, 288, "metric_name")).thenReturn(graphiteResults);
        when(graphiteClient.getData(1580815495, 1580901895, 288, "null_metric")).thenReturn(new ArrayList<>());
        when(graphiteClient.getData(1580815495, 1580901895, 288, "null_value")).thenReturn(graphiteResults_null);
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

    private List<DataSourceResult> buildDataSourceResults() {

        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
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
