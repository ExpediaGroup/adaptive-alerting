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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.outlier.ConstantThresholdOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.CusumOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.ForecastingOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.IndividualsOutlierDetector;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Detector configuration interface. The nature of the configuration depends on the detector type. For example, a
 * constant threshold detector will specify a number of thresholds. A forecasting detector on the other hand will
 * include a point forecast model and an interval forecast model, with model configurations for each.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConstantThresholdOutlierDetector.Params.class, name = "constant-threshold"),
        @JsonSubTypes.Type(value = CusumOutlierDetector.Params.class, name = "cusum"),
        @JsonSubTypes.Type(value = ForecastingOutlierDetector.Params.class, name = "forecasting"),
        @JsonSubTypes.Type(value = IndividualsOutlierDetector.Params.class, name = "individuals"),
})
public interface DetectorConfig {

    void validate();
}
