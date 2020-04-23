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
package com.expedia.adaptivealerting.anomdetect.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CacheUtilTest {

    private Map<String, String> tags;
    private String cacheKey;
    private List<Detector> detectors;
    private String cacheValue;

    private void initMap() {
        tags = new HashMap<>();
        tags.put("key1", "value1");
        tags.put("key2", "value2");
        tags.put("key3", "value3");
        tags.put("key4", "value4");

        cacheKey = "key1->dmFsdWUx,key2->dmFsdWUy,key3->dmFsdWUz,key4->dmFsdWU0";
    }

    private void initDetectors() {
        UUID uuid1 = UUID.fromString("fe1a2366-a73e-4c9d-9186-474e60df6de8");
        UUID uuid2 = UUID.fromString("65eea7d8-8ec3-4f8a-ab2c-7a9dc873723d");
        UUID uuid3 = UUID.fromString("65eea7d8-8ec3-4f8a-ab2c-7a9dc8737231");
        UUID uuid4 = UUID.fromString("fe1a2366-a73e-4c9d-9186-474e60df6de9");

        detectors = new ArrayList<>();
        detectors.add(new Detector("ad-manager", uuid1));
        detectors.add(new Detector("external-detector-1", uuid2));
        detectors.add(new Detector("external-detector-2", uuid3));
        detectors.add(new Detector("", uuid4));
        cacheValue = "ad-manager,fe1a2366-a73e-4c9d-9186-474e60df6de8|external-detector-1,65eea7d8-8ec3-4f8a-ab2c-7a9dc873723d|external-detector-2,65eea7d8-8ec3-4f8a-ab2c-7a9dc8737231|,fe1a2366-a73e-4c9d-9186-474e60df6de9";
    }

    @Test
    public void getKey() {
        initMap();
        Assert.assertEquals(cacheKey, CacheUtil.getKey(tags));
    }

    @Test
    public void getTags() {
        initMap();
        Assert.assertEquals(tags, CacheUtil.getTags(cacheKey));

    }

    @Test
    public void getDetectorIds() {
        initDetectors();
        Assert.assertEquals(cacheValue, CacheUtil.getValue(detectors));
    }

    @Test
    public void buildDetectors() {
        initDetectors();
        Assert.assertEquals(detectors, CacheUtil.buildDetectors(cacheValue));
        Assert.assertEquals(Collections.EMPTY_LIST, CacheUtil.buildDetectors(""));
    }
}
