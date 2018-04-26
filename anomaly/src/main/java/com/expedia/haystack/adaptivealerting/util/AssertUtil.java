package com.expedia.haystack.adaptivealerting.util;

/**
 * Assertion utilities.
 *
 * @author Willie Wheeler
 */
public class AssertUtil {
    
    public static void isTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }
}
