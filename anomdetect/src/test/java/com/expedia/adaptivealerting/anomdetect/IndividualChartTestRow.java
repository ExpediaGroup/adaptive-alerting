/*
 * Copyright 2018 Expedia Group, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.expedia.adaptivealerting.anomdetect;

import com.opencsv.bean.CsvBindByName;

/**
 * The type Individual chart test row.
 *
 * @author shsethi
 */
public class IndividualChartTestRow {

    @CsvBindByName
    private int sample;

    @CsvBindByName
    private double observed;

    @CsvBindByName
    private double upperControlLimit_R;

    @CsvBindByName
    private double upperControlLimit_X;

    @CsvBindByName
    private double lowerControlLimit_X;

    @CsvBindByName
    private double target;

    @CsvBindByName
    private String anomalyLevel;

    /**
     * Gets sample.
     *
     * @return the sample
     */
    public int getSample() {
        return sample;
    }

    /**
     * Sets sample.
     *
     * @param sample the sample
     */
    public void setSample(int sample) {
        this.sample = sample;
    }

    /**
     * Gets observed.
     *
     * @return the observed
     */
    public double getObserved() {
        return observed;
    }

    /**
     * Sets observed.
     *
     * @param observed the observed
     */
    public void setObserved(double observed) {
        this.observed = observed;
    }

    /**
     * Gets upper control limit r.
     *
     * @return the upper control limit r
     */
    public double getUpperControlLimit_R() {
        return upperControlLimit_R;
    }

    /**
     * Sets upper control limit r.
     *
     * @param upperControlLimit_R the upper control limit r
     */
    public void setUpperControlLimit_R(double upperControlLimit_R) {
        this.upperControlLimit_R = upperControlLimit_R;
    }

    /**
     * Gets upper control limit x.
     *
     * @return the upper control limit x
     */
    public double getUpperControlLimit_X() {
        return upperControlLimit_X;
    }

    /**
     * Sets upper control limit x.
     *
     * @param upperControlLimit_X the upper control limit x
     */
    public void setUpperControlLimit_X(double upperControlLimit_X) {
        this.upperControlLimit_X = upperControlLimit_X;
    }

    /**
     * Gets lower control limit x.
     *
     * @return the lower control limit x
     */
    public double getLowerControlLimit_X() {
        return lowerControlLimit_X;
    }

    /**
     * Sets lower control limit x.
     *
     * @param lowerControlLimit_X the lower control limit x
     */
    public void setLowerControlLimit_X(double lowerControlLimit_X) {
        this.lowerControlLimit_X = lowerControlLimit_X;
    }

    /**
     * Gets target.
     *
     * @return the target
     */
    public double getTarget() {
        return target;
    }

    /**
     * Sets target.
     *
     * @param target the target
     */
    public void setTarget(double target) {
        this.target = target;
    }

    /**
     * Gets anomaly level.
     *
     * @return the anomaly level
     */
    public String getAnomalyLevel() {
        return anomalyLevel;
    }

    /**
     * Sets anomaly level.
     *
     * @param anomalyLevel the anomaly level
     */
    public void setAnomalyLevel(String anomalyLevel) {
        this.anomalyLevel = anomalyLevel;
    }
}
