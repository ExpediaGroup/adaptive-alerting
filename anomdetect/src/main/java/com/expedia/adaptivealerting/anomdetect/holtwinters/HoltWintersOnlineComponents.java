package com.expedia.adaptivealerting.anomdetect.holtwinters;

import lombok.Data;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Arrays;

/**
 * Encapsulates the values that represent the components for the {@link HoltWintersOnlineAlgorithm} logic.
 *
 * @author Matt Callanan
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
@Data
public class HoltWintersOnlineComponents {
    private static final double MULTIPLICATIVE_IDENTITY = 1;
    private static final double ADDITIVE_IDENTITY = 0;
    private final HoltWintersParams params;
    private double level = 0;
    private double base = 0;
    private double[] seasonal;
    private SummaryStatistics overallSummaryStatistics = new SummaryStatistics();
    private SummaryStatistics[] seasonalSummaryStatistics;
    private double forecast;

    public HoltWintersOnlineComponents(HoltWintersParams params) {
        this.params = params;
        initLevelFromParams(params);
        initBaseFromParams(params);
        initSeasonalsFromParams(params);
        initSeasonalStatistics(params);
        initForecastFromParams(params);
    }

    public long getN() {
        return overallSummaryStatistics.getN();
    }

    public double getSeasonal(int seasonalIdx) {
        return seasonal[seasonalIdx];
    }

    public void setSeasonal(int seasonalIdx, double seasonalValue, double observed) {
        seasonal[seasonalIdx] = seasonalValue;
        seasonalSummaryStatistics[seasonalIdx].addValue(observed);
    }

    public void addValue(double observed) {
        overallSummaryStatistics.addValue(observed);
    }

    public double getMean() {
        return overallSummaryStatistics.getMean();
    }

    public double getMin() {
        return overallSummaryStatistics.getMin();
    }

    public double getMax() {
        return overallSummaryStatistics.getMax();
    }

    public double getVariance() {
        return overallSummaryStatistics.getVariance();
    }

    public double getStandardDeviation() {
        return overallSummaryStatistics.getStandardDeviation();
    }

    public double getSeasonalStandardDeviation(int seasonalIdx) {
        return seasonalSummaryStatistics[seasonalIdx].getStandardDeviation();
    }

    public double getSeasonalMean(int seasonalIdx) {
        return seasonalSummaryStatistics[seasonalIdx].getMean();
    }

    /**
     * @return Index into seasonal components, ranges from 0 to period-1.  Increments whenever addValue() is called.  Wraps back to 0 after period ticks.
     */
    public int currentSeasonalIndex() {
        return (int) (getN() % params.getPeriod());
    }

    /**
     * @return The index for the last season (wraps around to period-1 if current index = 0)
     */
    public int previousSeasonalIndex() {
        return (currentSeasonalIndex() + params.getPeriod() - 1) % params.getPeriod();
    }

    @Override
    public String toString() {
        return "HoltWintersOnlineComponents{" +
                "params=" + params +
                ", level=" + level +
                ", base=" + base +
                ", seasonal=" + Arrays.toString(seasonal) +
                ", n=" + getN() +
                ", mean=" + getMean() +
                '}';
    }


    // TODO HW: Test cases

    private void initLevelFromParams(HoltWintersParams params) {
        this.level = Double.isNaN(params.getInitLevelEstimate()) ? seasonalityIdentity() : params.getInitLevelEstimate();
    }

    private void initBaseFromParams(HoltWintersParams params) {
        this.base = Double.isNaN(params.getInitBaseEstimate()) ? seasonalityIdentity() : params.getInitBaseEstimate();
    }

    private void initSeasonalsFromParams(HoltWintersParams params) {
        int s = params.getInitSeasonalEstimates().length;
        if (s == 0) {
            fillSeasonalsWithIdentity();
        } else if (s != params.getPeriod()) {
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
        return params.getSeasonalityType() == SeasonalityType.MULTIPLICATIVE ? MULTIPLICATIVE_IDENTITY : ADDITIVE_IDENTITY;
    }

    private void initSeasonalStatistics(HoltWintersParams params) {
        seasonalSummaryStatistics = new SummaryStatistics[params.getPeriod()];
        for (int i = 0; i < params.getPeriod(); i++) {
            seasonalSummaryStatistics[i] = new SummaryStatistics();
            seasonalSummaryStatistics[i].addValue(seasonal[i]);
        }
    }

    private void initForecastFromParams(HoltWintersParams params) {
        this.forecast = Double.isNaN(params.getInitForecastEstimate()) ? seasonalityIdentity() : params.getInitForecastEstimate();
    }

}
