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
/**
 * 
 */
package com.expedia.adaptivealerting.tools.pipeline.filter;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.evaluator.Evaluator;
import com.expedia.adaptivealerting.core.evaluator.ModelEvaluation;
import com.expedia.adaptivealerting.tools.pipeline.StreamPublisherSupport;
import com.expedia.adaptivealerting.tools.pipeline.StreamSubscriber;

/**
 * Stream filter that applies model evaluator to metrics and publishes the score.
 * 
 * @author kashah
 *
 */
public class EvaluatorStreamFilter implements StreamSubscriber<AnomalyResult> {

    private final Evaluator evaluator;
    private final StreamPublisherSupport<ModelEvaluation> publisherSupport = new StreamPublisherSupport<>();

    public EvaluatorStreamFilter(Evaluator evaluator) {
        notNull(evaluator, "evaluator can't be null");
        this.evaluator = evaluator;
    }

    @Override
    public void next(AnomalyResult anamolyResult) {
        notNull(anamolyResult, "anamolyResult can't be null");
        evaluator.update(anamolyResult.getObserved(), anamolyResult.getPredicted());
        publisherSupport.publish(evaluator.evaluate());
    }

}
