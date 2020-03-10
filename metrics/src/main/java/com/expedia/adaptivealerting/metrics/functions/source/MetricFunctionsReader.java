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
package com.expedia.adaptivealerting.metrics.functions.source;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricFunctionsReader {

    public static List<MetricFunctionsSpec> readFromInputFile(String InputFilename) {
        List<MetricFunctionsSpec> metricFunctionSpecList = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(InputFilename);
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String metricFunctionSpecString;
            while ((metricFunctionSpecString = bufferedReader.readLine()) != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                metricFunctionSpecList.add(objectMapper.readValue(metricFunctionSpecString, MetricFunctionsSpec.class));
            }
            bufferedReader.close();
        } catch (Exception e) {
            log.error("Exception while reading input functions' definition", e);
        }
        return metricFunctionSpecList;
    }
}