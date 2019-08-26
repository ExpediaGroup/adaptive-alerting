package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.source.DetectorException;
import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.service.DetectorService;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private DetectorService detectorService;

    @Mock
    private Detector detector;

    @Mock
    private List<Detector> detectors;

    private Detector illegalParamsDetector;
    private Detector legalParamsDetector;


    @Before
    public void setUp() {
        this.controller = new DetectorController();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        when(detectorService.findByUuid(anyString())).thenReturn(detector);
        when(detectorService.getLastUpdatedDetectors(anyLong())).thenReturn(detectors);
    }

    private void initTestObjects() {
        val mom = ObjectMother.instance();
        illegalParamsDetector = mom.getIllegalParamsDetector();
        legalParamsDetector = mom.getDetector();
    }


    @Test
    public void testFindByUuid() {
        Detector actualDetector = controller.findByUuid("uuid");
        assertNotNull(actualDetector);
    }

    @Test
    public void testFindByCreatedBy() {
        List<Detector> actualDetectors = controller.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
    }

    @Test
    public void testToggleDetector() {
        controller.toggleDetector("uuid", true);
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
        controller.updateDetector("uuid", legalParamsDetector);
        verify(controller, times(1)).updateDetector("uuid", legalParamsDetector);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        int interval = 5;
        List<Detector> actualDetectors = controller.getLastUpdatedDetectors(interval);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test(expected = DetectorException.class)
    public void testCreateDetectorNullValues() {
        Detector detector1 = new Detector();
        detector1.setCreatedBy("user");
        controller.createDetector(detector1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDetectorIllegalThresholds() {
        controller.createDetector(illegalParamsDetector);
    }

    @Test(expected = DetectorException.class)
    public void testUpdateDetectorNullValues() {
        Detector detector1 = new Detector();
        detector1.setCreatedBy("user");
        controller.updateDetector("", detector1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDetectorIllegalThresholds() {
        controller.updateDetector("", illegalParamsDetector);
    }
}
