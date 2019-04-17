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
 *
 * Response from model-service for request findMatchingDetectorMappings
 * same as com.expedia.adaptivealerting.modelservice.model.MatchingDetectorsResponse
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectorMatchResponse {
    /*
    * groupedDetectorsBySearchIndex  is map of [index of tags list] to [matching Detectors]
    * eg. if
    *    request  is
    *     List(tags1,tags2)  and
    *
    *   response is
    *   {
    *     {  0:[detector1, detector2],
    *       1:[detector3, detector4]  }
    *   }
    *
    *   that means
    *       tags1 matches to detector1, detector2
    *       tags2 matches to detector3, detector4
    * */
    private Map<Integer, List<Detector>> groupedDetectorsBySearchIndex;
    private long lookupTimeInMillis;
}
