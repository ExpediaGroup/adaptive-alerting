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
import lombok.Generated;

@Generated
public class AnomalyModel extends Object {

    private String key;
    private double value;
    private String level;
    private String uuid;
    private String timestamp;
    private AnomalyThresholds anomalyThresholds;
    private TagCollection tags;

    private AnomalyModel(Builder builder){
        this.key = builder.key;
        this.value = builder.value;
        this.level = builder.level;
        this.uuid = builder.uuid;
        this.timestamp = builder.timestamp;
        this.anomalyThresholds = builder.anomalyThresholds;
        this.tags = builder.tags;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(AnomalyModel copy) {
        Builder builder = new Builder();
        builder.key = copy.getKey();
        builder.value = copy.getValue();
        builder.level = copy.getLevel();
        builder.uuid = copy.getUuid();
        builder.timestamp = copy.getTimestamp();
        builder.anomalyThresholds = copy.getAnomalyThresholds();
        builder.tags = copy.getTags();
        return builder;
    }

    public String getKey() {
        return key;
    }

    public String getLevel() {
        return level;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public AnomalyThresholds getAnomalyThresholds() {
        return anomalyThresholds;
    }

    public TagCollection getTags() {
        return tags;
    }

    public double getValue() {
        return value;
    }

    public static final class Builder {
        private String key;
        private double value;
        private String level;
        private String uuid;
        private String timestamp;
        private AnomalyThresholds anomalyThresholds;
        private TagCollection tags;

        private Builder() {
        }

        public Builder key(String val) {
            key = val;
            return this;
        }

        public Builder value(double val) {
            value = val;
            return this;
        }

        public Builder level(String val) {
            level = val;
            return this;
        }

        public Builder uuid(String val) {
            uuid = val;
            return this;
        }

        public Builder timestamp(String val) {
            timestamp = val;
            return this;
        }

        public Builder anomalyThresholds(AnomalyThresholds val) {
            anomalyThresholds = val;
            return this;
        }

        public Builder tags(TagCollection val) {
            tags = val;
            return this;
        }

        public AnomalyModel build() {
            return new AnomalyModel(this);
        }
    }
}
