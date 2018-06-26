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

import com.opencsv.bean.CsvBindByName;

/**
 * @author kashah
 *
 */
public class CusumTestRow {

    @CsvBindByName
    private int sample;

    @CsvBindByName
    private double observed;

    @CsvBindByName
    private double sh;

    @CsvBindByName
    private double sl;

    @CsvBindByName(column = "stdev")
    private double stdDev;

    @CsvBindByName
    private String level;

    /**
     * @return the sample
     */
    public int getSample() {
        return sample;
    }

    /**
     * @param sample
     *            the sample to set
     */
    public void setSample(int sample) {
        this.sample = sample;
    }

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
     * @return the sh
     */
    public double getSh() {
        return sh;
    }

    /**
     * @param sh
     *            the sh to set
     */
    public void setSh(double sh) {
        this.sh = sh;
    }

    /**
     * @return the sl
     */
    public double getSl() {
        return sl;
    }

    /**
     * @param sl
     *            the sl to set
     */
    public void setSl(double sl) {
        this.sl = sl;
    }

    /**
     * @return the stdDev
     */
    public double getStdDev() {
        return stdDev;
    }

    /**
     * @param stdDev
     *            the stdDev to set
     */
    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

}
