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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectorMapping {
    private String id;
    private Detector detector;
    private ExpressionTree expression;
    private long lastModifiedTimeInMillis;
    private long createdTimeInMillis;
    private boolean isEnabled;

    
    public Map<String, String> getTags() {
        return this.getExpression()
                .getOperands()
                .stream()
                .collect(Collectors.toMap(op -> op.getField().getKey(), op -> op.getField().getValue()));
    }


    public String getKey() {
        final String KEY_VAL_DELIMITER = "->";
        final String TAG_DELIMITER = ",";
        
        return this.getTags().entrySet()
                .stream()
                .map(entry -> {
                    String encodedValue = Base64.getEncoder().encodeToString(entry.getValue().getBytes());
                    return entry.getKey() + KEY_VAL_DELIMITER + encodedValue;
                })
                .sorted()
                .collect(Collectors.joining(TAG_DELIMITER));
    }

}
