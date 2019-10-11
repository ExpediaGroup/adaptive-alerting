package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import lombok.val;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class GraphiteQueryResultTest {

   private String sampleJsonGraphiteString;

    public void setupJsonGraphite(String testDatapoint, String testTarget,
                      HashMap testTags){
       JSONArray sampleJsonGraphite = new JSONArray();
       JSONObject sampleJsonGraphiteResult = new JSONObject();
       JSONArray testDatapoints = new JSONArray();
       testDatapoints.put(0, testDatapoint);
       sampleJsonGraphiteResult.put("datapoints", testDatapoints);
       sampleJsonGraphiteResult.put("target", testTarget);
       sampleJsonGraphiteResult.put("tags", testTags);
       sampleJsonGraphite.put(0, sampleJsonGraphiteResult);
       sampleJsonGraphiteString = sampleJsonGraphite.toString();
    }

    @Test
    public void testGetGraphiteQueryResultFromJson(){
       HashMap testTags = new HashMap();
       testTags.put("aggregatedBy", "sum");
       testTags.put("name", "sumSeries(a.b.c)");
       setupJsonGraphite("[12.0,1568255056]","sumSeries(a.b.c)",
               testTags);
       GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
       graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphiteString);
       assertEquals(12.0, graphiteQueryResult.getDatapoint().getValue(), 0.1);
       assertEquals(1568255056, graphiteQueryResult.getDatapoint().getTimestamp());
       assertEquals("sumSeries(a.b.c)", graphiteQueryResult.getTarget());
       Iterator it = graphiteQueryResult.getTags().entrySet().iterator();
       Map.Entry tag1 = (Map.Entry)it.next();
       assertEquals(tag1.getKey(), "aggregatedBy");
       assertEquals(tag1.getValue(), "sum");
       Map.Entry tag2 = (Map.Entry)it.next();
       assertEquals(tag2.getKey(), "name");
       assertEquals(tag2.getValue(), "sumSeries(a.b.c)");
    }

   @Test
   public void testGetGraphiteQueryResultFromJsonDefaultDatapoint(){
      HashMap testTags = new HashMap();
      testTags.put("aggregatedBy", "sum");
      testTags.put("name", "sumSeries(a.b.c)");
      setupJsonGraphite("[0.0,0]","sumSeries(a.b.c)",
              testTags);
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphiteString);
      assertEquals(0.0, graphiteQueryResult.getDatapoint().getValue(), 0.0);
      assertEquals(0, graphiteQueryResult.getDatapoint().getTimestamp());
      assertEquals("sumSeries(a.b.c)", graphiteQueryResult.getTarget());
      Iterator it = graphiteQueryResult.getTags().entrySet().iterator();
      Map.Entry tag1 = (Map.Entry)it.next();
      assertEquals(tag1.getKey(), "aggregatedBy");
      assertEquals(tag1.getValue(), "sum");
      Map.Entry tag2 = (Map.Entry)it.next();
      assertEquals(tag2.getKey(), "name");
      assertEquals(tag2.getValue(), "sumSeries(a.b.c)");
   }

   @Test
   public void testGetGraphiteQueryResultFromJsonDefaultTarget(){
      HashMap testTags = new HashMap();
      testTags.put("aggregatedBy", "sum");
      testTags.put("name", "sumSeries(a.b.c)");
      setupJsonGraphite("[12.0,1568255056]","",
              testTags);
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphiteString);
      assertEquals(12.0, graphiteQueryResult.getDatapoint().getValue(), 0.1);
      assertEquals(1568255056, graphiteQueryResult.getDatapoint().getTimestamp());
      assertEquals("", graphiteQueryResult.getTarget());
      Iterator it = graphiteQueryResult.getTags().entrySet().iterator();
      Map.Entry tag1 = (Map.Entry)it.next();
      assertEquals(tag1.getKey(), "aggregatedBy");
      assertEquals(tag1.getValue(), "sum");
      Map.Entry tag2 = (Map.Entry)it.next();
      assertEquals(tag2.getKey(), "name");
      assertEquals(tag2.getValue(), "sumSeries(a.b.c)");
   }

   @Test
   public void testGetGraphiteQueryResultFromJsonDefaultTags(){
      HashMap testTags = new HashMap();
      setupJsonGraphite("[12.0,1568255056]","sumSeries(a.b.c)",
              testTags);
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphiteString);
      assertEquals(12.0, graphiteQueryResult.getDatapoint().getValue(), 0.1);
      assertEquals(1568255056, graphiteQueryResult.getDatapoint().getTimestamp());
      assertEquals("sumSeries(a.b.c)", graphiteQueryResult.getTarget());
      assertEquals(graphiteQueryResult.getTags().size(), 0);
   }

   @Test
   public void testGetGraphiteQueryResultFromJsonNullDatapoint(){
      HashMap testTags = new HashMap();
      testTags.put("aggregatedBy", "sum");
      testTags.put("name", "sumSeries(a.b.c)");
      setupJsonGraphite("[null,1568255056]","sumSeries(a.b.c)",
              testTags);
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      assertTrue(graphiteQueryResult.validateNullDatapoint(sampleJsonGraphiteString));
   }

   @Test
   public void testGetGraphiteQueryResultFromJsonNonNullDatapoint(){
      HashMap testTags = new HashMap();
      testTags.put("aggregatedBy", "sum");
      testTags.put("name", "sumSeries(a.b.c)");
      setupJsonGraphite("[12.0,1568255056]","sumSeries(a.b.c)",
              testTags);
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      assertFalse(graphiteQueryResult.validateNullDatapoint(sampleJsonGraphiteString));
   }

   @Test
   public void testJsonDatapointGraphiteResultJsonArrayZeroLength(){
      val sampleJsonGraphite = "[]";
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      assertFalse(graphiteQueryResult.validateNullDatapoint(sampleJsonGraphite));
   }

   @Test
   public void testJsonDatapointArrayZeroLength(){
      HashMap testTags = new HashMap();
      testTags.put("aggregatedBy", "sum");
      testTags.put("name", "sumSeries(a.b.c)");
      setupJsonGraphite("[]","sumSeries(a.b.c)",
              testTags);
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      assertFalse(graphiteQueryResult.validateNullDatapoint(sampleJsonGraphiteString));
   }
}
