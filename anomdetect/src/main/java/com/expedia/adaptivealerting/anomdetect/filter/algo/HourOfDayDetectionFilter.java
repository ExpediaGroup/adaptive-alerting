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
package com.expedia.adaptivealerting.anomdetect.filter.algo;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.expedia.adaptivealerting.anomdetect.util.DateUtil;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.ZonedDateTime;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.anomdetect.util.DateUtil.instantToUTCDateTime;
import static com.expedia.adaptivealerting.anomdetect.util.DateUtil.isBetweenHours;

/**
 * Uses a simple schedule to determine if a metric is a candidate for anomaly detection.
 * A metric only passes through this filter (on to detection or further filtering) if the metric's
 * timestamp falls within the detector's configured start and end hours.
 * Special Case: When utcStartHour and utcEndHour are configured with the same value (e.g. both equal 0), it is assumed the user intends to allow
 * detection to proceed at all hours of the day - i.e. this filter acts as a noop pass-through.
 */
@Data
@NoArgsConstructor
@Setter(AccessLevel.NONE)
public class HourOfDayDetectionFilter implements DetectionFilter {

    private int utcStartHour;
    private int utcEndHour;

    public HourOfDayDetectionFilter(@JsonProperty("utcStartHour") int utcStartHour,
                                    @JsonProperty("utcEndHour") int utcEndHour) {
        isBetween(utcStartHour, 0, 23, "Required: utcStartHour should be between 0 and 23");
        isBetween(utcEndHour, 0, 23, "Required: utcEndHour should be between 0 and 23");
        this.utcStartHour = utcStartHour;
        this.utcEndHour = utcEndHour;
    }

    @Override
    public void doFilter(DetectorRequest detectorRequest, DetectorResponse detectorResponse, DetectionFilterChain chain) {
        notNull(detectorRequest, "metricData can't be null");
        if (metricTimeFallsWithinFilter(detectorRequest.getMetricData())) {
            // Metric passes time filter - pass it along to the detector/next filter
            chain.doFilter(detectorRequest, detectorResponse);
        } else {
            // Metric fails time filter - set empty response and abort chain
            OutlierDetectorResult emptyResult = new OutlierDetectorResult();
            detectorResponse.setDetectorResult(emptyResult);
        }
    }

    private boolean metricTimeFallsWithinFilter(MetricData metricData) {
        long metricTimestamp = metricData.getTimestamp();
        Instant epochSecond = DateUtil.epochSecondToInstant(metricTimestamp);
        ZonedDateTime dt = instantToUTCDateTime(epochSecond);
        return isBetweenHours(dt.getHour(), this.getUtcStartHour(), this.getUtcEndHour());
    }
}
