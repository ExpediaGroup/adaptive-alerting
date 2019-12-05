package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.sma;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.val;

import static org.junit.Assert.assertEquals;

public class SmaPointForcasterTest {
    private static final double TOLERANCE = 0.001;

    private static List<SmaPointForecasterTestRow> testRows;

    @BeforeClass
    public static void setUpClass() {
        loadSampleData();
    }

    @Test
    public void testWithInitialValues() {
        SmaPointForecasterParams params = new SmaPointForecasterParams()
            .setLookBackPeriod(3)
            .setInitialPeriodOfValues(Arrays.asList(1.0, 2.0, 3.0));
        SmaPointForecaster sma3 = new SmaPointForecaster(params);

        // [1.0, 2.0, 3.0]; mean=2.0
        assertEquals(2.0, sma3.getMean(), TOLERANCE);
        // [2.0, 3.0, 4.0]; mean=3.0
        assertEquals(3.0, sma3.forecast(createMetricData(4.0)).getValue(), TOLERANCE);
        // [3.0, 4.0, 5.0]; mean=4.0
        assertEquals(4.0, sma3.forecast(createMetricData(5.0)).getValue(), TOLERANCE);
    }

    @Test
    public void testNoInitialValues() {
        SmaPointForecasterParams params = new SmaPointForecasterParams().setLookBackPeriod(3);
        SmaPointForecaster sma3 = new SmaPointForecaster(params);

        // [2.0]; mean=2.0
        assertEquals(2.0, sma3.forecast(createMetricData(2.0)).getValue(), TOLERANCE);
        // [2.0, 4.0]; mean=3.0
        assertEquals(3.0, sma3.forecast(createMetricData(4.0)).getValue(), TOLERANCE);
        // [2.0, 4.0, 6.0]; mean=4.0
        assertEquals(4.0, sma3.forecast(createMetricData(6.0)).getValue(), TOLERANCE);
        // [4.0, 6.0, 8.0]; mean=6.0
        assertEquals(6.0, sma3.forecast(createMetricData(8.0)).getValue(), TOLERANCE);
    }

    @Test
    public void testAgainstSampleData() {
        List<SmaPointForecaster> forecasters = Arrays.asList(
            createSmaForecaster(1),
            createSmaForecaster(3),
            createSmaForecaster(9),
            createSmaForecaster(21)
        );

        List<Function<SmaPointForecasterTestRow, Double>> expectedValueExtractors = Arrays.asList(
            SmaPointForecasterTestRow::getSma1,
            SmaPointForecasterTestRow::getSma3,
            SmaPointForecasterTestRow::getSma9,
            SmaPointForecasterTestRow::getSma21
        );

        testRows.forEach(testRow -> {
            for (int i = 0; i < forecasters.size(); i++) {
                validateForecaster(testRow, forecasters.get(i), expectedValueExtractors.get(i));
            }
        });
    }

    private SmaPointForecaster createSmaForecaster(int period) {
        return new SmaPointForecaster(new SmaPointForecasterParams().setLookBackPeriod(period));
    }

    private void validateForecaster(SmaPointForecasterTestRow testRow, SmaPointForecaster forecaster,
        Function<SmaPointForecasterTestRow, Double> expectedValueExtractor) {

        Double expectedSmaValue = expectedValueExtractor.apply(testRow);

        MetricData metricData = createMetricData(testRow.getObserved());
        double actualSmaValue = forecaster.forecast(metricData).getValue();

        assertEquals(failureMessage(forecaster, testRow), expectedSmaValue, actualSmaValue, TOLERANCE);
    }

    private MetricData createMetricData(double observed) {
        return new MetricData(
            new MetricDefinition("some-key"),
            observed,
            System.currentTimeMillis()
        );
    }

    private String failureMessage(SmaPointForecaster forecaster, SmaPointForecasterTestRow testRow) {
        int lookbackPeriod = forecaster.getParams().getLookBackPeriod();
        return "forecaster SMA" + lookbackPeriod + " failed. testRow=" + testRow;
    }

    private static void loadSampleData() {
        val is = ClassLoader.getSystemResourceAsStream("tests/sma-sample-input.csv");
        testRows = new CsvToBeanBuilder<SmaPointForecasterTestRow>(new InputStreamReader(is))
            .withType(SmaPointForecasterTestRow.class)
            .build()
            .parse();
    }
}