package com.expedia.adaptivealerting.anomdetect.holtwinters;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.MULTIPLICATIVE;

/**
 * Encapsulates the algorithm for forecasting one-step ahead estimate using Holt-Winters (Triple-Exponential Smoothing) method.
 *
 * @author Matt Callanan
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
public class HoltWintersOnlineAlgorithm {

    /**
     * Get a single forecast value for the next tick given the previously observed values.  Updates values for the model's components based on this observation.
     *
     * @param y          Observed value for time "t"
     * @param params     Contains the parameters for model
     * @param components Contains the online values used to calculate level, base and seasonality components (based on most recent history from observation at t-1).
     */
    public void observeValueAndUpdateForecast(double y, HoltWintersParams params, HoltWintersOnlineComponents components) {
        // Retrieve the model's parameters as set by user (or defaults)
        double alpha = params.getAlpha();
        double beta = params.getBeta();
        double gamma = params.getGamma();
        boolean multiplicative = params.isMultiplicative();
        // Retrieve model's level and base component values from previous observation (t - 1)
        double prevLevel = components.getLevel();
        double prevBase = components.getBase();
        // Retrieve model's seasonal component that relates to the current season we're observing for time t (i.e. "period" seasons ago)
        int seasonalIdx = components.getCurrentSeasonalIndex();
        double season = components.getSeasonal(seasonalIdx);

        double newLevel, newBase, newSeason;

        // Calculate new components given y_t (current observed value) and generate new forecast for y_t+1
        if (multiplicative) {
            newLevel = alpha * (y / season) + (1 - alpha) * (prevLevel + prevBase);
            newBase = beta * (newLevel - prevLevel) + (1 - beta) * prevBase;
            newSeason = gamma * (y / (prevLevel + prevBase)) + (1 - gamma) * season;
        } else {
            newLevel = alpha * (y - season) + (1 - alpha) * (prevLevel + prevBase);
            newBase = beta * (newLevel - prevLevel) + (1 - beta) * prevBase;
            newSeason = gamma * (y - prevLevel - prevBase) + (1 - gamma) * season;
        }

        // Update the model's components
        updateComponents(components, y, newLevel, newBase, newSeason, seasonalIdx);

        // Record observation of y
        observeValue(components, y);

        double nextSeason = components.getSeasonal(components.getCurrentSeasonalIndex());
        // Forecast the value for next season
        updateForecast(components, getForecast(params.getSeasonalityType(), newLevel, newBase, nextSeason));
    }

    public double getForecast(SeasonalityType seasonalityType, double level, double base, double season) {
        return MULTIPLICATIVE.equals(seasonalityType)
                ? (level + base) * season
                : level + base + season;
    }


    private void updateComponents(HoltWintersOnlineComponents components, double y, double newLevel, double newBase, double newSeason, int seasonalIdx) {
        components.setLevel(newLevel);
        components.setBase(newBase);
        components.setSeasonal(seasonalIdx, newSeason, y);
    }

    private void updateForecast(HoltWintersOnlineComponents components, double newForecast) {
        components.setForecast(newForecast);
    }

    private void observeValue(HoltWintersOnlineComponents components, double y) {
        components.addValue(y);
    }

}
