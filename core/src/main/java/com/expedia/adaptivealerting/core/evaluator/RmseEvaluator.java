package com.expedia.adaptivealerting.core.evaluator;

import com.expedia.adaptivealerting.core.util.MathUtil;

/**
 * Calculates Root-mean-squared error. https://en.wikipedia.org/wiki/Root-mean-square_deviation.
 * 
 * @author kashah
 *
 */

public class RmseEvaluator implements Evaluator {

    private int n;
    private double resSumSquares;

    public RmseEvaluator(int n, double resSumSquares) {
        this.n = n;
        this.resSumSquares = resSumSquares;
    }

    @Override
    public double computeScore(double observed, double predicted) {
        this.resSumSquares += MathUtil.getSquare(observed - predicted);
        this.n += 1;
        return MathUtil.getSquareRoot(resSumSquares / n);
    }
}
