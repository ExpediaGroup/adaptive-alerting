package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticsearchUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GeneralMetersTest {

    private GeneralMeters generalMeters = mock(GeneralMeters.class);

    @Test
    public void getDelayMappingTimer() {
        when(generalMeters.getDelayMappingTimer()).thenReturn(mock(Timer.class));
    }

    @Test
    public void getDelayGettingDetectors() {
        when(generalMeters.getDelayGettingDetectors()).thenReturn(mock(Timer.class));
    }

    @Test
    public void getMappingExceptionCount() {
        when(generalMeters.getDelayMappingTimer()).thenReturn(mock(Timer.class));
    }

    @Test
    public void getDetectorExceptionCount() {
        when(generalMeters.getDetectorExceptionCount()).thenReturn(mock(Counter.class));
    }
}