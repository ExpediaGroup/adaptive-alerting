package com.expedia.adaptivealerting.anomdetect.source.data.initializer.throttlegate;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class RandomThrottleGateTest {

    private static final double THROTTLE_GATE_LIKELIHOOD = 0.05;
    private static final int MAX_ITERATIONS = 1000;
    private static final int EXPECTED_OPEN_GATES = (int) (THROTTLE_GATE_LIKELIHOOD * MAX_ITERATIONS);
    private static final double VARIANCE = 0.7; // with respect to EXPECTED_OPEN_GATES
    private static final int UPPER_GATE_LIMIT = (int) (EXPECTED_OPEN_GATES + EXPECTED_OPEN_GATES * VARIANCE);
    private static final int LOWER_GATE_LIMiT = (int) (EXPECTED_OPEN_GATES - EXPECTED_OPEN_GATES * VARIANCE);

    private RandomThrottleGate throttleGateUnderTest;

    @Before
    public void setUp() throws Exception {
        this.throttleGateUnderTest = spy(new RandomThrottleGate(THROTTLE_GATE_LIKELIHOOD));
    }

    @Test
    public void isOpenStatisticTest() {
        int openGateCounter = 0;
        for (int i = 0; i < 1000; i++) {
            if (this.throttleGateUnderTest.isOpen()) {
                openGateCounter = openGateCounter + 1;
            }
        }
        assertTrue(openGateCounter > LOWER_GATE_LIMiT && openGateCounter < UPPER_GATE_LIMIT);
    }
}