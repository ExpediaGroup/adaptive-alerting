package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import lombok.val;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class GraphiteResultTest {

    public static List<GraphiteResult> readGraphiteFile(String filePath) {
        List<GraphiteResult> graphiteResults = new ArrayList();
        String content = "";
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(filePath).getPath())));
            graphiteResults = Arrays.asList(objectMapper.readValue(content.getBytes(), GraphiteResult[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graphiteResults;
    }

    @Test
    public void testGetGraphiteQueryResultFromJson() {
        List<GraphiteResult> graphiteResults = readGraphiteFile("tests/validGraphiteResponse.json");
        for (GraphiteResult graphiteResult : graphiteResults) {
            Datapoint datapoint = graphiteResult.getDatapoint();
            assertEquals(datapoint.getTimestamp(), 1583039100);
            assertEquals(String.valueOf(datapoint.getValue()), "12.0");
        }
    }

    @Test
    public void testGetGraphiteQueryWithOneNullResultFromJson() {
        List<GraphiteResult> graphiteResults = readGraphiteFile("tests/validGraphiteResponseWithNull.json");
        for (GraphiteResult graphiteResult : graphiteResults) {
            Datapoint datapoint = graphiteResult.getDatapoint();
            assertEquals(datapoint.getTimestamp(), 1583125500);
            assertEquals(String.valueOf(datapoint.getValue()), "155.0");
        }
    }

    @Test
    public void testGetGraphiteQueryWithOnlyNullResultFromJson() throws MissingDatapointException {
        List<GraphiteResult> graphiteResults = readGraphiteFile("tests/invalidGraphiteResponse.json");
        for (GraphiteResult graphiteResult : graphiteResults) {
            try {
                graphiteResult.getDatapoint();
            } catch (MissingDatapointException e) {
                assertTrue(true);
            }
        }
    }

}
