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
 * Model Evaluation
 * 
 * @author kashah
 */
public class ModelEvaluation {

    private String evaluatorMethod;
    private double evaluatorScore;

    public ModelEvaluation(String method, double score) {
        this.evaluatorMethod = method;
        this.evaluatorScore = score;
    }

    /**
     * @return the evaluatorScore
     */
    public String getEvaluatorMethod() {
        return evaluatorMethod;
    }

    /**
     * @return the evaluatorScore
     */
    public double getEvaluatorScore() {
        return evaluatorScore;
    }

}
