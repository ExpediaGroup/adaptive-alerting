package com.expedia.adaptivealerting.modelservice.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GeneralMeters {

    //ELASTIC SEARCH COUNTERS
    private static final String MAPPING_EXCEPTION_COUNTER = "es-lookup.mapping-exception";
    private static final String DETECTOR_EXCEPTION_COUNTER = "es-lookup.detector-exception";

    //ELASTIC SEARCH TIME DELAY
    private static final String MAPPING_TIME_DELAY = "es-lookup.delay-getting-mapping";
    private static final String DELAY_IN_GETTING_DETECTORS = "es-lookup.delay-getting-detectors";

    private Timer delayMappingTimer;
    private Timer delayGettingDetectors;
    private Counter mappingExceptionCount;
    private Counter detectorExceptionCount;

    @Autowired
    public GeneralMeters(MeterRegistry meterRegistry) {
        this.delayMappingTimer = meterRegistry.timer(MAPPING_TIME_DELAY);
        this.delayGettingDetectors = meterRegistry.timer(DELAY_IN_GETTING_DETECTORS);
        this.mappingExceptionCount = meterRegistry.counter(MAPPING_EXCEPTION_COUNTER);
        this.detectorExceptionCount = meterRegistry.counter(DETECTOR_EXCEPTION_COUNTER);
    }
}
