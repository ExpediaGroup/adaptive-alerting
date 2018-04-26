package com.expedia.haystack.adaptivealerting.anomaly;

import com.expedia.haystack.adaptivealerting.anomaly.util.AssertUtil;

import java.time.Instant;

/**
 * Outlier detector with one-sided constant thresholds for small and large outliers.
 *
 * @author Willie Wheeler
 */
public class ConstantThresholdAnomalyDetector implements AnomalyDetector {
    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    
    private final int tail;
    private final double smallThreshold;
    private final double largeThreshold;
    
    /**
     * Builds a constant-threshold anomaly detector that performs anomaly tests in the specified tail. For left-tailed
     * detectors we expect largeThreshold &lt;= smallThreshold, and for right-tailed detectors we expect
     * smallThreshold &lt;= largeThreshold.
     *
     * @param tail           Either LEFT_TAILED or RIGHT_TAILED.
     * @param smallThreshold Small anomaly threshold.
     * @param largeThreshold Large anomaly threshold.
     */
    public ConstantThresholdAnomalyDetector(int tail, double smallThreshold, double largeThreshold) {
        if (tail == LEFT_TAILED) {
            AssertUtil.isTrue(largeThreshold <= smallThreshold,
                    "Left-tailed detector requires largeThreshold <= smallThreshold");
        } else if (tail == RIGHT_TAILED) {
            AssertUtil.isTrue(smallThreshold <= largeThreshold,
                    "Right-tailed detector required smallThreshold <= largeThreshold");
        } else {
            throw new IllegalArgumentException("tail must be either LEFT_TAILED or RIGHT_TAILED");
        }
        
        this.tail = tail;
        this.smallThreshold = smallThreshold;
        this.largeThreshold = largeThreshold;
    }
    
    public int getTail() {
        return tail;
    }
    
    public Double getSmallThreshold() {
        return smallThreshold;
    }
    
    public Double getLargeThreshold() {
        return largeThreshold;
    }
    
    @Override
    public AnomalyLevel evaluate(Instant instant, double value) {
        return tail == LEFT_TAILED ? evaluateLeftTailed(value) : evaluateRightTailed(value);
    }
    
    private AnomalyLevel evaluateLeftTailed(double value) {
        if (value <= largeThreshold) {
            return AnomalyLevel.LARGE;
        } else if (value <= smallThreshold) {
            return AnomalyLevel.SMALL;
        } else {
            return AnomalyLevel.NORMAL;
        }
    }
    
    private AnomalyLevel evaluateRightTailed(double value) {
        if (value >= largeThreshold) {
            return AnomalyLevel.LARGE;
        } else if (value >= smallThreshold) {
            return AnomalyLevel.SMALL;
        } else {
            return AnomalyLevel.NORMAL;
        }
    }
}
