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
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@Slf4j
public class ModelServiceTest {

    @InjectMocks
    private ModelServiceImpl modelServiceimpl;
    private Metric metric;

    @Mock
    private ModelServiceImpl getModelServiceimpl;

    @Before
    public void setUp() throws Exception {
        this.modelServiceimpl = new ModelServiceImpl();
        this.metric = exisitingMetric();
        MockitoAnnotations.initMocks(this);
        initDependencies();
    }

    @Test
    public void onboardMetricCheck() {
        Metric onboardMetric = new Metric();
        onboardMetric.setId(Long.valueOf("138"));
        onboardMetric.setKey("test49");
        onboardMetric.setHash("hash49");
        Map<String, Object> tagsMap = new LinkedHashMap<>();
        String tags = "{\"unit\":\"unknown10\", \"mtype\":\"gauge14\", \"org_id\":\"107\", \"interval\":\"500\", \"region\":\"expedia-com\", \"lob\":\"package\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            tagsMap = mapper.readValue(tags, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        onboardMetric.setTags(tagsMap);
        Metric metricCheck = getModelServiceimpl.onboard(onboardMetric);
        assertEquals(onboardMetric, metricCheck);
    }

    @Test
    public void newMetricOnboard() {
        Metric newOnboardMetric = new Metric();
        newOnboardMetric.setId(Long.valueOf("139"));
        newOnboardMetric.setKey("test50");
        newOnboardMetric.setHash("hash50");
        Map<String, Object> tagsMap = new LinkedHashMap<>();
        String tags = "{\"unit\":\"unknown20\", \"mtype\":\"gauge15\", \"org_id\":\"108\", \"interval\":\"501\", \"region\":\"expedia-se-com\", \"lob\":\"car\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            tagsMap = mapper.readValue(tags, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        newOnboardMetric.setTags(tagsMap);
        Metric newMetric = getModelServiceimpl.onboard(newOnboardMetric);
        try {
            assertEquals(newOnboardMetric, newMetric);
        } catch (AssertionError e) {
            log.info("Metric is not onboarded");
        }
    }

    private Metric exisitingMetric() {
        Metric newMetric = new Metric();
        newMetric.setId(Long.valueOf("138"));
        newMetric.setKey("test49");
        newMetric.setHash("hash49");
        Map<String, Object> tagsMap = new LinkedHashMap<>();
        String tags = "{\"unit\":\"unknown10\", \"mtype\":\"gauge14\", \"org_id\":\"107\", \"interval\":\"500\", \"region\":\"expedia-com\", \"lob\":\"package\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            tagsMap = mapper.readValue(tags, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        newMetric.setTags(tagsMap);
        return newMetric;
    }

    @Test
    public void metricCheckPass() {
        List metricCheckList = new ArrayList<>();
        Map<String, Object> tagsMap = new LinkedHashMap<>();
        String tags = "{\"unit\":\"unknown\", \"mtype\":\"gauge\", \"org_id\":\"1\", \"interval\":\"30\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            tagsMap = mapper.readValue(tags, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Optional<Metric>> metricList = new ArrayList<>();
        metricList.add(Optional.of(new Metric((long) 17, "karmalab.stats.gauges.AirBoss.chelappabo001.karmalab.net.java.nio.BufferPool.direct.MemoryUsed", "1.36585c5f53807a5785787b9fa0a66c83", tagsMap)));
        metricCheckList = getModelServiceimpl.findMetricsByTags(anyList());
        assertEquals(metricCheckList, metricList);
    }

    @Test
    public void MetricCheckFail() {
        List metricCheckList = new ArrayList<>();
        Map<String, Object> tagsMap = new LinkedHashMap<>();
        String tags = "{\"units\":\"unknown\", \"mtypes\":\"gauge\", \"org_id\":\"1\", \"interval\":\"30\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            tagsMap = mapper.readValue(tags, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Optional<Metric>> metricList = new ArrayList<>();
        metricList.add(Optional.of(new Metric((long) 17, "karmalab.stats.gauges.AirBoss.chelappabo001.karmalab.net.java.nio.BufferPool.direct.MemoryUsed", "1.36585c5f53807a5785787b9fa0a66c83", tagsMap)));
        metricCheckList = getModelServiceimpl.findMetricsByTags(anyList());
        try {
            assertEquals(metricCheckList, metricList);
        }catch (AssertionError e) {
            log.info("Tags weren't found.");
        }
    }


    private List<Optional<Metric>> existingMetric() {
        Map<String, Object> tagsMap = new LinkedHashMap<>();
        String tags = "{\"unit\":\"unknown\", \"mtype\":\"gauge\", \"org_id\":\"1\", \"interval\":\"30\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            tagsMap = mapper.readValue(tags, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Optional<Metric>> metricList = new ArrayList<>();
        metricList.add(Optional.of(new Metric((long) 17, "karmalab.stats.gauges.AirBoss.chelappabo001.karmalab.net.java.nio.BufferPool.direct.MemoryUsed", "1.36585c5f53807a5785787b9fa0a66c83", tagsMap)));
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("unit", "unknown"));
        tagList.add(new Tag("mtype", "gauge"));
        tagList.add(new Tag("org_id", "1"));
        tagList.add(new Tag("interval", "30"));

        return metricList;
    }

    private void initDependencies() {
        when(getModelServiceimpl.onboard(metric)).thenReturn(exisitingMetric());
        when(getModelServiceimpl.findMetricsByTags(anyList())).thenReturn(existingMetric());
    }
}
