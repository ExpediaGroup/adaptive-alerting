package com.expedia.adaptivealerting.anomdetect.util;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertiesUtilTest {

    @Test
    public void testGetValueFromProperty() {
        val baseUri = PropertiesUtil.getValueFromProperty("graphite.baseUri");
        assertEquals("http://graphite", baseUri);

        val earliestTime = PropertiesUtil.getValueFromProperty("graphite.earliestTime");
        assertEquals("7d", earliestTime);

        val maxDataDataPoints = PropertiesUtil.getValueFromProperty("graphite.maxDataDataPoints");
        assertEquals(2016, Integer.parseInt(maxDataDataPoints));

        val functionTagKey = PropertiesUtil.getValueFromProperty("graphite.functionTagKey");
        assertEquals("function", functionTagKey);
    }
}
