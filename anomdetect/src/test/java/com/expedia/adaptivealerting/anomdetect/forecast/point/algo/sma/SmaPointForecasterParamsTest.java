package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.sma;

import java.util.Arrays;

import org.junit.Test;

public class SmaPointForecasterParamsTest {

    @Test
    public void validParamsTotallyEmpty() {
        new SmaPointForecasterParams()
            .validate();
    }

    @Test
    public void validParamsLookbackOnly() {
        new SmaPointForecasterParams()
            .setLookBackPeriod(3)
            .validate();
    }

    @Test
    public void validParamsFullyPopulated() {
        new SmaPointForecasterParams()
            .setLookBackPeriod(3)
            .setInitialPeriodOfValues(Arrays.asList(1.0, 2.0, 3.0))
            .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLookbackPeriod() {
        new SmaPointForecasterParams()
            .setLookBackPeriod(-1)
            .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidInitialPeriodOfValues() {
        new SmaPointForecasterParams()
            .setLookBackPeriod(2)
            .setInitialPeriodOfValues(Arrays.asList(1.0, 2.0, 3.0))
            .validate();
    }

}
