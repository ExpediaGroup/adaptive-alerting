package com.expedia.adaptivealerting.core.evaluator;

/**
 * Evaluator interface.
 * 
 * @author kashah
 *
 */

public interface Evaluator {

    /**
     * Updates residual sum of squares and n for a given observed and predicted value.
     *
     * @param observed
     *            Time series value.
     * @param predicted
     *            Model predicted value.
     */
    void update(double observed, double predicted);

    /**
     * Returns RMSE value
     *
     */
    double evaluate();

    /**
     * Resets residual sum of squares and n values to 0
     *
     */
    void reset();
}
