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
package com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters;

import com.expedia.adaptivealerting.anomdetect.comp.legacy.HoltWintersParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.HoltWintersForecaster;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Arrays;

/**
 * Encapsulates the values that represent the components for the {@link HoltWintersOnlineAlgorithm} logic.
 * This represents the model's online data as opposed to {@link HoltWintersParams} which represents the users values for
 * the model's parameters.
 *
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a> and
 * <a href="https://robjhyndman.com/hyndsight/seasonal-periods/">https://robjhyndman.com/hyndsight/seasonal-periods/</a>
 * for naming conventions (e.g. usage of "frequency" and "cycle").
 */
@Data
public class HoltWintersOnlineComponents {
    private static final double MULTIPLICATIVE_IDENTITY = 1;
    private static final double ADDITIVE_IDENTITY = 0;
    @NonNull
    private final HoltWintersForecaster.Params params;
    private double level = 0;
    private double base = 0;
    @NonNull
    private double[] seasonal;
    private SummaryStatistics overallSummaryStatistics = new SummaryStatistics();
    private SummaryStatistics[] seasonalSummaryStatistics;
    private double forecast = Double.NaN;

    /**
     * Constructs HoltWintersOnlineComponents object
     *
     * @param params User-supplied parameters for the model.  Assumed to be valid.
     */
    public HoltWintersOnlineComponents(HoltWintersForecaster.Params params) {
        this.params = params;
        initLevelFromParams(params);
        initBaseFromParams(params);
        initSeasonalsFromParams(params);
        initSeasonalStatistics(params);
    }

    public long getN() {
        return overallSummaryStatistics.getN();
    }

    public double getSeasonal(int seasonalIdx) {
        return seasonal[seasonalIdx];
    }

    /**
     * Return n=frequency seasonal components in reverse order, starting with the current season.
     * E.g. if frequency=4 and we've most recently observed the 2nd season (s2), the seasonal components will be returned in the following order:
     * seasonal[1], seasonal[0], seasonal[3], seasonal[2]
     * <p>
     * Makes for easy comparison with R-generated datasets.
     *
     * @return seasonal components in reverse order
     */
    public double[] getReverseHistorySeasonals() {
        int currentIdx = getCurrentSeasonalIndex();
        int m = getParams().getFrequency();
        double[] result = new double[m];
        for (int i = 0; i < result.length; i++) {
            result[i] = getSeasonal((currentIdx + m - i - 1) % m);
        }
        return result;
    }

    public void setSeasonal(int seasonalIdx, double seasonalValue, double observed) {
        seasonal[seasonalIdx] = seasonalValue;
        seasonalSummaryStatistics[seasonalIdx].addValue(observed);
    }

    public void addValue(double observed) {
        overallSummaryStatistics.addValue(observed);
    }

    public double getSeasonalStandardDeviation(int seasonalIdx) {
        return seasonalSummaryStatistics[seasonalIdx].getStandardDeviation();
    }

    /**
     * @return Index into seasonal components, ranges from 0 to frequency-1.  Increments whenever addValue() is called.  Wraps back to 0 after frequency ticks.
     */
    public int getCurrentSeasonalIndex() {
        return (int) (getN() % params.getFrequency());
    }

    private void initLevelFromParams(HoltWintersForecaster.Params params) {
        this.level = Double.isNaN(params.getInitLevelEstimate()) ? seasonalityIdentity() : params.getInitLevelEstimate();
    }

    private void initBaseFromParams(HoltWintersForecaster.Params params) {
        this.base = Double.isNaN(params.getInitBaseEstimate()) ? seasonalityIdentity() : params.getInitBaseEstimate();
    }

    private void initSeasonalsFromParams(HoltWintersForecaster.Params params) {
        int s = params.getInitSeasonalEstimates().length;
        if (s == 0) {
            fillSeasonalsWithIdentity();
        } else if (s != params.getFrequency()) {
            throw new IllegalStateException(String.format("Invalid: initSeasonalEstimates array is not the same size (%d) as frequency (%d). Ensure only valid parameters are used.", s, params.getFrequency()));
        } else {
            this.seasonal = Arrays.copyOf(params.getInitSeasonalEstimates(), params.getFrequency());
        }
    }

    private void fillSeasonalsWithIdentity() {
        seasonal = new double[params.getFrequency()];
        Arrays.fill(seasonal, seasonalityIdentity());
    }

    private double seasonalityIdentity() {
        return params.getSeasonalityType() == SeasonalityType.MULTIPLICATIVE
                ? MULTIPLICATIVE_IDENTITY
                : ADDITIVE_IDENTITY;
    }

    private void initSeasonalStatistics(HoltWintersForecaster.Params params) {
        seasonalSummaryStatistics = new SummaryStatistics[params.getFrequency()];
        for (int i = 0; i < params.getFrequency(); i++) {
            seasonalSummaryStatistics[i] = new SummaryStatistics();
            seasonalSummaryStatistics[i].addValue(seasonal[i]);
        }
    }

}
