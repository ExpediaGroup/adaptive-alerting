package com.expedia.adaptivealerting.modelservice.acutator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
Custom service to track HTTP status. Micrometer by default tracks http status by URI which is pretty hard to manage. [Karan Shah]
 */
@Service
@Slf4j
public class CustomActuatorMetricServiceImpl implements CustomActuatorMetricService {

    @Autowired
    private MeterRegistry registry;

    private final List<List<Integer>> statusMetricsByMinute;
    private final List<String> statusList;

    public CustomActuatorMetricServiceImpl() {
        super();
        statusMetricsByMinute = new ArrayList<>();
        statusList = new ArrayList<>();
    }

    @Override
    public void increaseCount(final int status) {
        String counterName = "counter.status." + status;
        registry.counter(counterName).increment(1);
        if (!statusList.contains(counterName)) {
            statusList.add(counterName);
        }
    }

    @Scheduled(fixedDelay = 60000)
    private void exportMetrics() {
        final List<Integer> statusCount = new ArrayList<>();
        for (final String status : statusList) {
            Search search = registry.find(status);
            if (search != null) {
                Counter counter = search.counter();
                statusCount.add(counter != null ? ((int) counter.count()) : 0);
                registry.remove(counter);
            } else {
                statusCount.add(0);
            }
        }
        statusMetricsByMinute.add(statusCount);
    }
}
