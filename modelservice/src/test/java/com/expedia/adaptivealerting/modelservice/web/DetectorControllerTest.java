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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DetectorControllerTest {

    @Spy
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
        when(detectorRepo.findByUuid(someUuid.toString())).thenReturn(detector);
        when(detectorRepo.getLastUpdatedDetectors(anyLong())).thenReturn(detectors);
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();

        val mom = ObjectMother.instance();
        legalParamsDetector = mom.getDetectorDocument();
        legalParamsDetector.setUuid(someUuid);
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
    public void testToggleDetector() {
        controllerUnderTest.toggleDetector(someUuid.toString(), true);
    }

    @Test
    public void testCreateDetector() {
        doReturn("1").when(controllerUnderTest).createDetector(legalParamsDetector);
        Assert.assertEquals(controllerUnderTest.createDetector(legalParamsDetector), "1");
        controllerUnderTest.createDetector(legalParamsDetector);
        verify(controllerUnderTest, times(2)).createDetector(legalParamsDetector);
    }

    @Test
    public void testUpdateDetector() {
        controllerUnderTest.updateDetector(someUuid.toString(), legalParamsDetector);
        verify(controllerUnderTest, times(1)).updateDetector(someUuid.toString(), legalParamsDetector);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        int interval = 5;
        val actualDetectors = controllerUnderTest.getLastUpdatedDetectors(interval);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test
    public void testDeleteDetector() {
        val someUuidStr = someUuid.toString();
        controllerUnderTest.deleteDetector(someUuidStr);
        verify(detectorRepo, times(1)).deleteDetector(someUuidStr);
    }
}
