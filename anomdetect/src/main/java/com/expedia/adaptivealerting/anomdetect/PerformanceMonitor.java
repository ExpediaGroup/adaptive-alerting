/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;

/**
 * <p>
 * Performance monitor which generates a result every 100 ticks and resets evaluator. Currently it uses only RMSE
 * evaluator.
 * </p>
 * 
 * @author kashah
 */
public class PerformanceMonitor {

    /**
     * Local tick counter.
     */
    private int tickCounter;

    /**
     * Local RMSE evaluator.
     */
    private RmseEvaluator evaluator;

    /**
     * Local threshold lookup where we maintain thresholds for different evaluators.
     */

    public static final int MAX_TICKS = 100;

    /**
     * Creates a new performance monitor and sets evaluator as RMSE evaluator
     */
    public PerformanceMonitor() {
        this.evaluator = new RmseEvaluator();
        resetCounter();
    }

    public double evaluatePerformance(AnomalyResult result) {
        double observed = result.getObserved();
        double predicted = result.getPredicted();
        double evaluatorScore = 0;
        evaluator.update(observed, predicted);

        if (tickCounter >= MAX_TICKS) {
            evaluatorScore = evaluator.evaluate().getEvaluatorScore();
            evaluator.reset();
            resetCounter();
        }
        this.tickCounter++;
        return evaluatorScore;
    }

    private void resetCounter() {
        this.tickCounter = 0;
    }

}
