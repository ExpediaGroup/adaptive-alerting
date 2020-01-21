package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.anomdetect.source.data.initializer.DataInitializer;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertiesUtilTest {

    @Test
    public void testGetValueFromProperty() {
        val baseUri = PropertiesUtil.getValueFromProperty(DataInitializer.BASE_URI);
        val earliestTime = PropertiesUtil.getValueFromProperty(DataInitializer.EARLIEST_TIME);
        val maxDataDataPoints = PropertiesUtil.getValueFromProperty(DataInitializer.MAX_DATA_POINTS);
        val functionTagKey = PropertiesUtil.getValueFromProperty(MetricUtil.DATA_RETRIEVAL_TAG_KEY);

        assertEquals("http://graphite", baseUri);
        assertEquals("7d", earliestTime);
        assertEquals(2016, Integer.parseInt(maxDataDataPoints));
        assertEquals("function", functionTagKey);
    }
}
