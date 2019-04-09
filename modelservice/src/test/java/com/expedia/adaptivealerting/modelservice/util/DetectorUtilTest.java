package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Slf4j
public class DetectorUtilTest {

    @Test
    public void testGetDetector() {

        String thresholds = "{\"thresholds\": {\"lowerStrong\": \"8\", \"lowerWeak\": \"9\"}}";
        Map<String, Object> detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        Detector detector = DetectorUtil.getDetector("constant-detector", detectorParams);
        assertEquals(ConstantThresholdDetector.class, detector.getClass());
    }

    @SneakyThrows
    private Map<String, Object> toObject(String message) {
        return new ObjectMapper().readValue(message, HashMap.class);
    }
}
