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
package com.expedia.adaptivealerting.anomdetect.source.factory;

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;

import java.io.IOException;

public abstract class AbstractDetectorFactoryTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected DetectorDocument readDocument(String name) {
        val path = "detector-documents/" + name + ".json";
        try {
            return objectMapper.readValue(ClassLoader.getSystemResourceAsStream(path), DetectorDocument.class);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read " + path, e);
        }
    }
}
