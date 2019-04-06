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

package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.ToString;

/**
 * The type Individual chart test row.
 */
@Data
@ToString
public class IndividualsChartTestRow {

    @CsvBindByName
    private int sample;

    @CsvBindByName
    private double observed;

    @CsvBindByName
    private double upperControlLimit_R;

    @CsvBindByName
    private double upperControlLimit_X;

    @CsvBindByName
    private double lowerControlLimit_X;

    @CsvBindByName
    private double target;

    @CsvBindByName
    private String anomalyLevel;
}
