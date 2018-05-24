package com.expedia.adaptivealerting.core.evaluator;

import static org.junit.Assert.assertEquals;

/**
 * @author kashah
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.opencsv.bean.CsvToBeanBuilder;

public class RmseEvaluatorTests {

    private static List<RmseTestRow> calInflowTestRows;

    @BeforeClass
    public static void setUpClass() throws IOException {
        readData_calInflow();
    }

    // Class under test
    private RmseEvaluator evaluator;

    @Before
    public void setUp() {
        this.evaluator = new RmseEvaluator();
    }

    @Test
    public void testScore() {
        final ListIterator<RmseTestRow> testRows = calInflowTestRows.listIterator();
        while (testRows.hasNext()) {
            final RmseTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            final double predicted = testRow.getPredicted();
            assertEquals(testRow.getRmse(), evaluator.computeScore(observed, predicted), 0);
        }
    }

    private static void readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-rmse.csv");
        calInflowTestRows = new CsvToBeanBuilder<RmseTestRow>(new InputStreamReader(is)).withType(RmseTestRow.class)
                .build().parse();
    }

}
