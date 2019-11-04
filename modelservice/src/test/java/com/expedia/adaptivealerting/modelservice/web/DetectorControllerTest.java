package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.modelservice.repo.DetectorRepository;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DetectorControllerTest {

    @InjectMocks
    private DetectorController controllerUnderTest;

    @Mock
    private DetectorRepository detectorRepo;

    @Mock
    private DetectorDocument detector;

    @Mock
    private List<DetectorDocument> detectors;

    private UUID someUuid;
    private DetectorDocument legalParamsDetector;

    @Before
    public void setUp() {
        this.controllerUnderTest = new DetectorController();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testCreateDetector() {
        val uuidStr = controllerUnderTest.createDetector(legalParamsDetector);
        val uuid = UUID.fromString(uuidStr);
        assertNotNull(uuid);
    }

    @Test
    public void testFindByUuid() {
        val actualDetector = controllerUnderTest.findByUuid(someUuid.toString());
        assertNotNull(actualDetector);
    }

    @Test
    public void testFindByCreatedBy() {
        val actualDetectors = controllerUnderTest.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        val actualDetectors = controllerUnderTest.getLastUpdatedDetectors(5);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test
    public void testUpdateDetector() {
        controllerUnderTest.updateDetector(someUuid.toString(), legalParamsDetector);
    }

    @Test
    public void testToggleDetector() {
        controllerUnderTest.toggleDetector(someUuid.toString(), true);
    }

    @Test
    public void testTrustDetector() {
        controllerUnderTest.trustDetector(someUuid.toString(), true);
    }

    @Test
    public void testDeleteDetector() {
        val someUuidStr = someUuid.toString();
        controllerUnderTest.deleteDetector(someUuidStr);
        verify(detectorRepo, times(1)).deleteDetector(someUuidStr);
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();

        val mom = ObjectMother.instance();
        this.legalParamsDetector = mom.getDetectorDocument();
        legalParamsDetector.setUuid(someUuid);
    }

    private void initDependencies() {
        when(detectorRepo.createDetector(any(DetectorDocument.class))).thenReturn(someUuid);
        when(detectorRepo.findByUuid(someUuid.toString())).thenReturn(detector);
        when(detectorRepo.getLastUpdatedDetectors(anyLong())).thenReturn(detectors);
    }
}
