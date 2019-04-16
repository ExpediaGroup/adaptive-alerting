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
package com.expedia.adaptivealerting.anomdetect.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * The type Detector match response.
 *
 * Response from model service , for
 *
 * groupedDetectorsBySearchIndex  is map of [index of tags list] to [matching Detectors]
 *
 */
//same as com.expedia.adaptivealerting.modelservice.model.MatchingDetectorsResponse
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectorMatchResponse {
    private Map<Integer, List<Detector>> groupedDetectorsBySearchIndex;
    private long lookupTimeInMillis;
}
