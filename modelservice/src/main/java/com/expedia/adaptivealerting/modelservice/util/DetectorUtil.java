package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.anomdetect.comp.DetectorLookup;
import com.expedia.adaptivealerting.anomdetect.detector.*;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class DetectorUtil {

    private static final DetectorLookup detectorLookup = new DetectorLookup();

    public static Detector getDetector(String detectorType, Map paramsMap) {
        Class<? extends Detector> detectorClass = detectorLookup.getDetector(detectorType);
        AbstractDetector detector = (AbstractDetector) ReflectionUtil.newInstance(detectorClass);

        Class<? extends DetectorParams> paramsClass = detector.getParamsClass();
        DetectorParams params = (DetectorParams) new ObjectMapper().convertValue(paramsMap, paramsClass);
        AnomalyType anomalyType = getAnomalyType(paramsClass, params);
        detector.init(UUID.randomUUID(), params, anomalyType);
        return detector;
    }

    private static AnomalyType getAnomalyType(Class<? extends DetectorParams> paramsClass, DetectorParams params) {
        if (ConstantThresholdParams.class.equals(paramsClass)) {
            return ((ConstantThresholdParams) params).getType();
        } else if (CusumParams.class.equals(paramsClass)) {
            return ((CusumParams) params).getType();
        } else {
            return AnomalyType.TWO_TAILED;
        }
    }
}
