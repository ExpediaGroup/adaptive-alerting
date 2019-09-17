package com.expedia.adaptivealerting.metrics.functions.kafka;

import com.expedia.adaptivealerting.metrics.functions.source.GraphiteQueryResult;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class KafkaMetricFunctionsToAAMetricsTest {

    @Test
    public void teststeamAggregateRecord(){
        val sampleJsonGraphite = "[{" + "\"datapoints\": [[12.0, 1568255056]], " +
                "\"target\": \"sumSeries(a.b.c)\", " +
                "\"tags\":" + "{" + "\"aggregatedBy\": \"sum\", \"name\": \"sumSeries(a.b.c)\"" + "}}]";
        val functionsInputFile = "/config/functions-test.txt";
        MetricFunctionsSpec metricFunctionsSpec = MetricFunctionsReader.readFromInputFile(functionsInputFile).get(0);
        GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
        graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphite);
        MetricData metricDataTest = KafkaMetricFunctionsToAAMetrics.streamAggregateRecord(graphiteQueryResult, metricFunctionsSpec);
        MetricDefinition metricDefinitionTest = metricDataTest.getMetricDefinition();
        assertEquals("sumSeries(a.b.c)", metricDefinitionTest.getKey());
        assertEquals(4, metricDefinitionTest.getTags().getKv().size());
        assertEquals(TagCollection.EMPTY, metricDefinitionTest.getMeta());
        assertEquals(12.0, metricDataTest.getValue(), 0.1);
        assertEquals(1568255056, metricDataTest.getTimestamp());
    }
}
