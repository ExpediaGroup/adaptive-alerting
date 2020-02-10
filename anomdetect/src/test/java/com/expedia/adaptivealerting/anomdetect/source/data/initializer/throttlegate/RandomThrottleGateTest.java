package com.expedia.adaptivealerting.anomdetect.source.data.initializer.throttlegate;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;

public class RandomThrottleGateTest {

    private static final double THROTTLE_GATE_LIKELIHOOD = 0.05;
    private static final int MAX_ITERATIONS = 10000;
    private static final int MIN_OPEN_GATES = 1;
    private static final int MAX_OPEN_GATES = MAX_ITERATIONS - 1;

    private RandomThrottleGate throttleGateUnderTest;

    @Before
    public void setUp() {
        this.throttleGateUnderTest = new RandomThrottleGate(THROTTLE_GATE_LIKELIHOOD);
    }

    @Test
    public void testNeverFullyOpenOrFullyClosed() {
        int openGateCounter = (int) IntStream.range(0, MAX_ITERATIONS).filter(i -> this.throttleGateUnderTest.isOpen()).count();
        String message = "Gate opened " + openGateCounter + "/" + MAX_ITERATIONS + " times " +
                "(expecting at least " + MIN_OPEN_GATES + " and a maximum of " + MAX_OPEN_GATES + ")";
        isBetween(openGateCounter, MIN_OPEN_GATES, MAX_OPEN_GATES, message);
        System.out.println(message); // Show result of test even if it passes - can track progress via build logs
    }
}