/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.acutator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * Custom service to track HTTP status <br>
 * Micrometer by default tracks HTTP status by URI which is pretty hard to manage <br>
 * With this all the HTTP requests will be clubbed under counter.status.* metric [Karan Shah]
 */
@Service
public class CustomActuatorMetricServiceImpl implements CustomActuatorMetricService {

    @Autowired
    private MeterRegistry registry;

    private final List<List<Integer>> statusMetricsByMinute;
    private final List<String> statusList;

    public CustomActuatorMetricServiceImpl() {
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

    @Generated
    @Scheduled(fixedDelay = 60000)
    public void exportMetrics() {
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
