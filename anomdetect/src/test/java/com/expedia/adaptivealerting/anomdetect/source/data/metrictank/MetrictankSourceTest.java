package com.expedia.adaptivealerting.anomdetect.source.data.metrictank;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.util.TimeConstantsUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
public class MetrictankSourceTest {

    private static final String EARLIEST_TIME = "2018-04-01T01:05:00Z";

    @Mock
    private MetrictankClient client;

    private MetrictankSource sourceUnderTest;

    private long earliestTimeInEpoch;
    private int intervalLength = 5 * TimeConstantsUtil.SECONDS_PER_MIN;
    private int noOfBinsInADay;

    private List<MetrictankResult> metrictankResults = new ArrayList<>();
    private List<MetrictankResult> metrictankResults_null = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new MetrictankSource(client);
    }

    @Test
    public void testGetMetricData_seven_days() {
        val windowLength = 7;
        val latestTimeInEpoch = earliestTimeInEpoch + windowLength * TimeConstantsUtil.SECONDS_PER_DAY;
        val expected = buildExpectedResults(earliestTimeInEpoch, windowLength);
        val actual = sourceUnderTest.getMetricData(earliestTimeInEpoch, latestTimeInEpoch, intervalLength, "metric_name");
        assertEquals(expected, actual);
        assertEquals(noOfBinsInADay * windowLength, actual.size());
    }

    @Test
    public void testGetMetricData_time_window_less_than_day() {
        val latestTimeInEpoch = earliestTimeInEpoch + TimeConstantsUtil.SECONDS_PER_DAY / 2;
        val expected = buildExpectedResults(earliestTimeInEpoch, 1);
        val actual = sourceUnderTest.getMetricData(earliestTimeInEpoch, latestTimeInEpoch, intervalLength, "metric_name");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        val latestTimeInEpoch = earliestTimeInEpoch + TimeConstantsUtil.SECONDS_PER_DAY;
        val actual = sourceUnderTest.getMetricData(earliestTimeInEpoch, latestTimeInEpoch, intervalLength, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        val latestTimeInEpoch = earliestTimeInEpoch + TimeConstantsUtil.SECONDS_PER_DAY;
        val actual = sourceUnderTest.getMetricData(earliestTimeInEpoch, latestTimeInEpoch, intervalLength, "null_value");
        val dataSourceResult = buildDataSourceResult(MetrictankSource.MISSING_VALUE, earliestTimeInEpoch);
        val expected = new ArrayList<>();
        expected.add(dataSourceResult);
        assertEquals(expected, actual);
    }

    private void initTestObjects() {
        earliestTimeInEpoch = Instant.parse(EARLIEST_TIME).getEpochSecond();
        noOfBinsInADay = getBinsInDay(intervalLength);
        metrictankResults.add(buildMetrictankResult(earliestTimeInEpoch));
        metrictankResults_null.add(buildNullMetrictankResult());
    }

    private void initDependencies() {
        when(client.getData(anyLong(), anyLong(), anyInt(), eq("metric_name"))).thenReturn(metrictankResults);
        when(client.getData(anyLong(), anyLong(), anyInt(), eq("null_metric"))).thenReturn(new ArrayList<>());
        when(client.getData(anyLong(), anyLong(), anyInt(), eq("null_value"))).thenReturn(metrictankResults_null);
    }

    private MetrictankResult buildMetrictankResult(long earliestTime) {
        //For testing, we return an extra data point to see if graphite source discards it or not.
        String[][] dataPoints = new String[noOfBinsInADay + 1][2];
        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i][0] = String.valueOf(i);
            dataPoints[i][1] = String.valueOf(earliestTime);
            earliestTime = earliestTime + intervalLength;
        }
        MetrictankResult result = new MetrictankResult();
        result.setDatapoints(dataPoints);
        return result;
    }

    private MetrictankResult buildNullMetrictankResult() {
        MetrictankResult result = new MetrictankResult();
        String[][] dataPoints = {
                {null, "1522544700"},
                {null, "1523144900"}
        };
        result.setDatapoints(dataPoints);
        return result;
    }

    private List<DataSourceResult> buildExpectedResults(long earliestTime, int windowLength) {
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        double value = 0;
        long epochSecond = earliestTime;

        for (int i = 0; i < windowLength * noOfBinsInADay; i++) {
            if (value > noOfBinsInADay - 1) {
                value = 0;
                epochSecond = earliestTime;
            }
            dataSourceResults.add(buildDataSourceResult(value, epochSecond));
            epochSecond += intervalLength;
            value++;
        }
        return dataSourceResults;
    }

    private DataSourceResult buildDataSourceResult(Double value, long epochSecs) {
        val dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }

    private int getBinsInDay(int intervalLength) {
        return TimeConstantsUtil.SECONDS_PER_DAY / intervalLength;
    }
}
