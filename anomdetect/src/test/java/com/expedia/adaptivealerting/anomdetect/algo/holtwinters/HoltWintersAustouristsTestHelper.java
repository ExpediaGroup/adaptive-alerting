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
package com.expedia.adaptivealerting.anomdetect.algo.holtwinters;

import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.util.CsvToBeanFileReader.readData;

public class HoltWintersAustouristsTestHelper {
    public static final double[] ADDITIVE_IDENTITY_SEASONALS = {0, 0, 0, 0};
    public static final double[] MULTIPLICATIVE_IDENTITY_SEASONALS = {1, 1, 1, 1};
    public static final int AUSTOURISTS_FREQUENCY = 4;
    public static final double AUSTOURISTS_ALPHA = 0.441;
    public static final double AUSTOURISTS_BETA = 0.030;
    public static final double AUSTOURISTS_GAMMA = 0.002;
    public static final double[] AUSTOURISTS_FIRST_TWO_SEASONS = {30.052513, 19.148496, 25.317692, 27.591437,
            32.076456, 23.487961, 28.47594, 35.123753};
    public static final double MULT_LEVEL = 31.5627386776757;
    public static final double MULT_BASE = 1.00423842901043;
    public static final double[] MULT_SEASONAL = {1.17712812297656, 0.750171172765994, 0.991509119512213, 1.08080386146847};
    public static final double ADD_LEVEL = 31.6186014537107;
    public static final double ADD_BASE = 1.00659064213558;
    public static final double[] ADD_SEASONAL = {4.52260406322175, -6.37978566983114, -0.216664733898538, 2.06488449502578};
    public static final double TOLERANCE = 0.001;
    public static final String AUSTOURISTS_ADD_FILE = "tests/austourists-tests-holtwinters-additive.csv";
    public static List<HoltWintersAustouristsTestRow> AUSTOURISTS_ADD_DATA = readData(AUSTOURISTS_ADD_FILE, HoltWintersAustouristsTestRow.class);
    public static final String AUSTOURISTS_MULT_FILE = "tests/austourists-tests-holtwinters-multiplicative.csv";
    public static List<HoltWintersAustouristsTestRow> AUSTOURISTS_MULT_DATA = readData(AUSTOURISTS_MULT_FILE, HoltWintersAustouristsTestRow.class);

    public static HoltWintersParams buildAustouristsParams(SeasonalityType seasonalityType) {
        return new HoltWintersParams()
                .setFrequency(AUSTOURISTS_FREQUENCY)
                .setAlpha(AUSTOURISTS_ALPHA)
                .setBeta(AUSTOURISTS_BETA)
                .setGamma(AUSTOURISTS_GAMMA)
                .setSeasonalityType(seasonalityType)
                .setWarmUpPeriod(AUSTOURISTS_FREQUENCY);
    }

    public static HoltWintersParams buildAustouristsParams(SeasonalityType seasonalityType, double level, double base, double[] seasonal) {
        return new HoltWintersParams()
                .setFrequency(AUSTOURISTS_FREQUENCY)
                .setAlpha(AUSTOURISTS_ALPHA)
                .setBeta(AUSTOURISTS_BETA)
                .setGamma(AUSTOURISTS_GAMMA)
                .setSeasonalityType(seasonalityType)
                .setWarmUpPeriod(AUSTOURISTS_FREQUENCY)
                .setInitLevelEstimate(level)
                .setInitBaseEstimate(base)
                .setInitSeasonalEstimates(seasonal);
    }
}
