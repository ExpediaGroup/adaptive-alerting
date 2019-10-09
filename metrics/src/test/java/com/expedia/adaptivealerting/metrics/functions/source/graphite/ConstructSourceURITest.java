package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import com.expedia.adaptivealerting.metrics.functions.TypesafeConfigLoader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.typesafe.config.Config;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstructSourceURITest {

    @Test
    public void testGetGraphiteURI() {
        val functionsInputFileName = "config/functions-test.txt";
        val uri = "samplegraphitehosturi/render?until=now&format=json&target=sumSeries(a.b.c)&from=-30s";
        MetricFunctionsSpec metricFunctionsSpec = MetricFunctionsReader.
                readFromInputFile(ClassLoader.getSystemResource(functionsInputFileName).getPath()).get(0);
        Config config = new TypesafeConfigLoader("aa-metric-functions-test").loadMergedConfig();
        val metricSourceSinkConfigTest = config.getConfig("metric-source-sink");
        ConstructSourceURI constructSourceURI = new ConstructSourceURI();
        assertEquals(uri, constructSourceURI.getGraphiteURI(metricSourceSinkConfigTest, metricFunctionsSpec));;
    }
}
