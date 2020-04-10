package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.kafka.visualizer.AnomalyModel;
import org.elasticsearch.client.Response;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

public class AnomalyVisualizerTest {

    public AnomalyVisualizer anomalyVisualizer;


    @Before
    public void setUp() {
        anomalyVisualizer = new AnomalyVisualizer();
    }

    @Test
    public void testPutdatatoElasticSearch() {


    }

    @Test
    public void testConvertTimestamp() {

    }
}
