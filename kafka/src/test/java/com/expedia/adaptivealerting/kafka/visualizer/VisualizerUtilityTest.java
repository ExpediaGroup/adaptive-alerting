package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static com.expedia.adaptivealerting.kafka.visualizer.VisualizerUtility.convertToDate;
import static com.expedia.adaptivealerting.kafka.visualizer.VisualizerUtility.convertToJson;
import static com.expedia.adaptivealerting.kafka.visualizer.VisualizerUtility.getConfig;
import static com.expedia.adaptivealerting.kafka.visualizer.VisualizerUtility.getMetricConsumerProps;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class VisualizerUtilityTest {

    private static Config config;

    @Before
    public void setUp() {
        config = new TypesafeConfigLoader("visualizer").loadMergedConfig();
    }

    @Test
    public void testLoadConfig() {
        assertNotNull(config);
    }

    @Test
    public void testConvertJson() {
        AnomalyModel anomalyModel = AnomalyModel.newBuilder().build();
        assertNotNull(convertToJson(anomalyModel));
    }

    public void testConvertToDate() {
        long time = 1587665081L;
        String dateTime = convertToDate(time);
        assertNotNull(dateTime);
        assertTrue(dateTime.contains("2020"));
        assertTrue(dateTime.contains("04"));
        assertTrue(dateTime.contains("23"));
        assertTrue(dateTime.contains("13"));
    }

    @Test
    public void testMetricConsumerProps() {
        Properties properties = getMetricConsumerProps(getConfig("metric-consumer"));
        assertNotNull(properties);
        assertTrue(properties.getProperty("key.deserializer")
                .equalsIgnoreCase("org.apache.kafka.common.serialization.StringDeserializer"));
        assertTrue(properties.getProperty("bootstrap.servers")
                .equalsIgnoreCase("localhost:19092"));
        assertTrue(properties.getProperty("group.id").equalsIgnoreCase("my-group"));
        assertTrue(properties.getProperty("value.deserializer")
                .equalsIgnoreCase("com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Deser"));
    }
}
