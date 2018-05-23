package com.expedia.adaptivealerting.core.evaluator;

/**
 * Evaluator interface.
 * 
 * @author kashah
 *
 */

public interface Evaluator {
	double getScore(double[] actual, double[] prediction);
}
