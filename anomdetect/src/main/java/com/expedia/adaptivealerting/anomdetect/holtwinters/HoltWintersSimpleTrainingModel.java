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

import java.util.Arrays;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersTrainingMethod.SIMPLE;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isFalse;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.util.stream.DoubleStream.concat;

/**
 * Implements an online model to train the HoltWintersComponents values based on the first two periods of data.
 * <p>
 * See the "simple" value for the "initial" argument to <a href="https://www.rdocumentation.org/packages/forecast/versions/8.5/topics/ses#l_arguments">ses</a> in the R "forecast" package.
 * <br>
 * R source code: <a href="https://github.com/robjhyndman/forecast/blob/master/R/HoltWintersNew.R#L61-L67">https://github.com/robjhyndman/forecast/blob/master/R/HoltWintersNew.R#L61-L67</a>
 */
public class HoltWintersSimpleTrainingModel {
    private int n = 0;
    private final double[] firstPeriod;
    private final double[] secondPeriod;

    public HoltWintersSimpleTrainingModel(HoltWintersParams params) {
        this.firstPeriod = new double[params.getPeriod()];
        this.secondPeriod = new double[params.getPeriod()];
    }

    /**
     * SIMPLE training method requires 2 complete periods of data to finish training the initial level, base and seasonal components (l, b, s).
     * l and s can be calculated after the first period, b can only be determined after the 2nd season. This object stores those 2 periods as the
     * firstPeriod and secondPeriod array fields.
     *
     * E.g. if period=4, then on the 8th observation, the model will complete its training of l, b, s.
     * Furthermore, on the 8th observation this method will then fit the model to the 8 initial observations, by running through those 8 stored data
     * points (in firstPeriod and secondPeriod) one at a time, to retrospectively apply the smoothing parameters (alpha, beta, gamma) to l, b, s.
     *
     * After the 8th observation, the components.getForecast() returns the correct forecast for the 9th observation which is the first non-training
     * observation that can be used to detect anomalies.
     */
    public void observeAndTrain(double y, HoltWintersParams params, HoltWintersOnlineComponents components) {
        checkNulls(params, components);
        checkTrainingMethod(params);
        checkStillInInitialTraining(params);
        int period = params.getPeriod();

        // Capture data points
        if (isBetween(n, 0, period - 1)) {
            firstPeriod[n] = y;
        } else {
            secondPeriod[n - period] = y;
        }
        // Train
        if (n == params.getInitTrainingPeriod() - 1) {
            setLevel(components);
            setSeasonals(y, params, components);
            setBase(params, components);
            updateComponentsAndForecast(params, components);
        }
        n++;
    }

    public boolean isTrainingComplete(HoltWintersParams params) {
        return n >= (params.getInitTrainingPeriod());
    }

    /**
     * Update the level, base and seasonal components by running the main algorithm over each of the observations to this point.
     */
    private void updateComponentsAndForecast(HoltWintersParams params, HoltWintersOnlineComponents components) {
        HoltWintersOnlineAlgorithm algorithm = new HoltWintersOnlineAlgorithm();
        concat(Arrays.stream(firstPeriod), Arrays.stream(secondPeriod)).forEach(y -> algorithm.observeValueAndUpdateForecast(y, params, components));
    }

    private void setLevel(HoltWintersOnlineComponents components) {
        components.setLevel(mean(firstPeriod));
    }

    private void setBase(HoltWintersParams params, HoltWintersOnlineComponents components) {
        double base = (mean(secondPeriod) - components.getLevel()) / params.getPeriod();
        components.setBase(base);
    }

    private void setSeasonals(double y, HoltWintersParams params, HoltWintersOnlineComponents components) {
        for (int i = 0; i < params.getPeriod(); i++) {
            double s = params.isMultiplicative()
                    ? firstPeriod[i] / components.getLevel()
                    : firstPeriod[i] - components.getLevel();
            components.setSeasonal(i, s, y);
        }
    }

    private void checkNulls(HoltWintersParams params, HoltWintersOnlineComponents components) {
        notNull(params, "params can't be null");
        notNull(components, "components can't be null");
    }

    private void checkTrainingMethod(HoltWintersParams params) {
        isTrue(SIMPLE.equals(params.getInitTrainingMethod()),
                String.format("Expected training method to be %s but was %s", SIMPLE, params.getInitTrainingMethod()));
    }

    private void checkStillInInitialTraining(HoltWintersParams params) {
        isFalse(isTrainingComplete(params),
                String.format("Training invoked %d times which is greater than the training window of period * 2 (%d * 2 = %d) observations.",
                        n + 1, params.getPeriod(), params.getInitTrainingPeriod()));
    }

    // TODO HW: Potential reuse opportunity
    private boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    // TODO HW: Potential reuse opportunity
    private double mean(double[] values) {
        return Arrays.stream(values).average().getAsDouble();
    }

}
