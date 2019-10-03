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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.cusum;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CusumDetectorTestRow {

    @CsvBindByName
    private int sample;

    @CsvBindByName
    private double observed;

    @CsvBindByName
    private double sh;

    @CsvBindByName
    private double sl;

    @CsvBindByName(column = "stdev")
    private double stdDev;

    @CsvBindByName
    private String level;

    public CusumDetectorTestRow(double observed, String level) {
        this.observed = observed;
        this.level = level;
    }
}
