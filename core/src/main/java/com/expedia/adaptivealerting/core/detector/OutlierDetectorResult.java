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
package com.expedia.adaptivealerting.core.detector;

/**
 * Outlier detector result.
 *
 * @author Willie Wheeler
 */
public class OutlierDetectorResult {
    private Long epochSecond;
    private Double observed;
    private Double predicted;
    private Double weakThresholdUpper;
    private Double weakThresholdLower;
    private Double strongThresholdUpper;
    private Double strongThresholdLower;
    private Double outlierScore;
    private OutlierLevel outlierLevel;
    
    public Long getEpochSecond() {
        return epochSecond;
    }
    
    public void setEpochSecond(Long epochSecond) {
        this.epochSecond = epochSecond;
    }
    
    public Double getObserved() {
        return observed;
    }
    
    public void setObserved(Double observed) {
        this.observed = observed;
    }
    
    public Double getPredicted() {
        return predicted;
    }
    
    public void setPredicted(Double predicted) {
        this.predicted = predicted;
    }
    
    public Double getWeakThresholdUpper() {
        return weakThresholdUpper;
    }
    
    public void setWeakThresholdUpper(Double weakThresholdUpper) {
        this.weakThresholdUpper = weakThresholdUpper;
    }
    
    public Double getWeakThresholdLower() {
        return weakThresholdLower;
    }
    
    public void setWeakThresholdLower(Double weakThresholdLower) {
        this.weakThresholdLower = weakThresholdLower;
    }
    
    public Double getStrongThresholdUpper() {
        return strongThresholdUpper;
    }
    
    public void setStrongThresholdUpper(Double strongThresholdUpper) {
        this.strongThresholdUpper = strongThresholdUpper;
    }
    
    public Double getStrongThresholdLower() {
        return strongThresholdLower;
    }
    
    public void setStrongThresholdLower(Double strongThresholdLower) {
        this.strongThresholdLower = strongThresholdLower;
    }
    
    public Double getOutlierScore() {
        return outlierScore;
    }
    
    public void setOutlierScore(Double outlierScore) {
        this.outlierScore = outlierScore;
    }
    
    public OutlierLevel getOutlierLevel() {
        return outlierLevel;
    }
    
    public void setOutlierLevel(OutlierLevel outlierLevel) {
        this.outlierLevel = outlierLevel;
    }
    
    @Override
    public String toString() {
        return "OutlierDetectorResult{" +
                "epochSecond=" + epochSecond +
                ", observed=" + observed +
                ", predicted=" + predicted +
                ", weakThresholdUpper=" + weakThresholdUpper +
                ", weakThresholdLower=" + weakThresholdLower +
                ", strongThresholdUpper=" + strongThresholdUpper +
                ", strongThresholdLower=" + strongThresholdLower +
                ", outlierScore=" + outlierScore +
                ", outlierLevel=" + outlierLevel +
                '}';
    }
}
