package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.DetectorRepository;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DetectorServiceImplTest {

    @InjectMocks
    private DetectorService serviceUnderTest;

    @Mock
    private DetectorRepository repository;

    @Mock
    private List<Detector> detectors;

    private UUID someUuid;

    private Detector legalParamsDetector;

    @Before
    public void setUp() {
        this.serviceUnderTest = new DetectorServiceImpl();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testCreateDetector() {
        legalParamsDetector.setUuid(null);
        val uuid = serviceUnderTest.createDetector(legalParamsDetector);
        assertNotNull(uuid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_null_detector() {
        serviceUnderTest.createDetector(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetector_set_uuid() {
        legalParamsDetector.setUuid(UUID.randomUUID());
        serviceUnderTest.createDetector(legalParamsDetector);
    }

    @Test
    public void testFindByUuid() {
        val actualDetector = serviceUnderTest.findByUuid(someUuid.toString());
        assertNotNull(actualDetector);
    }


    @Test(expected = RecordNotFoundException.class)
    public void testFindByUuid_invalid_uuid() {
        val actualDetector = serviceUnderTest.findByUuid(UUID.randomUUID().toString());
        assertNotNull(actualDetector);
    }

    @Test
    public void testFindByCreatedBy() {
        val actualDetectors = serviceUnderTest.findByCreatedBy("userName");
        assertNotNull(actualDetectors);
    }

    @Test(expected = RecordNotFoundException.class)
    public void testFindByCreatedBy_invalid_user() {
        val actualDetectors = serviceUnderTest.findByCreatedBy("invalidUserName");
        assertNotNull(actualDetectors);
    }

    @Test
    public void testToggleDetector() {
        serviceUnderTest.toggleDetector(someUuid.toString(), true);
        verify(repository, times(1)).findByUuid(someUuid.toString());
        verify(repository, times(1)).save(legalParamsDetector);
    }

    @Test
    public void testTrustDetector() {
        serviceUnderTest.trustDetector(someUuid.toString(), true);
        verify(repository, times(1)).findByUuid(someUuid.toString());
        verify(repository, times(1)).save(legalParamsDetector);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        val actualDetectors = serviceUnderTest.getLastUpdatedDetectors(5);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test
    public void testGetLastUsedDetectors() {
        val actualDetectors = serviceUnderTest.getLastUsedDetectors(4);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test
    public void testUpdateDetector() {
        serviceUnderTest.updateDetector(someUuid.toString(), legalParamsDetector);
        verify(repository, times(1)).findByUuid(someUuid.toString());
        verify(repository, times(1)).save(legalParamsDetector);
    }

    @Test
    public void testUpdateDetectorWithTrainingMetaData() {
        Detector.TrainingMetaData trainingMetaData = new Detector.TrainingMetaData();
        trainingMetaData.setCronSchedule("42 1 * * 3");
        legalParamsDetector.getDetectorConfig().setTrainingMetaData(trainingMetaData);
        serviceUnderTest.updateDetector(someUuid.toString(), legalParamsDetector);
        verify(repository, times(1)).findByUuid(someUuid.toString());
        verify(repository, times(1)).save(legalParamsDetector);
    }

    @Test
    public void testUpdateDetectorLastUsed() {
        serviceUnderTest.updateDetectorLastUsed(someUuid.toString());
        verify(repository, times(1)).findByUuid(someUuid.toString());
        verify(repository, times(1)).save(legalParamsDetector);
    }

    @Test
    public void testDeleteDetector() {
        val someUuidStr = someUuid.toString();
        serviceUnderTest.deleteDetector(someUuidStr);
        verify(repository, times(1)).deleteByUuid(someUuidStr);
    }

    @Test
    public void testGetDetectorsToBeTrained() {
        serviceUnderTest.getDetectorsToBeTrained();
        verify(repository, times(1)).findByDetectorConfig_TrainingMetaData_DateNextTrainingLessThan(anyString());
    }

    @Test
    public void testUpdateDetectorTrainingTime() {
        val uuid = this.someUuid.toString();
        serviceUnderTest.updateDetectorTrainingTime(uuid, 0L);
        verify(repository, times(1)).findByUuid(someUuid.toString());
        verify(repository, times(1)).save(legalParamsDetector);
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();
        val mom = ObjectMother.instance();
        this.legalParamsDetector = mom.buildDetector();
        legalParamsDetector.setUuid(someUuid);
    }

    private void initDependencies() {
        when(repository.save(any(Detector.class))).thenReturn(legalParamsDetector);
        when(repository.findByUuid(someUuid.toString())).thenReturn(legalParamsDetector);
        when(repository.findByMeta_CreatedBy("userName")).thenReturn(detectors);
        when(repository.findByMeta_DateLastUpdatedGreaterThan(anyString())).thenReturn(detectors);
        when(repository.findByMeta_DateLastAccessedLessThan(anyString())).thenReturn(detectors);
    }
}
