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
package com.expedia.adaptivealerting.core.anomaly;

import com.expedia.adaptivealerting.core.metrics.MetricDefinition;

import java.util.List;

/**
 * Anomaly result.
 *
 * @author Willie Wheeler
 */
public class AnomalyResult {
    private Long epochSecond;
    private Double observed;
    private Double predicted;
    private Double weakThresholdUpper;
    private Double weakThresholdLower;
    private Double strongThresholdUpper;
    private Double strongThresholdLower;
    private Double anomalyScore;
    private AnomalyLevel anomalyLevel;
    private MetricDefinition metricDefinition;
    private List<InvestigationResult> investigationResults;

    /**
     * Param that is used for identifying the AnomalyDetector that created this AnomalyResult.
     */
    private String detectorId;
    
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
    
    public Double getAnomalyScore() {
        return anomalyScore;
    }
    
    public void setAnomalyScore(Double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }
    
    public AnomalyLevel getAnomalyLevel() {
        return anomalyLevel;
    }
    
    public void setAnomalyLevel(AnomalyLevel anomalyLevel) {
        this.anomalyLevel = anomalyLevel;
    }

    public String getDetectorId() {
        return detectorId;
    }

    public void setDetectorId(String detectorId) {
        this.detectorId = detectorId;
    }

    public MetricDefinition getMetricDefinition() {
        return metricDefinition;
    }

    public void setMetricDefinition(MetricDefinition metricDefinition) {
        this.metricDefinition = metricDefinition;
    }

    public List<InvestigationResult> getInvestigationResults() {
        return investigationResults;
    }

    public void setInvestigationResults(List<InvestigationResult> investigationResults) {
        this.investigationResults = investigationResults;
    }

    @Override
    public String toString() {
        return "AnomalyResult{" +
                "epochSecond=" + epochSecond +
                ", observed=" + observed +
                ", predicted=" + predicted +
                ", weakThresholdUpper=" + weakThresholdUpper +
                ", weakThresholdLower=" + weakThresholdLower +
                ", strongThresholdUpper=" + strongThresholdUpper +
                ", strongThresholdLower=" + strongThresholdLower +
                ", anomalyScore=" + anomalyScore +
                ", anomalyLevel=" + anomalyLevel +
                ", detectorId=" + detectorId +
                '}';
    }
}
