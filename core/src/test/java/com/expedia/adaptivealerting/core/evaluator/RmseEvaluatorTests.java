package com.expedia.adaptivealerting.core.evaluator;

/**
 * @author kashah
 *
 */

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class RmseEvaluatorTests {

    // Class under test
    private RmseEvaluator evaluator;

    @Before
    public void setUp() {
        this.evaluator = new RmseEvaluator();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSeriesLength() {
        double[] sample = { 1.9, 2.9, 3.4, 3.5 };
        double[] prediction = { 1.9, 2.9, 3.4, 3.5, 4.5 };
        evaluator.getScore(sample, prediction);
    }

    @Test
    public void testScore() {
        double[] sample = { 4, 10, 15, 45, 19, 50, 70 };
        double[] prediction = { 3, 12, 19, 50, 18, 60, 100 };
        double expected = 12.229939843328282;
        double actual = evaluator.getScore(sample, prediction);
        assertEquals(expected, actual, 0);
    }
}
