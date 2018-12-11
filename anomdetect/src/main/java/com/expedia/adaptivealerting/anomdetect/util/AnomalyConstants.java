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
package com.expedia.adaptivealerting.anomdetect.util;

/**
 * Anomaly constants.
 *
 * @author Willie Wheeler
 */
public interface AnomalyConstants {
    
    /**
     * Key/value tag key for metrics representing an anomaly. The value must be the detector UUID.
     */
    String AA_DETECTOR_UUID = "aa_detector_uuid";
}
