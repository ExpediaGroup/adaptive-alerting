package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.DetectorRepository;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isNull;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Service
public class DetectorServiceImpl implements DetectorService {

    @Autowired
    private DetectorRepository repository;

    @Override
    public UUID createDetector(Detector detector) {
        notNull(detector, "detectorDto can't be null");
        isNull(detector.getUuid(), "Required: detectorDto.uuid == null");

        val uuid = UUID.randomUUID();
        detector.setId(uuid.toString());
        detector.setUuid(uuid);
        detector.setMeta(buildDetectorMetaData(detector));
        repository.save(detector);
        return uuid;
    }

    @Override
    public Detector findByUuid(String uuid) {
        Detector detector = repository.findByUuid(uuid);
        if (detector == null) {
            throw new RecordNotFoundException("Invalid UUID: " + uuid);
        }
        return detector;
    }

    @Override
    public List<Detector> findByCreatedBy(String user) {
        List<Detector> detectors = repository.findByMeta_CreatedBy(user);
        if (detectors == null || detectors.isEmpty()) {
            throw new RecordNotFoundException("Invalid user: " + user);
        }
        return detectors;
    }

    @Override
    public void toggleDetector(String uuid, Boolean enabled) {
        Detector detector = repository.findByUuid(uuid);
        detector.setEnabled(enabled);
        repository.save(detector);
    }

    @Override
    public void trustDetector(String uuid, Boolean trusted) {
        Detector detector = repository.findByUuid(uuid);
        detector.setTrusted(trusted);
        repository.save(detector);
    }

    @Override
    public List<Detector> getLastUpdatedDetectors(long interval) {
        val now = DateUtil.now().toInstant();
        val fromDate = DateUtil.toUtcDateString((now.minus(interval, ChronoUnit.SECONDS)));
        return repository.findByMeta_DateLastUpdatedGreaterThan(fromDate);
    }

    @Override
    public List<Detector> getLastUsedDetectors(int noOfWeeks) {
        val now = DateUtil.now().toInstant();
        val fromDate = DateUtil.toUtcDateString((now.minus(noOfWeeks, ChronoUnit.WEEKS)));
        return repository.findByMeta_DateLastAccessedLessThan(fromDate);
    }

    @Override
    public void deleteDetector(String uuid) {
        repository.deleteByUuid(uuid);
    }

    @Override
    public void updateDetector(String uuid, Detector detector) {
        notNull(detector, "detector can't be null");
        MDC.put("DetectorUuid", uuid);
        Date nowDate = DateUtil.now();
        Detector detectorToBeUpdated = repository.findByUuid(uuid);
        detectorToBeUpdated.setDetectorConfig(detector.getDetectorConfig());
        detectorToBeUpdated.setMeta(buildDetectorMetaData(detector));
        repository.save(detectorToBeUpdated);
    }

    private Detector.Meta buildDetectorMetaData(Detector detector) {
        Date nowDate = DateUtil.now();
        Detector.Meta metaBlock = detector.getMeta();
        if (metaBlock == null) {
            metaBlock = new Detector.Meta();
            metaBlock.setDateLastUpdated(nowDate);
            metaBlock.setCreatedBy(detector.getCreatedBy());
            detector.setMeta(metaBlock);
        } else {
            metaBlock = detector.getMeta();
            metaBlock.setDateLastUpdated(nowDate);
            if (metaBlock.getCreatedBy() == null) {
                metaBlock.setCreatedBy(detector.getCreatedBy());
            }
        }
        return metaBlock;
    }
}
