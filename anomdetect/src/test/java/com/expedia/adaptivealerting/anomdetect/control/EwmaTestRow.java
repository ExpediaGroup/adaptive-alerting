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
package com.expedia.adaptivealerting.anomdetect.control;

import com.opencsv.bean.CsvBindByName;

public class EwmaTestRow {
    
    @CsvBindByName
    private String date;
    
    @CsvBindByName
    private int observed;
    
    @CsvBindByName
    private double mean;
    
    @CsvBindByName(column = "known.mean")
    private double knownMean;
    
    @CsvBindByName
    private double var;
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public int getObserved() {
        return observed;
    }
    
    public void setObserved(int observed) {
        this.observed = observed;
    }
    
    public double getMean() {
        return mean;
    }
    
    public void setMean(double mean) {
        this.mean = mean;
    }
    
    public double getKnownMean() {
        return knownMean;
    }
    
    public void setKnownMean(double knownMean) {
        this.knownMean = knownMean;
    }
    
    public double getVar() {
        return var;
    }
    
    public void setVar(double var) {
        this.var = var;
    }
}
