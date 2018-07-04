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
import java.util.Map;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Performance monitor which generates a result every 100 ticks and resets evaluator. Currently it uses only RMSE
 * evaluator.
 * </p>
 * 
 * @author kashah
 */
public class PerformanceMonitor {

    private Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitor.class);
    private int tickCounter = 0;
    private RmseEvaluator evaluator;
    private Map<String, Double> perfLookup;
    public static final int MAX_TICKS = 100;
    public static final double RMSE_THRESHOLD = 3.0;

    /**
     * Creates a new performance monitor and sets evaluator as RMSE evaluator
     */
    public PerformanceMonitor() {
        this.evaluator = new RmseEvaluator();
        this.perfLookup = new LinkedHashMap<String, Double>();
        perfLookup.put("rmse", RMSE_THRESHOLD);
    }

    public void update(AnomalyResult result) {
        double observed = result.getObserved();
        double predicted = result.getPredicted();
        evaluator.update(observed, predicted);
        this.tickCounter++;
    }

    public boolean rebuildModel() {
        double evaluatorScore = evaluator.evaluate().getEvaluatorScore();
        if (tickCounter >= MAX_TICKS) {
            double lookupEvaluatorScore = perfLookup.get("rmse").doubleValue();
            if (evaluatorScore > lookupEvaluatorScore) {
                LOGGER.info("Need to rebuild this model");
                return true;
            }
            evaluator.reset();
            reset();
        }
        return false;
    }

    private void reset() {
        this.tickCounter = 0;
    }
 
}
