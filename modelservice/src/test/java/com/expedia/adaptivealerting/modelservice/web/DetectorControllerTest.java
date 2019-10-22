package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorException;
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
    private DetectorController controller;

    @Mock
    private DetectorRepository detectorRepo;

    @Mock
    private DetectorDocument detector;

    @Mock
    private List<DetectorDocument> detectors;

    private UUID someUuid;
    private DetectorDocument illegalParamsDetector;
    private DetectorDocument legalParamsDetector;

    @Before
    public void setUp() {
        this.controller = new DetectorController();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        when(detectorRepo.findByUuid(someUuid.toString())).thenReturn(detector);
        when(detectorRepo.getLastUpdatedDetectors(anyLong())).thenReturn(detectors);
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();

        val mom = ObjectMother.instance();
        legalParamsDetector = mom.getDetector();
        legalParamsDetector.setUuid(someUuid);

        illegalParamsDetector = mom.getIllegalParamsDetector();
        illegalParamsDetector.setUuid(someUuid);
    }

    @Test
    public void testFindByUuid() {
        DetectorDocument actualDetector = controller.findByUuid(someUuid.toString());
        assertNotNull(actualDetector);
    }

    @Test
    public void testFindByCreatedBy() {
        List<DetectorDocument> actualDetectors = controller.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
    }

    @Test
    public void testToggleDetector() {
        controller.toggleDetector(someUuid.toString(), true);
    }

    @Test
    public void testCreateDetector() {
        doReturn("1").when(controller).createDetector(legalParamsDetector);
        Assert.assertEquals(controller.createDetector(legalParamsDetector), "1");
        controller.createDetector(legalParamsDetector);
        verify(controller, times(2)).createDetector(legalParamsDetector);
    }

    @Test
    public void testUpdateDetector() {
        controller.updateDetector(someUuid.toString(), legalParamsDetector);
        verify(controller, times(1)).updateDetector(someUuid.toString(), legalParamsDetector);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        int interval = 5;
        List<DetectorDocument> actualDetectors = controller.getLastUpdatedDetectors(interval);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test(expected = DetectorException.class)
    public void testCreateDetectorNullValues() {
        DetectorDocument detector1 = new DetectorDocument();
        detector1.setCreatedBy("user");
        controller.createDetector(detector1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetectorIllegalThresholds() {
        controller.createDetector(illegalParamsDetector);
    }

    @Test(expected = DetectorException.class)
    public void testUpdateDetectorNullValues() {
        DetectorDocument detector1 = new DetectorDocument();
        detector1.setCreatedBy("user");
        controller.updateDetector("", detector1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDetectorIllegalThresholds() {
        controller.updateDetector("", illegalParamsDetector);
    }
}
