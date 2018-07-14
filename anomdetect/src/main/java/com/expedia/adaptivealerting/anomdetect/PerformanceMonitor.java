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
import com.expedia.adaptivealerting.core.evaluator.Evaluator;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.core.util.AssertUtil;

/**
 * <p>
 * Performance monitor to track performance of a given series. Returns a performance score and resets the evaluator
 * every nth ticks where n is a user defined value.
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
     * Local evaluator.
     */
    private Evaluator evaluator;

    /**
     * Local performance monitor listener.
     */
    private PerfMonListener listener;

    /**
     * Local max no ticks where performance monitor resets evaluator.
     */

    private int maxTicks;

    /**
     * Creates a new performance monitor and sets evaluator as RMSE evaluator
     */
    public PerformanceMonitor(PerfMonListener listener, Evaluator evaluator, int maxTicks) {
        AssertUtil.notNull(listener, "Listener can't be null");
        AssertUtil.notNull(evaluator, "Evaluator can't be null");
        AssertUtil.isTrue(maxTicks > 0, "Max ticks should be greather than 0");

        this.listener = listener;
        this.evaluator = new RmseEvaluator();
        this.maxTicks = maxTicks;
        resetCounter();
    }

    public double evaluatePerformance(AnomalyResult result) {
        double observed = result.getObserved();
        double predicted = result.getPredicted();
        evaluator.update(observed, predicted);
        double evaluatorScore = evaluator.evaluate().getEvaluatorScore();

        if (tickCounter >= maxTicks) {
            listener.processScore(evaluatorScore);
            evaluator.reset();
            resetCounter();
            return evaluatorScore;
        } else {
            this.tickCounter++;
            return evaluatorScore;
        }
    }

    private void resetCounter() {
        this.tickCounter = 0;
    }

}
