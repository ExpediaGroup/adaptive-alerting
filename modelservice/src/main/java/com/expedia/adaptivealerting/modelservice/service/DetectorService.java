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
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Detector;

import java.util.List;
import java.util.UUID;

public interface DetectorService {

    /**
     * Saves a detector to the detector store. The detector UUID must be {@literal null}, as this method
     * assigns a UUID.
     *
     * @param detector Detector
     * @return Detector UUID assigned by this call
     */
    UUID createDetector(Detector detector);

    Detector findByUuid(String uuid);

    List<Detector> findByCreatedBy(String user);

    void toggleDetector(String uuid, Boolean enabled);

    void trustDetector(String uuid, Boolean trusted);

    List<Detector> getLastUpdatedDetectors(long interval);

    List<Detector> getLastUsedDetectors(int noOfDays);

    List<Detector> getDetectorsToBeTrained();

    void updateDetector(String uuid, Detector detector);

    void updateDetectorLastUsed(String uuid);

    void updateDetectorTrainingTime(String uuid, long nextRun);

    void deleteDetector(String uuid);

}
