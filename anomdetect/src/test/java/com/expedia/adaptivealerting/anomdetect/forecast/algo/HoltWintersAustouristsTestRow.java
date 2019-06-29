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
package com.expedia.adaptivealerting.anomdetect.forecast.algo;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class HoltWintersAustouristsTestRow {

    /**
     * The observed metric value.
     */
    @CsvBindByName
    private double y;

    /**
     * y-hat: The value predicted for y after the last observation.
     */
    @CsvBindByName(column = "y.hat")
    private double yHat;

    /**
     * Level
     */
    @CsvBindByName
    private double l;

    /**
     * Base
     */
    @CsvBindByName
    private double b;

    /**
     * 1 season ago: seasonal component for t-1
     */
    @CsvBindByName
    private double s1;

    /**
     * 2 seasons ago: seasonal component for t-2
     */
    @CsvBindByName
    private double s2;

    /**
     * 3 seasons ago: seasonal component for t-3
     */
    @CsvBindByName
    private double s3;

    /**
     * 4 seasons ago: seasonal component for t-4
     */
    @CsvBindByName
    private double s4;

//    @CsvCustomBindByName(converter = StringToAnomalyLevelConverter.class)
//    private AnomalyLevel expectedLevel;
//
//    @CsvBindByName
//    private double expectedForecast;

}
