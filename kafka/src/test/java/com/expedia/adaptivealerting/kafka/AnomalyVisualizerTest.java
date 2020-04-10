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
        AnomalyModel anomalyModel = new AnomalyModel();
        anomalyModel.setUuid("asca");
        anomalyModel.setTimestamp(new Date());
        anomalyModel.setKey("key");
        anomalyModel.setLevel("level");
        String json = anomalyVisualizer.convertToJson(anomalyModel);
        Response response = anomalyVisualizer.sendMetricsToESSearch(json);
        assertNotNull(response);

    }

    @Test
    public void testConvertTimestamp() {
        long timestamp = 1586453367;
        Date time = anomalyVisualizer.convertToDate(timestamp);
        assertNotNull(time);
    }
}
