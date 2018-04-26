package com.expedia.haystack.adaptivealerting.anomaly.util;

import org.junit.Test;

import static com.expedia.haystack.adaptivealerting.anomaly.util.AssertUtil.isTrue;

/**
 * @author Willie Wheeler
 */
public class AssertUtilTests {
    
    @Test
    public void testIsTrue_trueValue() {
        // Expect this to work without throwing an exception
        isTrue(true, "The param must be true");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsTrue_falseValue() {
        isTrue(false, "The param must be true");
    }
}
