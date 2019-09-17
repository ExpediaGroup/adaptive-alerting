package com.expedia.adaptivealerting.metrics.functions.util;

import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.typesafe.config.Config;
import lombok.val;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ConstructSourceURITest {

    @Test
    public void testgetGraphiteURI() {
        val functionsInputFileName = "/config/functions-test.txt";
        val uri = "samplegraphitehosturi/render?target=sumSeries(a.b.c)&from=-30s&until=now&format=json";
        MetricFunctionsSpec metricFunctionsSpec = MetricFunctionsReader.readFromInputFile(functionsInputFileName).get(0);
        Config config = new TypesafeConfigLoader("aa-metric-functions-test").loadMergedConfig();
        val metricSourceSinkConfigTest = config.getConfig("metric-source-sink");
        ConstructSourceURI constructSourceURI = new ConstructSourceURI();
        assertEquals(uri, constructSourceURI.getGraphiteURI(metricSourceSinkConfigTest, metricFunctionsSpec));;
    }
}
