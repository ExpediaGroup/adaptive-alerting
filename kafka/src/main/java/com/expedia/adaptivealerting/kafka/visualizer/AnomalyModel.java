/*
 * Copyright 2018-2019 Expedia Group, Inc.
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
