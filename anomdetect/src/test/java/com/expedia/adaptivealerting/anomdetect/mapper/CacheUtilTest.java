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
    private String tagKey;
    private List<Detector> detectors;
    private String bunchOfDetectorIds;

    private void initMap() {
        tags = new HashMap<>();
        tags.put("key1", "value1");
        tags.put("key2", "value2");
        tags.put("key3", "value3");
        tags.put("key4", "value4");

        tagKey = "key1->value1,key2->value2,key3->value3,key4->value4";
    }

    private void initDetectors() {
        UUID uuid1 = UUID.fromString("fe1a2366-a73e-4c9d-9186-474e60df6de8");
        UUID uuid2 = UUID.fromString("65eea7d8-8ec3-4f8a-ab2c-7a9dc873723d");
        detectors = new ArrayList<>();
        detectors.add(new Detector(uuid1));
        detectors.add(new Detector(uuid2));

        bunchOfDetectorIds = "fe1a2366-a73e-4c9d-9186-474e60df6de8|65eea7d8-8ec3-4f8a-ab2c-7a9dc873723d";
    }


    @Test
    public void getKey() {
        initMap();
        Assert.assertEquals(tagKey, CacheUtil.getKey(tags));
    }

    @Test
    public void getTags() {
        initMap();
        Assert.assertEquals(tags, CacheUtil.getTags(tagKey));

    }

    @Test
    public void getDetectorIds() {
        initDetectors();
        Assert.assertEquals(bunchOfDetectorIds, CacheUtil.getDetectorIds(detectors));
    }

    @Test
    public void buildDetectors() {
        initDetectors();
        Assert.assertEquals(detectors, CacheUtil.buildDetectors(bunchOfDetectorIds));
        Assert.assertEquals(Collections.EMPTY_LIST, CacheUtil.buildDetectors(""));
    }
}
