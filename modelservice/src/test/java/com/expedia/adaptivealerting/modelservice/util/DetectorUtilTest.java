package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.Assert.assertEquals;


@Slf4j
public class DetectorUtilTest {

    private Map<String, Object> detectorParams;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testGetDetector() {
        Detector detector = DetectorUtil.getDetector("constant-detector", detectorParams);
        assertEquals(ConstantThresholdDetector.class, detector.getClass());
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        detectorParams = mom.getDetectorParams();
    }
}
