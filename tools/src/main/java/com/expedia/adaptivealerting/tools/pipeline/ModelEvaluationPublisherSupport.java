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
package com.expedia.adaptivealerting.tools.pipeline;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

import java.util.LinkedList;
import java.util.List;
import com.expedia.adaptivealerting.core.evaluator.ModelEvaluation;

/**
 * @author kashah
 */
public class ModelEvaluationPublisherSupport {

    private final List<ModelEvaluationSubscriber> subscribers = new LinkedList<>();

    public void addSubscriber(ModelEvaluationSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.add(subscriber);
    }

    public void removeSubscriber(ModelEvaluationSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.remove(subscriber);
    }

    public void publish(ModelEvaluation modelEvaluation) {
        notNull(modelEvaluation, "modelEvaluation can't be null");
        subscribers.stream().forEach(subscriber -> subscriber.next(modelEvaluation));
    }
}
