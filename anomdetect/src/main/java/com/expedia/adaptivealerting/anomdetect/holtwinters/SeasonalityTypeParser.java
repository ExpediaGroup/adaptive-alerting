/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.holtwinters;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SeasonalityTypeParser {

    /**
     * Parse a string to its matching SeasonalityType
     *
     * @param text SeasonalityType in string format (e.g. "ADDITIVE")
     * @return SeasonalityType enum
     */
    public SeasonalityType parse(String text) {
        if (text == null) {
            return null;
        }
        Optional<SeasonalityType> match = findMatchingSeasonalityType(text);
        if (!match.isPresent()) {
            throw new RuntimeException(String.format("Failed to parse seasonality type \"%s\". Accepted values are %s", text, getValidNames()));
        }
        return match.get();
    }

    private Optional<SeasonalityType> findMatchingSeasonalityType(String text) {
        return Arrays.stream(SeasonalityType.values())
                .filter(
                        seasonalityType -> seasonalityType.getName().matches(text.toLowerCase())
                )
                .findFirst();
    }

    private List<String> getValidNames() {
        return Arrays.stream(SeasonalityType.values())
                .map(SeasonalityType::getName)
                .collect(Collectors.toList());
    }

}
