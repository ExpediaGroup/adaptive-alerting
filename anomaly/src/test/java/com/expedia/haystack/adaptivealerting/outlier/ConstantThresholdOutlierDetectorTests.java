package com.expedia.haystack.adaptivealerting.outlier;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static com.expedia.haystack.adaptivealerting.OutlierLevel.*;
import static com.expedia.haystack.adaptivealerting.outlier.ConstantThresholdOutlierDetector.LEFT_TAILED;
import static com.expedia.haystack.adaptivealerting.outlier.ConstantThresholdOutlierDetector.RIGHT_TAILED;
import static junit.framework.TestCase.assertEquals;

/**
 * @author Willie Wheeler
 */
public class ConstantThresholdOutlierDetectorTests {
    private Instant instant;
    
    @Before
    public void setUp() {
        this.instant = Instant.now();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateTail() {
        new ConstantThresholdOutlierDetector(-1, 0.0, 1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLeftTailedThresholds() {
        new ConstantThresholdOutlierDetector(LEFT_TAILED, 10.0, 30.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateRightTailedThresholds() {
        new ConstantThresholdOutlierDetector(RIGHT_TAILED, 30.0, 10.0);
    }
    
    @Test
    public void testAccessors() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, 30.0, 60.0);
        assertEquals(RIGHT_TAILED, detector.getTail());
        assertEquals(30.0, detector.getSmallThreshold());
        assertEquals(60.0, detector.getLargeThreshold());
    }
    
    @Test
    public void testEvaluateLeftTailed_positiveThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(LEFT_TAILED, 300.0, 100.0);
        assertEquals(NORMAL, detector.evaluate(instant, 500.0));
        assertEquals(SMALL, detector.evaluate(instant, 300.0));
        assertEquals(SMALL, detector.evaluate(instant, 200.0));
        assertEquals(LARGE, detector.evaluate(instant, 100.0));
        assertEquals(LARGE, detector.evaluate(instant, 50.0));
    }
    
    @Test
    public void testEvaluateLeftTailed_negativeThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(LEFT_TAILED, -10.0, -30.0);
        assertEquals(NORMAL, detector.evaluate(instant, 1.0));
        assertEquals(SMALL, detector.evaluate(instant, -10.0));
        assertEquals(SMALL, detector.evaluate(instant, -15.0));
        assertEquals(LARGE, detector.evaluate(instant, -30.0));
        assertEquals(LARGE, detector.evaluate(instant, -50.0));
    }
    
    @Test
    public void testEvaluateRightTailed_positiveThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, 200.0, 300.0);
        assertEquals(NORMAL, detector.evaluate(instant, 100.0));
        assertEquals(SMALL, detector.evaluate(instant, 200.0));
        assertEquals(SMALL, detector.evaluate(instant, 220.0));
        assertEquals(LARGE, detector.evaluate(instant, 300.0));
        assertEquals(LARGE, detector.evaluate(instant, 8675309.0));
    }
    
    @Test
    public void testEvaluateRightTailed_negativeThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, -300.0, -100.0);
        assertEquals(NORMAL, detector.evaluate(instant, -1000.0));
        assertEquals(SMALL, detector.evaluate(instant, -300.0));
        assertEquals(SMALL, detector.evaluate(instant, -250.0));
        assertEquals(LARGE, detector.evaluate(instant, -100.0));
        assertEquals(LARGE, detector.evaluate(instant, 0.0));
    }
}
