package com.expedia.adaptivealerting.core.evaluator;

/**
 * Calculates Root-mean-squared error. https://en.wikipedia.org/wiki/Root-mean-square_deviation.
 * 
 * @author kashah
 *
 */

public class RmseEvaluator implements Evaluator {

    private int n;
    private double resSumSquares;

    /**
     * Creates a new RmseEvaluator. Initial n and residual sum of squares values are set to 0.
     * 
     */
    public RmseEvaluator() {
        this.n = 0;
        this.resSumSquares = 0;
    }

    @Override
    public void update(double observed, double predicted) {
        double residual = observed - predicted;
        this.resSumSquares += residual * residual;
        this.n++;
    }

    @Override
    public double evaluate() {
        return Math.sqrt(resSumSquares / n);
    }

    @Override
    public void reset() {
        this.n = 0;
        this.resSumSquares = 0;
    }
}
