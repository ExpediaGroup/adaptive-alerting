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
    public void testReadFromInputFile() {
        val functionInputFileName = "config/functions-test.txt";
        List<MetricFunctionsSpec> metricFunctionsSpecList = MetricFunctionsReader
                .readFromInputFile(ClassLoader.getSystemResource(functionInputFileName).getPath());
        assertEquals(1, metricFunctionsSpecList.size());
        MetricFunctionsSpec metricFunctionsSpec = metricFunctionsSpecList.get(0);
        assertEquals("sumSeries(a.b.c)", metricFunctionsSpec.getFunction());
        assertEquals(60, metricFunctionsSpec.getIntervalInSecs());
        Map<String, String> tags = metricFunctionsSpec.getTags();
        assertEquals(3, tags.size());
        assertEquals("sample_app1", tags.get("app_name"));
        assertEquals("test", tags.get("env"));
        assertEquals("custom_tag_value", tags.get("custom_tag"));
        assertEquals(true, metricFunctionsSpec.getMergeTags());
    }

    @Test
    public void testReadFromInputFileMergeTagsFalse() {
        val functionInputFileName = "config/functions-mergeTags-false-test.txt";
        List<MetricFunctionsSpec> metricFunctionsSpecList = MetricFunctionsReader
                .readFromInputFile(ClassLoader.getSystemResource(functionInputFileName).getPath());
        assertEquals(1, metricFunctionsSpecList.size());
        MetricFunctionsSpec metricFunctionsSpec = metricFunctionsSpecList.get(0);
        assertEquals(false, metricFunctionsSpec.getMergeTags());
    }

    @Test
    public void testReadFromInputFileException() throws Exception {
        val invalidFileName = "config/no-such-file-test.txt";
        List<MetricFunctionsSpec> metricFunctionsSpecList = MetricFunctionsReader.readFromInputFile(invalidFileName);
        if (metricFunctionsSpecList.isEmpty()) {
            log.error("Exception reading input file, exiting");
        }
    }
}
