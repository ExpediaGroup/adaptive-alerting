package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class DetectorRegistry {

    private Config config = new TypesafeConfigLoader("detector-runner").loadMergedConfig();
    public Detector detector;

    @Autowired
    public DetectorRegistry(ListableBeanFactory beanFactory) {
        Collection<Detector> detectorBeans = beanFactory.getBeansOfType(Detector.class).values();
        detector = detectorBeans.stream().filter(d -> config.getString("detector").equalsIgnoreCase(d.getName()))
                .findFirst().get();
        log.info("Detector in use is : " + detector.getName());
    }

    public Detector getDetector() {
        return detector;
    }
}
