/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.kafka.util;

import com.expedia.adaptivealerting.anomdetect.AnomalyToMetricMapper;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.alertmanager.model.Alert;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

/**
 * <p>
 * Utility for creating test objects.
 * </p>
 * <p>
 * Fine to use this for a small number of objects. If this class gets too big then we would want to switch to the
 * builder pattern.
 * </p>
 */
public final class TestObjectMother {

    /**
     * Prevent instantiation.
     */
    private TestObjectMother() {
    }

    /**
     * Returns a set of metric tags, valid for Metrictank.
     *
     * @return Metrictank-valid metric tags
     */
    public static TagCollection metricTags() {
        val tags = new HashMap<String, String>();

        // Metrics 2.0
        tags.put("mtype", "gauge");
        tags.put("unit", "");

        // Metrictank
        tags.put("org_id", "1");
        tags.put("interval", "1");

        return new TagCollection(tags);
    }

    public static TagCollection metricTagsWithAADetectorUuid() {
        val tags = new HashMap<String, String>();
        tags.put("mtype", "gauge");
        tags.put("unit", "");
        tags.put("org_id", "1");
        tags.put("interval", "1");
        tags.put(AnomalyToMetricMapper.AA_DETECTOR_UUID, UUID.randomUUID().toString());
        return new TagCollection(tags);
    }

    public static TagCollection metricTagsWithNullTagValue() {
        val tags = new HashMap<String, String>();
        tags.put("mtype", "gauge");
        tags.put("unit", "");
        tags.put("org_id", "1");
        tags.put("interval", "1");
        tags.put("some-tag-key", null);
        return new TagCollection(tags);
    }

    public static TagCollection metricMeta() {
        val meta = new HashMap<String, String>();
        return new TagCollection(meta);
    }

    /**
     * Returns a metric data with value 100.0 and timestamp = now.
     *
     * @return metric data
     */
    public static MetricData metricData() {
        return metricData(100.0);
    }

    /**
     * Returns a metric data with the given value and timestamp = now.
     *
     * @param value a value for the metric data
     * @return metric data
     */
    public static MetricData metricData(double value) {
        val metricDef = new MetricDefinition("some-metric-key", metricTags(), metricMeta());
        return metricData(metricDef, value);
    }

    public static MetricData metricData(MetricDefinition metricDef, double value) {
        val now = Instant.now().getEpochSecond();
        return new MetricData(metricDef, value, now);
    }

    /**
     * Returns a mapped metric data with the following characteristics:
     *
     * <ul>
     * <li>metricData.value = 100.0</li>
     * <li>detectorUuid = random</li>
     * <li>detectorType = "constant-detector"</li>
     * <li>anomalyResult = null</li>
     * </ul>
     *
     * @return Mapped metric data
     */
    public static MappedMetricData mappedMetricData() {
        return mappedMetricData(metricData());
    }

    public static MappedMetricData mappedMetricData(MetricData metricData) {
        return new MappedMetricData(metricData, UUID.randomUUID());
    }
    public static MappedMetricData mappedMetricData(MetricData metricData,UUID uuid) {
        return new MappedMetricData(metricData, uuid);
    }

    public static MappedMetricData mappedMetricDataWithAnomalyResult() {
        return mappedMetricDataWithAnomalyResult(metricData());
    }

    public static MappedMetricData mappedMetricDataWithAnomalyResult(MetricData metricData) {
        val mmd = mappedMetricData(metricData);
        mmd.setAnomalyResult(new AnomalyResult(AnomalyLevel.STRONG));
        return mmd;
    }

    public static MappedMetricData mappedMetricData(AnomalyLevel anomalyLevel) {
        val mmd = mappedMetricData();
        mmd.setAnomalyResult(new AnomalyResult(anomalyLevel));
        return mmd;
    }

    public static MappedMetricData mappedMetricDataWithAnomalyResultAndAADetectorUuid() {
        val tags = metricTagsWithAADetectorUuid();
        val metricDef = new MetricDefinition("some-metric-key", tags, TagCollection.EMPTY);
        val metricData = metricData(metricDef, 100.0);
        return mappedMetricDataWithAnomalyResult(metricData);
    }

    public static MappedMetricData mappedMetricDataWithAnomalyResultAndNullTagValue() {
        val tags = metricTagsWithNullTagValue();
        val metricDef = new MetricDefinition("some-metric-key", tags, TagCollection.EMPTY);
        val metricData = metricData(metricDef, 100.0);
        return mappedMetricDataWithAnomalyResult(metricData);
    }

    public static Alert alert() {
        val alert = new Alert();
        alert.setName("some-metric-key");

        val labels = new HashMap<String, String>();
        // Note: metric key can override a tag if it has the same name.
        labels.put("metric_key", "some-metric-key");
        labels.put("mtype", "gauge");
        labels.put("unit", "");
        labels.put("org_id", "1");
        labels.put("interval", "1");
        labels.put("anomalyLevel", "STRONG");
        alert.setLabels(labels);

        val annotations = new HashMap<String, String>();
        annotations.put("value", "100.0");
        annotations.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        alert.setAnnotations(annotations);

        return alert;
    }

    public static TopologyTestDriver topologyTestDriver(
            Topology topology,
            Class<?> valueSerdeClass,
            boolean continueOnDeserException) {

        val props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerdeClass.getName());

        if (continueOnDeserException) {
            props.put(
                    StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                    LogAndContinueExceptionHandler.class.getName());
        }

        return new TopologyTestDriver(topology, props);
    }

    public static ConsumerRecordFactory<String, String> stringFactory() {
        val stringSerializer = new StringSerializer();
        return new ConsumerRecordFactory<>(stringSerializer, stringSerializer);
    }

    public static ConsumerRecordFactory<String, MetricData> metricDataFactory() {
        return new ConsumerRecordFactory<>(new StringSerializer(), new MetricDataJsonSerde.Ser());
    }

    public static ConsumerRecordFactory<String, MappedMetricData> mappedMetricDataFactory() {
        return new ConsumerRecordFactory<>(new StringSerializer(), new MappedMetricDataJsonSerde.Ser());
    }
}
