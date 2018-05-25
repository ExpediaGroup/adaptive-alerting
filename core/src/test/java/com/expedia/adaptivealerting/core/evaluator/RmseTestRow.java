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
package com.expedia.adaptivealerting.core.evaluator;

import com.opencsv.bean.CsvBindByName;

public class RmseTestRow {

    @CsvBindByName
    private int observed;

    @CsvBindByName
    private double predicted;

    @CsvBindByName
    private double rmse;

    public int getObserved() {
        return observed;
    }

    public void setObserved(int observed) {
        this.observed = observed;
    }

    public double getPredicted() {
        return predicted;
    }

    public void setPredicted(double predicted) {
        this.predicted = predicted;
    }

    public double getRmse() {
        return rmse;
    }

    public void setRmse(double rmse) {
        this.rmse = rmse;
    }
}
