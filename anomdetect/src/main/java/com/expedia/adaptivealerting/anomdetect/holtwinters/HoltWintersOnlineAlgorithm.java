package com.expedia.adaptivealerting.anomdetect.holtwinters;

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
        // Retrieve model's component values from previous observations (t - 1)
        // TODO HW: Consider "prev" instead of "last"
        double lastLevel = components.getLevel();
        double lastBase = components.getBase();
        int seasonalIdx = components.currentSeasonalIndex();
        // We get the last seasonal component that relates to the current season we're observing for time t (i.e. seasonalIdx = (t - period) % period)
        // TODO HW: Think about a better name:
        double lastSeason = components.getSeasonal(seasonalIdx);

        double newLevel, newBase, newSeason, newForecast;

        // Calculate new components given y_t (current observed value) and generate new forecast for y_t+1 (to be compared with next observed value)
        if (multiplicative) {
            newLevel = alpha * (y / lastSeason) + (1 - alpha) * (lastLevel + lastBase);
            newBase = beta * (newLevel - lastLevel) + (1 - beta) * lastBase;
            newSeason = gamma * (y / (lastLevel + lastBase)) + (1 - gamma) * lastSeason;
            newForecast = (newLevel + newBase) * newSeason;
        } else {
            newLevel = alpha * (y - lastSeason) + (1 - alpha) * (lastLevel + lastBase);
            newBase = beta * (newLevel - lastLevel) + (1 - beta) * lastBase;
            newSeason = gamma * (y - (lastLevel - lastBase)) + (1 - gamma) * lastSeason;
            newForecast = newLevel + newBase + newSeason;
        }

        // Store the forecast to be compared with next metric observation and update the model's components
        updateComponents(components, y, newLevel, newBase, newSeason, seasonalIdx, newForecast);
    }


    private void updateComponents(HoltWintersOnlineComponents components, double y, double newLevel, double newBase, double newSeason, int seasonalIdx, double newForecast) {
        components.addValue(y);
        components.setLevel(newLevel);
        components.setBase(newBase);
        components.setSeasonal(seasonalIdx, newSeason, y);
        components.setForecast(newForecast);
    }

}
