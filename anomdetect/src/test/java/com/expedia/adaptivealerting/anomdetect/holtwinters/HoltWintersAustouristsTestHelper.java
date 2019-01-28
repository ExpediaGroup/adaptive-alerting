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
