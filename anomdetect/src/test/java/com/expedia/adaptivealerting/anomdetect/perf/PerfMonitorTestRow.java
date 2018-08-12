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
package com.expedia.adaptivealerting.anomdetect.perf;

import com.opencsv.bean.CsvBindByName;

/**
 * @author kashah
 *
 */
public class PerfMonitorTestRow {

    @CsvBindByName
    private double observed;

    @CsvBindByName
    private double predicted;

    @CsvBindByName
    private double score;

    /**
     * @return the observed
     */
    public double getObserved() {
        return observed;
    }

    /**
     * @param observed
     *            the observed to set
     */
    public void setObserved(double observed) {
        this.observed = observed;
    }

    /**
     * @return the predicted
     */
    public double getPredicted() {
        return predicted;
    }

    /**
     * @param predicted
     *            the predicted to set
     */
    public void setPredicted(double predicted) {
        this.predicted = predicted;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @param score
     *            the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }

}
