package com.expedia.adaptivealerting.core.evaluator;
import com.expedia.adaptivealerting.core.util.MathUtil;

/**
 * Calculates Root-mean-squared error. https://en.wikipedia.org/wiki/Root-mean-square_deviation.
 * 
 * @author kashah
 *
 */

public class RmseEvaluator implements Evaluator {
    @Override
    public double getScore(double observed, double prediction) {
        double resSumSquares = MathUtil.getSquare(observed - prediction);
        return MathUtil.getSquareRoot(resSumSquares);
    }
}
