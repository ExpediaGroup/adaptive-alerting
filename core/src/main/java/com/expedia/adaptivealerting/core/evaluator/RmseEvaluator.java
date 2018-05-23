package com.expedia.adaptivealerting.core.evaluator;

import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.core.util.MathUtil;

/**
 * Calculates Root-mean-squared error. https://en.wikipedia.org/wiki/Root-mean-square_deviation.
 * 
 * @author kashah
 *
 */

public class RmseEvaluator implements Evaluator {
    @Override
    public double getScore(double[] actual, double[] prediction) {
        AssertUtil.isTrue(actual.length == prediction.length, "actual.length != prediction.length");
        int n = actual.length;
        double resSumSquares = 0.0;
        for (int i = 0; i < n; i++) {
            resSumSquares += MathUtil.getSquare(actual[i] - prediction[i]);
        }
        return MathUtil.getSquareRoot((resSumSquares / n));
    }

}
