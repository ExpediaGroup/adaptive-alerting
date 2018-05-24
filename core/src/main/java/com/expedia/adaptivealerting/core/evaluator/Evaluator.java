package com.expedia.adaptivealerting.core.evaluator;

/**
 * Evaluator interface.
 * 
 * @author kashah
 *
 */

public interface Evaluator {
	double computeScore(double observed, double prediction);
}
