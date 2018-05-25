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
    private double rmse;

    public RmseEvaluator() {
        this.n = 0;
        this.resSumSquares = 0;
        this.rmse = 0;
    }

    @Override
    public void update(double observed, double predicted) {
        double residual = observed - predicted;
        this.resSumSquares += residual * residual;
        this.n += 1;
    }

    @Override
    public double evaluate() {
        this.rmse = Math.sqrt(resSumSquares / n);
        return rmse;
    }

    @Override
    public void reset() {
        this.n = 0;
        this.resSumSquares = 0;
        this.rmse = 0;
    }
}
