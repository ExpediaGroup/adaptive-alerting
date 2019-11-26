package com.expedia.adaptivealerting.modelservice.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class GeneralMetersTest {

    String mapping_time_delay = "es-lookup.delay-getting-mapping";
    //int timer = 100;
    long timer1 = System.currentTimeMillis();
    MeterRegistry meterRegistry;
    GeneralMeters generalMeters = new GeneralMeters(meterRegistry);
    //long mapping_time_delay = 1234;

    @Ignore
    @Test
    public void getDelayMappingTimer() {
//      log.info("Inside getDelayMappingTimer()");
//      mapping_time_delay = "es-lookup.delay-getting-mapping";
//      timer1 = System.currentTimeMillis();
//      assertEquals(timer1,generalMeters.getDelayMappingTimer());
    }

    @Ignore
    @Test
    public void getDelayGettingDetectors() {
    }

    @Ignore
    @Test
    public void getMappingExceptionCount() {
    }

    @Ignore
    @Test
    public void getDetectorExceptionCount() {
    }
}