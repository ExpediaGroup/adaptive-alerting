/*
 * Copyright 2019 Expedia Group, Inc.
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

import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.util.CsvToBeanFileReader.readData;

public class HoltWintersAustouristsTestHelper {
    public static final int AUSTOURISTS_PERIOD = 4;
    public static final double AUSTOURISTS_ALPHA = 0.441;
    public static final double AUSTOURISTS_BETA = 0.030;
    public static final double AUSTOURISTS_GAMMA = 0.002;
    public static final String AUSTOURISTS_ADD_FILE = "tests/austourists-tests-holtwinters-additive.csv";
    public static final double[] ADDITIVE_IDENTITY_SEASONALS = {0, 0, 0, 0};
    public static final double[] MULTIPLICATIVE_IDENTITY_SEASONALS = {1, 1, 1, 1};
    public static List<HoltWintersAustouristsTestRow> AUSTOURISTS_ADD_DATA = readData(AUSTOURISTS_ADD_FILE, HoltWintersAustouristsTestRow.class);
    public static final String AUSTOURISTS_MULT_FILE = "tests/austourists-tests-holtwinters-multiplicative.csv";
    public static List<HoltWintersAustouristsTestRow> AUSTOURISTS_MULT_DATA = readData(AUSTOURISTS_MULT_FILE, HoltWintersAustouristsTestRow.class);

    public static HoltWintersParams buildAustouristsParams(SeasonalityType seasonalityType) {
        return new HoltWintersParams()
                    .setPeriod(AUSTOURISTS_PERIOD)
                    .setAlpha(AUSTOURISTS_ALPHA)
                    .setBeta(AUSTOURISTS_BETA)
                    .setGamma(AUSTOURISTS_GAMMA)
                    .setSeasonalityType(seasonalityType)
                    .setWarmUpPeriod(AUSTOURISTS_PERIOD);
    }

    public static HoltWintersParams buildAustouristsParams(SeasonalityType seasonalityType, double level, double base, double[] seasonal) {
        return new HoltWintersParams()
                    .setPeriod(AUSTOURISTS_PERIOD)
                    .setAlpha(AUSTOURISTS_ALPHA)
                    .setBeta(AUSTOURISTS_BETA)
                    .setGamma(AUSTOURISTS_GAMMA)
                    .setSeasonalityType(seasonalityType)
                    .setWarmUpPeriod(AUSTOURISTS_PERIOD)
                    .setInitLevelEstimate(level)
                    .setInitBaseEstimate(base)
                    .setInitSeasonalEstimates(seasonal);
    }
}
