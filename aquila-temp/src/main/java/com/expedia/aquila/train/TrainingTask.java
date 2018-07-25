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
package com.expedia.aquila.train;

import com.expedia.adaptivealerting.core.data.Metric;

import java.io.File;

// TODO Eventually this will be more general:
// - general data source
// - general model sink
// - general algo params
// Training tasks will go in AA proper. [WLW]

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class TrainingTask {
    private Metric metric;
    private TrainingParams params;
    
    public TrainingTask(Metric metric, TrainingParams params) {
        this.metric = metric;
        this.params = params;
    }
    
    public Metric getMetric() {
        return metric;
    }
    
    public TrainingParams getParams() {
        return params;
    }
}
