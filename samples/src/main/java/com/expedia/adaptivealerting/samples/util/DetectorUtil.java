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
package com.expedia.adaptivealerting.samples.util;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.algo.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersSeasonalityType;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.EwmaPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.HoltWintersTrainingMethod;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.PewmaPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.ExponentialWelfordIntervalForecasterParams;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.UUID;

@UtilityClass
public class DetectorUtil {
    private static final int HW_PERIOD = 4;
    private static final double HW_WEAK_SIGMAS = 2.0;
    private static final double HW_STRONG_SIGMAS = 3.0;

    public static Detector buildEwmaDetector() {
        return buildEwmaDetector(new EwmaPointForecasterParams());
    }

    public static Detector buildPewmaDetector() {
        return buildPewmaDetector(new PewmaPointForecasterParams());
    }

    public static Detector buildHoltWintersDetector(
            HoltWintersSeasonalityType seasonalityType,
            double level,
            double base,
            double[] seasonal,
            double alpha,
            double beta,
            double gamma,
            HoltWintersTrainingMethod initTrainingMethod) {

        return buildHoltWintersDetector(new HoltWintersPointForecasterParams()
                .setFrequency(HW_PERIOD)
                .setAlpha(alpha)
                .setBeta(beta)
                .setGamma(gamma)
                .setSeasonalityType(seasonalityType)
                .setWarmUpPeriod(HW_PERIOD)
                .setInitLevelEstimate(level)
                .setInitBaseEstimate(base)
                .setInitSeasonalEstimates(seasonal)
                .setInitTrainingMethod(initTrainingMethod));
    }

    private static Detector buildEwmaDetector(EwmaPointForecasterParams params) {
        val ewma = new EwmaPointForecaster(params);
        val welfordParams = new ExponentialWelfordIntervalForecasterParams()
                .setAlpha(params.getAlpha());
        val welford = new ExponentialWelfordIntervalForecaster(welfordParams);
        return new ForecastingDetector(UUID.randomUUID(), ewma, welford, AnomalyType.RIGHT_TAILED);
    }

    private static Detector buildPewmaDetector(PewmaPointForecasterParams params) {
        val pewma = new PewmaPointForecaster(params);
        val welfordParams = new ExponentialWelfordIntervalForecasterParams()
                .setAlpha(params.getAlpha());
        val welford = new ExponentialWelfordIntervalForecaster(welfordParams);
        return new ForecastingDetector(UUID.randomUUID(), pewma, welford, AnomalyType.RIGHT_TAILED);
    }

    private static Detector buildHoltWintersDetector(HoltWintersPointForecasterParams params) {
        val holtWinters = new HoltWintersPointForecaster(params);
        val welfordParams = new ExponentialWelfordIntervalForecasterParams()
                .setAlpha(params.getAlpha())
                .setStrongSigmas(HW_STRONG_SIGMAS)
                .setWeakSigmas(HW_WEAK_SIGMAS);
        val welford = new ExponentialWelfordIntervalForecaster(welfordParams);
        return new ForecastingDetector(
                UUID.randomUUID(),
                holtWinters,
                welford,
                AnomalyType.RIGHT_TAILED);
    }
}
