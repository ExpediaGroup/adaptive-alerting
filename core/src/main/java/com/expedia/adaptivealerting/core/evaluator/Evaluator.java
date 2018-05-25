package com.expedia.adaptivealerting.core.evaluator;

/**
 * Evaluator interface.
 * 
 * @author kashah
 *
 */

public interface Evaluator {
	void update(double observed, double prediction);
	double evaluate();
	void reset();
}
