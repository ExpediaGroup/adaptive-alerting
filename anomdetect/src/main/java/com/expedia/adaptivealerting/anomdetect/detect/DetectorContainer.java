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
package com.expedia.adaptivealerting.anomdetect.detect;

import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;

import java.util.List;
import java.util.UUID;

public class DetectorContainer {
    private final Detector detector;
    private final List<DetectionFilter> filters;

    public DetectorContainer(Detector detector, List<DetectionFilter> filters) {
        this.detector = detector;
        this.filters = filters;
    }

    public Detector getDetector() {
        return this.detector;
    }

    public List<DetectionFilter> getFilters() {
        return this.filters;
    }

    public UUID getUuid() {
        return this.detector.getUuid();
    }

    public String getName() {
        return this.detector.getName();
    }
}
