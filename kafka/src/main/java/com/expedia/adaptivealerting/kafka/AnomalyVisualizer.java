package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.adaptivealerting.kafka.visualizer.AnomalyConsumer;
import com.expedia.adaptivealerting.kafka.visualizer.AnomalyModel;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

@SpringBootApplication
@Slf4j
public class AnomalyVisualizer {

    @Generated
    public static void main(String[] args) {
        Config config = new TypesafeConfigLoader("visualizer").loadMergedConfig();
        Config metricConsumerConfig = config.getConfig("metric-consumer");
        Properties metricConsumerProps = ConfigUtil.toConsumerConfig(metricConsumerConfig);
        KafkaConsumer metricConsumer = new KafkaConsumer<String, MappedMetricData>(metricConsumerProps);
        //run(metricConsumer, "anomalies");
        try {
            AnomalyConsumer anomalyConsumer = new AnomalyConsumer();
            anomalyConsumer.listen();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void run(KafkaConsumer metricConsumer, String metricTopic) {
        metricConsumer.subscribe(Collections.singletonList(metricTopic));
        boolean continueProcessing = true;

        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (continueProcessing) {
            try {
                processMetrics(metricConsumer, metricTopic);
            } catch (WakeupException e) {
                metricConsumer.close();
                continueProcessing = false;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    private static void processMetrics(KafkaConsumer metricConsumer, String metricTopic) throws IOException {
        log.info("running");
        ConsumerRecords<String, MappedMetricData> metricRecords = metricConsumer.poll(1000L);
        int numConsumed = metricRecords.count();
        log.trace("Read {} metric records from topic={}", numConsumed, metricTopic);

        AnomalyModel anomalyModel = null;
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            log.info("consumer record: " + consumerRecord.value().getMetricData() + " " + consumerRecord.value().getDetectorUuid()
                    + " " + consumerRecord.value().getAnomalyResult().getAnomalyLevel());
            MappedMetricData mappedMetricData = consumerRecord.value();
            MetricData metricData = mappedMetricData.getMetricData();
            anomalyModel = new AnomalyModel();
            if (metricData != null ) {
                anomalyModel.setKey(metricData.getMetricDefinition().getKey());
                anomalyModel.setTimestamp(new Date(metricData.getTimestamp()*1000L));
                anomalyModel.setTags(metricData.getMetricDefinition().getTags());
            }
            OutlierDetectorResult outlierDetectorResult = (OutlierDetectorResult) mappedMetricData.getAnomalyResult();
            anomalyModel.setLevel(outlierDetectorResult.getAnomalyLevel().toString());
            anomalyModel.setAnomalyThresholds(outlierDetectorResult.getThresholds());
            anomalyModel.setUuid(mappedMetricData.getDetectorUuid().toString());
            String json = convertToJson(anomalyModel);
            log.info(String.valueOf(json.length()));
            sendMetricsToESSearch(json);
        }
    }


    public static Response sendMetricsToESSearch(String json) {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "http"));
        RestClient restClient = builder.build();
        Response response = null;
        Request request = new Request(
                "POST",
                "/anomalies/doc");

        request.setEntity(new NStringEntity(json, ContentType.APPLICATION_JSON));
        try {
            response = restClient.performRequest(request);
        }
        catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        log.info("response " + response.getStatusLine());
        return response;
    }


    public static String convertToJson(Object object) {
        String json = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }



    public static RestHighLevelClient restClientBuilder() {
        RestClientBuilder builder = RestClient
                .builder(HttpHost.create("http://localhost:9200/anomalies/"))
                .setRequestConfigCallback(req -> {
                    req.setConnectionRequestTimeout(1000);
                    req.setConnectTimeout(1000);
                    req.setSocketTimeout(1000);
                    return req;
                }).setMaxRetryTimeoutMillis(1000)
                .setHttpClientConfigCallback(req -> {
                    req.setMaxConnTotal(10);
                    req.setMaxConnPerRoute(500);
                    return req;
                });
        return new RestHighLevelClient(builder);
    }

    public static Date convertToDate(long timestamp) {
        Date date = new Date(timestamp * 1000L);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateString =   format.format(date);
        return date;
    }

 }



