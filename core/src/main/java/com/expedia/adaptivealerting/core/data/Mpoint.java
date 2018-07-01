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
package com.expedia.adaptivealerting.core.data;

/**
 * Metric point.
 *
 * @author Willie Wheeler
 */
public final class Mpoint {
    private Metric metric;
    
    // TODO Prefer Instant and Double, but trying not to diverge much from Haystack's MetricPoint for now. [WLW]
    private long epochTimeInSeconds;
    private Float value;
    
    public Metric getMetric() {
        return metric;
    }
    
    public void setMetric(Metric metric) {
        this.metric = metric;
    }
    
    public long getEpochTimeInSeconds() {
        return epochTimeInSeconds;
    }
    
    public void setEpochTimeInSeconds(long epochTimeInSeconds) {
        this.epochTimeInSeconds = epochTimeInSeconds;
    }
    
    public Float getValue() {
        return value;
    }
    
    public void setValue(Float value) {
        this.value = value;
    }
}
