package com.expedia.adaptivealerting.core.evaluator;

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
            resSumSquares += Math.pow(actual[i] - prediction[i], 2);
        }
        return roundToThree(Math.sqrt(resSumSquares / n));
    }

    private double roundToThree(double value) {
        return (Math.round(value * 1000) / 1000);
    }
}
