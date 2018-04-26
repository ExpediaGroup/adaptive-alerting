package com.expedia.haystack.adaptivealerting.outlier;

import com.expedia.haystack.adaptivealerting.OutlierDetector;
import com.expedia.haystack.adaptivealerting.OutlierLevel;
import com.expedia.haystack.adaptivealerting.util.AssertUtil;

import java.time.Instant;

/**
 * Outlier detector with one-sided constant thresholds for small and large outliers.
 *
 * @author Willie Wheeler
 */
public class ConstantThresholdOutlierDetector implements OutlierDetector {
    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    
    private final int tail;
    private final double smallThreshold;
    private final double largeThreshold;
    
    /**
     * Builds a constant-threshold outlier detector that performs outlier tests in the specified tail. For left-tailed
     * detectors we expect largeThreshold &lt;= smallThreshold, and for right-tailed detectors we expect
     * smallThreshold &lt;= largeThreshold.
     *
     * @param tail           Either LEFT_TAILED or RIGHT_TAILED.
     * @param smallThreshold Small outlier threshold.
     * @param largeThreshold Large outlier threshold.
     */
    public ConstantThresholdOutlierDetector(int tail, double smallThreshold, double largeThreshold) {
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
    public OutlierLevel evaluate(Instant instant, double value) {
        return tail == LEFT_TAILED ? evaluateLeftTailed(value) : evaluateRightTailed(value);
    }
    
    private OutlierLevel evaluateLeftTailed(double value) {
        if (value <= largeThreshold) {
            return OutlierLevel.LARGE;
        } else if (value <= smallThreshold) {
            return OutlierLevel.SMALL;
        } else {
            return OutlierLevel.NORMAL;
        }
    }
    
    private OutlierLevel evaluateRightTailed(double value) {
        if (value >= largeThreshold) {
            return OutlierLevel.LARGE;
        } else if (value >= smallThreshold) {
            return OutlierLevel.SMALL;
        } else {
            return OutlierLevel.NORMAL;
        }
    }
}
