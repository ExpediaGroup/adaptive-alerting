package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelResource;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelTypeResource;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detector.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DetectorUtil {

    public static Detector getDetector(String detectorType, Map paramsMap) {
        ModelTypeResource modelTypeResource = new ModelTypeResource();
        modelTypeResource.setKey(detectorType);
        ModelResource resource = new ModelResource();
        resource.setDetectorType(modelTypeResource);
        resource.setParams(paramsMap);
        resource.setDateCreated(new Date());

        LegacyDetectorFactory factory = new LegacyDetectorFactory();
        return factory.createDetector(UUID.randomUUID(), resource);
    }
}
