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

    @Mock
    private MetrictankClient client;

    private MetrictankSource sourceUnderTest;
    private List<MetrictankResult> results_null = new ArrayList<>();
    private int intervalLength = 5 * TimeConstantsUtil.SECONDS_PER_MIN;
    private int noOfBinsInADay;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        this.sourceUnderTest = new MetrictankSource(client);
    }

    @Test
    public void testGetMetricData_seven_days() {

        List<MetrictankResult> resultsFirstDay = new ArrayList<>();
        resultsFirstDay.add(buildResult("2018-04-01T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-01T01:04:59Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsFirstDay);

        List<MetrictankResult> resultsSecondDay = new ArrayList<>();
        resultsSecondDay.add(buildResult("2018-04-02T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-02T01:04:59Z"), stringToEpochSeconds("2018-04-03T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsSecondDay);

        List<MetrictankResult> resultsThirdDay = new ArrayList<>();
        resultsThirdDay.add(buildResult("2018-04-03T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-03T01:04:59Z"), stringToEpochSeconds("2018-04-04T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsThirdDay);

        List<MetrictankResult> resultsFourthDay = new ArrayList<>();
        resultsFourthDay.add(buildResult("2018-04-04T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-04T01:04:59Z"), stringToEpochSeconds("2018-04-05T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsFourthDay);

        List<MetrictankResult> resultsFifthDay = new ArrayList<>();
        resultsFifthDay.add(buildResult("2018-04-05T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-05T01:04:59Z"), stringToEpochSeconds("2018-04-06T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsFifthDay);

        List<MetrictankResult> resultsSixthDay = new ArrayList<>();
        resultsSixthDay.add(buildResult("2018-04-06T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-06T01:04:59Z"), stringToEpochSeconds("2018-04-07T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsSixthDay);

        List<MetrictankResult> resultsSeventhDay = new ArrayList<>();
        resultsSeventhDay.add(buildResult("2018-04-07T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-07T01:04:59Z"), stringToEpochSeconds("2018-04-08T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsSeventhDay);

        val expected = buildDataSourceResults("2018-04-01T01:05:00Z", "2018-04-08T01:05:00Z");
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:09:55Z"), stringToEpochSeconds("2018-04-08T01:09:55Z"), intervalLength, "metric_name");
        assertEquals(expected, actual);
        assertEquals(noOfBinsInADay * 7, actual.size());
    }

    @Test
    public void testGetMetricData_time_window_less_than_day() {

        List<MetrictankResult> resultsFirstDay = new ArrayList<>();
        resultsFirstDay.add(buildResult("2018-04-01T01:05:00Z"));
        when(client.getData(stringToEpochSeconds("2018-04-01T01:04:59Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "metric_name")).thenReturn(resultsFirstDay);

        val expected = buildDataSourceResults("2018-04-01T01:05:00Z", "2018-04-02T01:05:00Z");
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:09:55Z"), stringToEpochSeconds("2018-04-02T00:09:55Z"), intervalLength, "metric_name");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMetricData_null_metric_data() {
        when(client.getData(stringToEpochSeconds("2018-04-01T01:04:59Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_metric")).thenReturn(new ArrayList<>());
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_metric");
        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void testGetMetricData_null_value() {
        when(client.getData(stringToEpochSeconds("2018-04-01T01:04:59Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_value")).thenReturn(results_null);
        val actual = sourceUnderTest.getMetricData(stringToEpochSeconds("2018-04-01T01:05:00Z"), stringToEpochSeconds("2018-04-02T01:05:00Z"), intervalLength, "null_value");
        val dataSourceResult = buildDataSourceResult(MetrictankSource.MISSING_VALUE, stringToEpochSeconds("2018-04-01T01:05:00Z"));
        List<DataSourceResult> expected = new ArrayList<>();
        expected.add(dataSourceResult);
        assertEquals(expected, actual);
    }

    private void initTestObjects() {
        results_null.add(buildNullValueResult());
        noOfBinsInADay = getBinsInDay(intervalLength);
    }

    private MetrictankResult buildResult(String time) {
        long earliest = stringToEpochSeconds(time);
        //For testing, we return an extra data point to see if graphite source discards it or not.
        String[][] dataPoints = new String[noOfBinsInADay + 1][2];
        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i][0] = String.valueOf(i);
            dataPoints[i][1] = String.valueOf(earliest);
            earliest = earliest + intervalLength;
        }
        MetrictankResult result = new MetrictankResult();
        result.setDatapoints(dataPoints);
        return result;
    }

    private MetrictankResult buildNullValueResult() {
        MetrictankResult result = new MetrictankResult();
        String[][] dataPoints = {
                {null, "1522544700"},
                {null, "1523144900"}
        };
        result.setDatapoints(dataPoints);
        return result;
    }

    private List<DataSourceResult> buildDataSourceResults(String earliestTime, String latestTime) {
        List<DataSourceResult> dataSourceResults = new ArrayList<>();
        int value = 0;
        for (long i = stringToEpochSeconds(earliestTime); i < stringToEpochSeconds(latestTime); i += intervalLength) {
            if (value > noOfBinsInADay - 1) {
                value = 0;
            }
            dataSourceResults.add(buildDataSourceResult(Double.valueOf(value), i));
            value++;
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

    private int getBinsInDay(int intervalLength) {
        return TimeConstantsUtil.SECONDS_PER_DAY / intervalLength;
    }
}