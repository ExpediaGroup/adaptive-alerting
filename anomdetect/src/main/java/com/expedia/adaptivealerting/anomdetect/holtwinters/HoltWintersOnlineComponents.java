package com.expedia.adaptivealerting.anomdetect.holtwinters;

import lombok.Data;
import lombok.NonNull;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Arrays;

/**
 * Encapsulates the values that represent the components for the {@link HoltWintersOnlineAlgorithm} logic.
 * This represents the model's online data as opposed to {@link HoltWintersParams} which represents the users values for the model's parameters.
 *
 * @author Matt Callanan
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
@Data
public class HoltWintersOnlineComponents {
    private static final double MULTIPLICATIVE_IDENTITY = 1;
    private static final double ADDITIVE_IDENTITY = 0;
    @NonNull
    private final HoltWintersParams params;
    private double level = 0;
    private double base = 0;
    @NonNull
    private double[] seasonal;
    private SummaryStatistics overallSummaryStatistics = new SummaryStatistics();
    private SummaryStatistics[] seasonalSummaryStatistics;
    private double forecast = Double.NaN;

    /**
     * Constructs HoltWintersOnlineComponents object
     * @param params User-supplied parameters for the model.  Assumed to be valid.
     */
    public HoltWintersOnlineComponents(HoltWintersParams params) {
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
     * Return n=period seasonal components in reverse order, starting with the current season.
     * E.g. if period=4 and we've most recently observed the 2nd season (s2), the seasonal components will be returned in the following order: seasonal[1], seasonal[0], seasonal[3], seasonal[2]
     *
     * Makes for easy comparison with R-generated datasets.
     *
     * @return seasonal components in reverse order
     */
    public double[] getReverseHistorySeasonals() {
        int currentIdx = getCurrentSeasonalIndex();
        int m = getParams().getPeriod();
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
     * @return Index into seasonal components, ranges from 0 to period-1.  Increments whenever addValue() is called.  Wraps back to 0 after period ticks.
     */
    public int getCurrentSeasonalIndex() {
        return (int) (getN() % params.getPeriod());
    }

    private void initLevelFromParams(HoltWintersParams params) {
        this.level = Double.isNaN(params.getInitLevelEstimate()) ? seasonalityIdentity() : params.getInitLevelEstimate();
    }

    private void initBaseFromParams(HoltWintersParams params) {
        this.base = Double.isNaN(params.getInitBaseEstimate()) ? seasonalityIdentity() : params.getInitBaseEstimate();
    }

    private void initSeasonalsFromParams(HoltWintersParams params) {
        int s = params.getInitSeasonalEstimates().length;
        if (s == 0) {
            // TODO HW: Test this
            fillSeasonalsWithIdentity();
        } else if (s != params.getPeriod()) {
            // TODO HW: Test this
            // TODO HW: Use strongly typed exception
            throw new IllegalArgumentException(String.format("initSeasonalEstimates array is not the same size (%d) as period (%d)", s, params.getPeriod()));
        } else {
            this.seasonal = Arrays.copyOf(params.getInitSeasonalEstimates(), params.getPeriod());
        }
    }

    private void fillSeasonalsWithIdentity() {
        seasonal = new double[params.getPeriod()];
        Arrays.fill(seasonal, seasonalityIdentity());
    }

    private double seasonalityIdentity() {
        return params.getSeasonalityType() == SeasonalityType.MULTIPLICATIVE
                ? MULTIPLICATIVE_IDENTITY
                : ADDITIVE_IDENTITY;
    }

    private void initSeasonalStatistics(HoltWintersParams params) {
        seasonalSummaryStatistics = new SummaryStatistics[params.getPeriod()];
        for (int i = 0; i < params.getPeriod(); i++) {
            seasonalSummaryStatistics[i] = new SummaryStatistics();
            seasonalSummaryStatistics[i].addValue(seasonal[i]);
        }
    }

}
