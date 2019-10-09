package com.expedia.adaptivealerting.metrics.functions.source;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

@Slf4j
public class MetricFunctionsReaderTest {

    @Test
    public void testReadFromInputFile(){
        val functionInputFileName = "config/functions-test.txt";
        List<MetricFunctionsSpec> metricFunctionsSpecList = MetricFunctionsReader.readFromInputFile
                (ClassLoader.getSystemResource(functionInputFileName).getPath());
        assertEquals(1, metricFunctionsSpecList.size());
        MetricFunctionsSpec metricFunctionsSpec = metricFunctionsSpecList.get(0);
        assertEquals("sumSeries(a.b.c)", metricFunctionsSpec.getFunction());
        assertEquals(30, metricFunctionsSpec.getIntervalInSecs());
        Iterator it = metricFunctionsSpec.getTags().entrySet().iterator();
        Map.Entry tag1 = (Map.Entry)it.next();
        assertEquals(tag1.getKey(), "app_name");
        assertEquals(tag1.getValue(), "sample_app1");
        Map.Entry tag2 = (Map.Entry)it.next();
        assertEquals(tag2.getKey(), "env");
        assertEquals(tag2.getValue(), "test");
    }

    @Test
    public void testReadFromInputFileException() throws Exception {
        val invalidFileName = "/config/no-such-file-test.txt";
        List<MetricFunctionsSpec> metricFunctionsSpecList = MetricFunctionsReader.readFromInputFile(invalidFileName);
        if (metricFunctionsSpecList.isEmpty()) {
            log.error("Exception reading input file, exiting");
        }
    }
}
