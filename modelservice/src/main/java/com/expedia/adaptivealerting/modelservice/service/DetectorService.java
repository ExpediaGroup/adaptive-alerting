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

    List<Detector> getLastUsedDetectors(int weeks);

    void deleteDetector(String uuid);

    void updateDetector(String uuid, Detector detector);

}
