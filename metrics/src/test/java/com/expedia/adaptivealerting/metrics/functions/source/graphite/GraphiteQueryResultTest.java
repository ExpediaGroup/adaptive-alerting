package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import lombok.val;
import org.junit.Test;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GraphiteQueryResultTest {

    @Test
    public void testgetGraphiteQueryResultFromJson(){
       val sampleJsonGraphite = "[{" + "\"datapoints\": [[12.0, 1568255056]], " +
               "\"target\": \"sumSeries(a.b.c)\", " +
               "\"tags\":" + "{" + "\"aggregatedBy\": \"sum\", \"name\": \"sumSeries(a.b.c)\"" + "}}]";
       GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
       graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphite);
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
   public void testgetGraphiteQueryResultFromJsonDefaultDatapoint(){
      val sampleJsonGraphite = "[{" + "\"datapoints\": [[]], " +
              "\"target\": \"sumSeries(a.b.c)\", " +
              "\"tags\":" + "{" + "\"aggregatedBy\": \"sum\", \"name\": \"sumSeries(a.b.c)\"" + "}}]";
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphite);
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
   public void testgetGraphiteQueryResultFromJsonDefaultTarget(){
      val sampleJsonGraphite = "[{" + "\"datapoints\": [[12.0, 1568255056]], " +
              "\"target\": \"\", " +
              "\"tags\":" + "{" + "\"aggregatedBy\": \"sum\", \"name\": \"sumSeries(a.b.c)\"" + "}}]";
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphite);
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
   public void testgetGraphiteQueryResultFromJsonDefaultTags(){
      val sampleJsonGraphite = "[{" + "\"datapoints\": [[12.0, 1568255056]], " +
              "\"target\": \"sumSeries(a.b.c)\", " +
              "\"tags\": {} " + "}]";
      GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
      graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphite);
      assertEquals(12.0, graphiteQueryResult.getDatapoint().getValue(), 0.1);
      assertEquals(1568255056, graphiteQueryResult.getDatapoint().getTimestamp());
      assertEquals("sumSeries(a.b.c)", graphiteQueryResult.getTarget());
      assertEquals(graphiteQueryResult.getTags().size(), 0);
   }
}
