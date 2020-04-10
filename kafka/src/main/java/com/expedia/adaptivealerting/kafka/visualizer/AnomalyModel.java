package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.metrics.TagCollection;

import java.util.Date;

public class AnomalyModel {

    String key;
    String level;
    String uuid;
    Date timestamp;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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
}
