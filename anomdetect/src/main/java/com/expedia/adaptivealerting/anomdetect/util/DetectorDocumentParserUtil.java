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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;
import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DetectorDocumentParserUtil {

    public static List<DetectionFilter> parseFilters(DetectorDocument document) {
        final ObjectMapper objectMapper = new ObjectMapper();
        Object filters = document.getConfig().get("filters");
        return (filters == null) ? Collections.emptyList() :
                Arrays.asList(objectMapper.convertValue(filters, DetectionFilter[].class));
    }
}
