package com.expedia.adaptivealerting.modelservice.acutator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CustomActuatorMetricServiceImplTest {

    @Spy
    @InjectMocks
    private CustomActuatorMetricService actuatorMetricService = new CustomActuatorMetricServiceImpl();

    @Mock
    private MeterRegistry registry;

    @Test
    public void increaseCount() {
        Mockito.when(registry.counter(anyString())).thenReturn(getCounter());
        actuatorMetricService.increaseCount(200);
        actuatorMetricService.increaseCount(404);
        verify(actuatorMetricService, times(2)).increaseCount(anyInt());
    }

    private Counter getCounter() {
        return Counter
                .builder("instance")
                .description("test counter")
                .tags("dev", "performance")
                .register(new SimpleMeterRegistry());
    }
}
