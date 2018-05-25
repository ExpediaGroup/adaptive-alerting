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
        setRmse(Math.sqrt(resSumSquares / n));
    }

    @Override
    public double evaluate() {
        return rmse;
    }

    @Override
    public void reset() {
        this.n = 0;
        this.resSumSquares = 0;
        this.rmse = 0;
    }

    /**
     * @return the rmse
     */
    public double getRmse() {
        return rmse;
    }

    /**
     * @param rmse
     *            the rmse to set
     */
    public void setRmse(double rmse) {
        this.rmse = rmse;
    }

}
