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
package com.expedia.adaptivealerting.metricprofiler.source;

import com.expedia.adaptivealerting.anomdetect.util.AssertUtil;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.http.client.fluent.Content;

import java.io.IOException;
import java.util.HashMap;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@RequiredArgsConstructor
public class DefaultProfileSource implements ProfileSource {

    @NonNull
    private final HttpClientWrapper httpClient;

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final String baseUri;

    public static final String FIND_DOCUMENT_PATH = "/api/metricProfiling/search/findByTags";

    @Override
    public Boolean profileExists(MetricDefinition metricDefinition) {
        AssertUtil.notNull(metricDefinition, "metricDefinition can't be null");
        val tags = metricDefinition.getTags().getKv();

        val mutableTags = new HashMap<>();
        mutableTags.putAll(tags);
        mutableTags.remove("box");

        isTrue(mutableTags.size() > 0, "tags must not be empty");

        val uri = baseUri + FIND_DOCUMENT_PATH;
        Content content;
        try {
            val body = objectMapper.writeValueAsString(mutableTags);
            content = httpClient.post(uri, body);
        } catch (IOException e) {
            val message = "IOException while finding matching metrics for" +
                    ": tags=" + mutableTags +
                    ", httpMethod=POST" +
                    ", uri=" + uri;
            throw new RuntimeException(message, e);
        }
        try {
            objectMapper.readValue(content.asBytes(), Boolean.class);
            return true;
        } catch (IOException e) {
            val message = "IOException while finding finding matching profile: tags=" + mutableTags;
            throw new RuntimeException(message, e);
        }
    }
}
