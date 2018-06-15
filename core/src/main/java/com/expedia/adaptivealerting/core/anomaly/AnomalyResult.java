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

import com.expedia.adaptivealerting.core.metric.Metric;
import com.expedia.www.haystack.commons.entities.MetricPoint;

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
    private Metric metric;
    private List<InvestigationResult> investigationResults;

    /**
     * Param that is used for differentiating AnomalyResults. This will likely be expanded in future to provide more
     * information so it shouldn't be relied upon to contain stable information.
     */
    private String detectorLabel;

    public  AnomalyResult() {
        // For jackson
    }


    /**
     * Convenience constructor for assigning a detector label based on the Class of the detector and Metric from a
     * MetricPoint.
     *
     * @param detector The AnomalyDetector that created this AnomalyResult
     */
    public AnomalyResult(Class detector, MetricPoint metricPoint) {
        this.detectorLabel = detector.getSimpleName();
        this.metric = new Metric(metricPoint);
    }
    
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

    public String getDetectorLabel() {
        return detectorLabel;
    }

    public void setDetectorLabel(String detectorLabel) {
        this.detectorLabel = detectorLabel;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
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
                ", detectorLabel=" + detectorLabel +
                ", metric=" + (metric == null ? "null" : metric.getName()) +
                '}';
    }
}
