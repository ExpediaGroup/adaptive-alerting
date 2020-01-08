package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GraphiteSourceTest {

    private GraphiteSource sourceUnderTest;

    @Mock
    private GraphiteClient graphiteClient;

    private GraphiteResult[] graphiteResults = new GraphiteResult[5];

    private List<DataSourceResult> dataSourceResults = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new GraphiteSource(graphiteClient);
    }

    @Test
    public void testGetMetricData() {
        val result = sourceUnderTest.getMetricData("1d", 2016, "");
        assertEquals(dataSourceResults, result);
    }

    private void initTestObjects() {
        GraphiteResult graphiteResult = new GraphiteResult();
        String[][] dataPoints = new String[2][2];
        dataPoints[0][0] = "1";
        dataPoints[0][1] = "1578307488";
        dataPoints[1][0] = "3";
        dataPoints[1][1] = "1578307489";
        graphiteResult.setDatapoints(dataPoints);
        graphiteResults[0] = graphiteResult;

        DataSourceResult dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(1.0);
        dataSourceResult.setEpochSecond(1578307488);

        DataSourceResult dataSourceResult1 = new DataSourceResult();
        dataSourceResult1.setDataPoint(3.0);
        dataSourceResult1.setEpochSecond(1578307489);

        dataSourceResults.add(dataSourceResult);
        dataSourceResults.add(dataSourceResult1);
    }

    private void initDependencies() {
        when(graphiteClient.getMetricData(anyString(), anyInt(), anyString())).thenReturn(graphiteResults);
    }

}
