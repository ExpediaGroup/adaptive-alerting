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
package com.expedia.adaptivealerting.tools.pipeline.filter;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.evaluator.Evaluator;
import com.expedia.adaptivealerting.core.evaluator.ModelEvaluation;
import com.expedia.adaptivealerting.tools.pipeline.util.AnomalyResultSubscriber;
import com.expedia.adaptivealerting.tools.pipeline.util.ModelEvaluationSubscriber;

import java.util.LinkedList;
import java.util.List;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Stream filter that applies model evaluator to metrics and publishes the score.
 */
public final class EvaluatorFilter implements AnomalyResultSubscriber {
    private final Evaluator evaluator;
    private final List<ModelEvaluationSubscriber> subscribers = new LinkedList<>();
    
    public EvaluatorFilter(Evaluator evaluator) {
        notNull(evaluator, "evaluator can't be null");
        this.evaluator = evaluator;
    }
    
    @Override
    public void next(AnomalyResult anomalyResult) {
        notNull(anomalyResult, "anomalyResult can't be null");
        // getPredicted() can return null during warm up; convert null to 0
        evaluator.update(anomalyResult.getMetricData().getValue(), getPredicted(anomalyResult));
        publish(evaluator.evaluate());
    }
    
    public void addSubscriber(ModelEvaluationSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.add(subscriber);
    }
    
    public void removeSubscriber(ModelEvaluationSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.remove(subscriber);
    }
    
    private Double getPredicted(AnomalyResult anomalyResult) {
        // getPredicted() can return null during warm up; convert null to 0
        return (anomalyResult.getPredicted() == null ? 0 : anomalyResult.getPredicted());
    }

    private void publish(ModelEvaluation modelEvaluation) {
        subscribers.stream().forEach(subscriber -> subscriber.next(modelEvaluation));
    }
}
