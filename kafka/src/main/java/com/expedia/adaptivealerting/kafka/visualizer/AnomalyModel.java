package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.metrics.TagCollection;

public class AnomalyModel {

    String key;
    double value;
    String level;
    String uuid;
    String timestamp;
    AnomalyThresholds anomalyThresholds;
    TagCollection tags;


    public AnomalyModel() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public AnomalyThresholds getAnomalyThresholds() {
        return anomalyThresholds;
    }

    public void setAnomalyThresholds(AnomalyThresholds anomalyThresholds) {
        this.anomalyThresholds = anomalyThresholds;
    }

    public TagCollection getTags() {
        return tags;
    }

    public void setTags(TagCollection tags) {
        this.tags = tags;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
