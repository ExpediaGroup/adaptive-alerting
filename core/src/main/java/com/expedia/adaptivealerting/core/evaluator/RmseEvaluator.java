package com.expedia.adaptivealerting.core.evaluator;

import com.expedia.adaptivealerting.core.util.MathUtil;

/**
 * Calculates Root-mean-squared error i.e. differences between values (sample and prediction values) predicted by a
 * model.
 * 
 * @author kashah
 *
 */

public class RmseEvaluator implements Evaluator {
    @Override
    public double getScore(double[] actual, double[] prediction) {
        if (actual.length != prediction.length) {
            throw new IllegalArgumentException("actual.length != prediction.length");
        }
        int n = actual.length;
        double resSumSquares = 0.0;
        for (int i = 0; i < n; i++) {
            resSumSquares += MathUtil.getSquare(actual[i] - prediction[i]);
        }
        return MathUtil.roundToThree(MathUtil.getSquareRoot((resSumSquares / n)));
    }

}
