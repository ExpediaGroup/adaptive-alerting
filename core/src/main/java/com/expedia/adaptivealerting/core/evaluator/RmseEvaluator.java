/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.core.evaluator;

/**
 * Calculates Root-mean-squared error. https://en.wikipedia.org/wiki/Root-mean-square_deviation.
 */
public class RmseEvaluator implements Evaluator {

    private int n;
    private double resSumSquares;

    /**
     * Creates a new RmseEvaluator. Initial n and residual sum of squares values are set to 0.
     * 
     */
    public RmseEvaluator() {
        reset();
    }

    @Override
    public void update(double observed, double predicted) {
        double residual = observed - predicted;
        this.resSumSquares += residual * residual;
        this.n++;
    }

    @Override
    public ModelEvaluation evaluate() {
        double rmse = Math.sqrt(resSumSquares / n);
        return new ModelEvaluation("rmse", rmse);
    }

    @Override
    public void reset() {
        this.n = 0;
        this.resSumSquares = 0;
    }
}
