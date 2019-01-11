package com.expedia.adaptivealerting.anomdetect.arima;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

/**
 * @author ddivakar
 */
public class ArimaAnomalyDetectorTest {
    private static final int WARMUP_PERIOD = 15;

    // TODO This tolerance is very loose. Can we tighten it up? [WLW]
    private static final double TOLERANCE = 0.1;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private static List<ArimaTestRow> data;


    @BeforeClass
    public static void setUpClass() {
        readDataFromCsv();
    }

    @Before
    public void setUp() {
        this.detectorUUID = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test
    public void testEvaluate() {
        final ListIterator<ArimaTestRow> testRows = data.listIterator();
        final ArimaTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();

        final ArimaParams params = new ArimaParams()
                .setInitValue(observed0)
                .setWarmUpPeriod(WARMUP_PERIOD);
        final ArimaAnomalyDetector detector = new ArimaAnomalyDetector(detectorUUID, params);

        int noOfDataPoints = 1;
        int noOfDataPointsExceedingTolerance = 0;

        while (testRows.hasNext()) {
            final ArimaTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            // difference of predicted values between used model and statsmodels.tsa.arima_model as a fraction of observed
            final double tolerance = (testRow.getPredictedstatsmodelstsaarimamodel() - testRow.getForecast())/testRow.getObserved();

            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel level = detector.classify(metricData).getAnomalyLevel();

            assertEquals((testRow.getMu()), detector.getLong_term_forecast_mu());
            assertEquals((testRow.getForecast()), detector.getTarget());
            assertEquals(AnomalyLevel.valueOf(testRow.getAnomalyLevel()), level);
            if ((testRow.getAnomalyLevel() != "MODEL_WARMUP") && (tolerance > 0.25)) {
                noOfDataPointsExceedingTolerance += 1;
            }
            noOfDataPoints += 1;
        }
        System.out.printf("Total values in series: %d", noOfDataPoints);
        System.out.printf("\nWarmup: %d", detector.getParams().getWarmUpPeriod());
        System.out.printf("\nValues with tolerance higher than 25/100 for series against library ARIMA model: %d", noOfDataPointsExceedingTolerance);
    }

    private static void readDataFromCsv() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-arima.csv");
        data = new CsvToBeanBuilder<ArimaTestRow>(new InputStreamReader(is))
                .withType(ArimaTestRow.class)
                .build()
                .parse();
    }
}