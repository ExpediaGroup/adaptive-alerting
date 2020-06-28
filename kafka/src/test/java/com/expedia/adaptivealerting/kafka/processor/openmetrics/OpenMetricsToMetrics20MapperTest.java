package com.expedia.adaptivealerting.kafka.processor.openmetrics;

import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenMetricsToMetrics20MapperTest {

    private static final String METRIC_NAME = "your_metric_name";
    private static final String METRIC_DESCRIPTION = "A helpful description of your measurement.";

    @Test
    public void testInitial() {
        final String metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total 1.000000\n" +
            "your_metric_name_total 2.000000 1541323663\n" +
            "your_metric_name_total{label1=\"label_value\"} 3.000000\n" +
            "your_metric_name_total{label2=\"label_value\"} 4.000000 1541323663\n" +
            "your_metric_name_total 5.000000\n" +
            "your_metric_name_total 6.000000 1541323663\n" +
            "your_metric_name_total{label3=\"label_value\"} 7.000000\n" +
            "your_metric_name_total{label4=\"label_value\", label5=\"label_value\"} 8.000000 1541323663";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);
        Assert.assertEquals(8, openMetricRecordList.size());
    }

    @Test
    public void testCounterOnlyValue() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total 1.000000\n";
        val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(1.0d)
                .helpDescription(METRIC_DESCRIPTION).build(),
            openMetricRecord
        );
    }

    @Test
    public void testCounterValueWithTimestamp() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total 2.000000 1541323663\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());
        val openMetricRecord = openMetricRecordList.get(0);
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(2.0d)
                .helpDescription(METRIC_DESCRIPTION).timestamp(1541323663.0).build(),
            openMetricRecord);
    }

    @Test
    public void testCounterValueWithSingleLabel() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total{label1=\"label_value\"} 3.000000\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("label1", "label_value");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(3.0d)
                .labelsMap(labelsMap).helpDescription(METRIC_DESCRIPTION).build(),
            openMetricRecord);
    }

    @Test
    public void testCounterValueWithSingleLabelAndTimestamp() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total{label2=\"label2_value\"} 4.000000 1541323663\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("label2", "label2_value");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(4.0d)
                .labelsMap(labelsMap).helpDescription(METRIC_DESCRIPTION).timestamp(1541323663.0).build(),
            openMetricRecord);
    }

    @Test
    public void testCounterValueWithMultiLabelAndTimestamp() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total{label4=\"label4_value\", label5=\"label5_value\"} 8.000000 1541323663";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("label4", "label4_value");
        labelsMap.put("label5", "label5_value");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(8.0d)
                .labelsMap(labelsMap).helpDescription(METRIC_DESCRIPTION).timestamp(1541323663.0).build(),
            openMetricRecord);
    }

    @Test
    public void testCounterExtractLabelsWithComma() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total{label4=\"label4_value, some statement\", label5=\"label5_value\"} 8.000000 1541323663";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("label4", "label4_value, some statement");
        labelsMap.put("label5", "label5_value");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(8.0d)
                .labelsMap(labelsMap).helpDescription(METRIC_DESCRIPTION).timestamp(1541323663.0).build(),
            openMetricRecord);
    }

    @Test
    public void testCounterExtractLabelsWithEscapingDoubleQoutes() {
        final val metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total{a=\"x\",b=\"escaping\\\" example \\n \"} 8.000000 1541323663";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("a", "x");
        labelsMap.put("b", "escaping\\\" example \\n ");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.COUNTER).metricName(METRIC_NAME).suffix("total").value(8.0d)
                .labelsMap(labelsMap).helpDescription(METRIC_DESCRIPTION).timestamp(1541323663.0).build(),
            openMetricRecord);
    }


    @Test
    public void testCounterGauge() {
        final val metricsStr = "# TYPE foo gauge\n" +
            "foo 17.0\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.GAUGE).metricName("foo").value(17.0d).build(),
            openMetricRecord);
    }

    @Test
    public void testUnknownMetricType() {
        final val metricsStr = "# TYPE foo unknown\n" +
            "foo{a=\"b\"} 17.0 1520879607.789\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);
        Assert.assertEquals(1, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("a", "b");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.UNKNOWN).metricName("foo")
                .value(17.0d).timestamp(1520879607.789d).labelsMap(labelsMap).build(),
            openMetricRecord);
    }

    @Test
    public void testInfoMetricType() {
        final val metricsStr = "# TYPE foo info\n" +
            "foo_info{entity=\"controller\",name=\"pretty name\",version=\"8.2.7\"} 1\n" +
            "foo_info{entity=\"replica\",name=\"prettier name\",version=\"8.1.9\"} 1\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);
        Assert.assertEquals(2, openMetricRecordList.size());

        val openMetricRecord = openMetricRecordList.get(0);
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("entity", "controller");
        labelsMap.put("name", "pretty name");
        labelsMap.put("version","8.2.7");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.INFO).metricName("foo").suffix("info")
                .value(1d).labelsMap(labelsMap).build(),
            openMetricRecord);
    }

    @Test
    public void testSummaryMetricType() {
        final val metricsStr = "# TYPE foo summary\n" +
            "foo{quantile=\"0.95\"} 123.7\n" +
            "foo{quantile=\"0.99\"} 150.0\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(2, openMetricRecordList.size());

        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("quantile", "0.95");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.SUMMARY).metricName("foo").value(123.7d).labelsMap(labelsMap).build(),
            openMetricRecordList.get(0));

        Map<String, String> labelsMap2 = new HashMap<>();
        labelsMap2.put("quantile", "0.99");

        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.SUMMARY).metricName("foo").value(150.0d).labelsMap(labelsMap2).build(),
            openMetricRecordList.get(1));
    }

    @Test
    public void testSummaryMetricTypeWithUnit() {
        final val metricsStr = "# TYPE foo_seconds summary\n" +
            "# UNIT foo_seconds seconds\n" +
            "foo_seconds_count{a=\"b\"} 0  123\n" +
            "foo_seconds_sum{a=\"b\"} 0  123\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);

        Assert.assertEquals(2, openMetricRecordList.size());
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("a", "b");
        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.SUMMARY).metricName("foo_seconds").suffix("count")
                .metricUnit("seconds").value(0.0d).timestamp(123.0d).labelsMap(labelsMap).build(),
            openMetricRecordList.get(0));

        Assert.assertEquals(
            OpenMetricRecord.builder().metricType(MetricType.SUMMARY).metricName("foo_seconds").suffix("sum")
                .metricUnit("seconds").value(0.0d).timestamp(123.0d).labelsMap(labelsMap).build(),
            openMetricRecordList.get(1));
    }

    @Test
    public void testHistogramMetricType() {
        final val metricsStr = "# TYPE foo histogram\n" +
            "foo_bucket{le=\"0.0\"} 0\n" +
            "foo_bucket{le=\"1e-05\"} 0\n" +
            "foo_bucket{le=\"0.0001\"} 0\n" +
            "foo_bucket{le=\"0.1\"} 8\n" +
            "foo_bucket{le=\"1.0\"} 10\n" +
            "foo_bucket{le=\"10.0\"} 17\n" +
            "foo_bucket{le=\"100000.0\"} 17\n" +
            "foo_bucket{le=\"1e+06\"} 17\n" +
            "foo_bucket{le=\"1e+23\"} 17\n" +
            "foo_bucket{le=\"+Inf\"} 17\n" +
            "foo_count 17\n" +
            "foo_sum 324789.3\n" +
            "foo_created 1520430000.123\n";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val openMetricRecordList = convertor.parse(metricsStr);
        Assert.assertEquals(13, openMetricRecordList.size());

        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("le", "0.0");
        Assert.assertEquals(
            OpenMetricRecord.builder()
                .metricName("foo").metricType(MetricType.HISTOGRAM).suffix("bucket").labelsMap(labelsMap).value(0.0d)
                .build(),
            openMetricRecordList.get(0));

        Assert.assertEquals(
            OpenMetricRecord.builder()
                .metricName("foo").metricType(MetricType.HISTOGRAM).suffix("count").value(17.0d)
                .build(),
            openMetricRecordList.get(10));

        Assert.assertEquals(
            OpenMetricRecord.builder()
                .metricName("foo").metricType(MetricType.HISTOGRAM).suffix("sum").value(324789.3d)
                .build(),
            openMetricRecordList.get(11));

        Assert.assertEquals(
            OpenMetricRecord.builder()
                .metricName("foo").metricType(MetricType.HISTOGRAM).suffix("created").value(1520430000.123d)
                .build(),
            openMetricRecordList.get(12));

    }

    @Test
    public void testConvertingCounterMetricsWithTimestampsToMetric20() throws IOException
    {
        final String metricsStr = "# TYPE your_metric_name counter\n" +
            "# HELP your_metric_name A helpful description of your measurement.\n" +
            "your_metric_name_total 1.000000\n" +
            "your_metric_name_total 2.000000 1541323663\n" +
            "your_metric_name_total{label1=\"label_value\"} 3.000000\n" +
            "your_metric_name_total{label2=\"label_value\"} 4.000000 1541323663\n" +
            "your_metric_name_total 5.000000\n" +
            "your_metric_name_total 6.000000 1541323663\n" +
            "your_metric_name_total{label3=\"label_value\"} 7.000000\n" +
            "your_metric_name_total{label4=\"label_value\", label5=\"label_value\"} 8.000000 1541323663";
        final val convertor = new OpenMetricsToMetrics20Mapper();
        final val metricDataList = convertor.convert(metricsStr);
        Assert.assertEquals(4, metricDataList.size());
        System.out.println(new ObjectMapper().writerFor(List.class).writeValueAsString(metricDataList));
    }
}
