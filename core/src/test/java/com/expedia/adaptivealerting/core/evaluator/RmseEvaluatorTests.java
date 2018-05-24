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

    @Test
    public void testScore() {
        double sample = 50;
        double prediction = 57;
        double expected = 7.0;
        double observed = evaluator.getScore(sample, prediction);
        assertEquals(expected, observed, 0);
    }
}
