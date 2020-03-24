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
package com.expedia.adaptivealerting.anomdetect.filter;

import com.expedia.adaptivealerting.anomdetect.filter.algo.post.MOfNAggregationFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.post.PassThroughPostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.HourOfDayDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.PassThroughPreDetectionFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;

public class DetectionFiltersTest {

    @Test
    public void testDeserialize() {
        DetectionFilters filters = deserializeJson("detection-filters", DetectionFilters.class);
        assertArrayEquals(expectedPreDetectionFilters().toArray(), filters.getPreDetectionFilters().toArray());
        assertArrayEquals(expectedPostDetectionFilters().toArray(), filters.getPostDetectionFilters().toArray());
    }

    @Test
    public void testDeserializeWithEmptyLists() {
        DetectionFilters filters = deserializeJson("detection-filters-empty-lists", DetectionFilters.class);
        assertArrayEquals(Collections.emptyList().toArray(), filters.getPreDetectionFilters().toArray());
        assertArrayEquals(Collections.emptyList().toArray(), filters.getPostDetectionFilters().toArray());
    }

    private ImmutableList<PreDetectionFilter> expectedPreDetectionFilters() {
        return ImmutableList.of(
                new HourOfDayDetectionFilter(9, 17),
                new PassThroughPreDetectionFilter());
    }

    private ImmutableList<PostDetectionFilter> expectedPostDetectionFilters() {
        return ImmutableList.of(
                new MOfNAggregationFilter(3, 5),
                new PassThroughPostDetectionFilter());
    }

    // TODO: Move somewhere reusable. Use it from AbstractDetectorFactoryTest
    private static <T> T deserializeJson(String name, Class<T> clazz) {
        final ObjectMapper objectMapper = new ObjectMapper();
        String path = "filter/" + name + ".json";
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);
        checkStreamExists(path, inputStream);
        try {
            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while parsing '%s'", path), e);
        }
    }

    private static void checkStreamExists(String path, InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException(String.format("Couldn't read '%s' file.  Does it exist?", path));
        }
    }

}
