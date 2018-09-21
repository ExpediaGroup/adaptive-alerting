/*
 * Copyright 2018 Expedia Group, Inc.
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

package com.expedia.adaptivealerting.pipeline.integration.test

import java.time.Instant

import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector
import com.expedia.adaptivealerting.anomdetect.{AnomalyDetectorManager, AnomalyDetectorMapper}
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult
import com.expedia.adaptivealerting.kafka.KafkaConfigProps._
import com.expedia.adaptivealerting.kafka.detector.KafkaAnomalyDetectorManager
import com.expedia.adaptivealerting.kafka.mapper.KafkaAnomalyDetectorMapper
import com.expedia.adaptivealerting.pipeline.integration.{EmbeddedKafka, IntegrationTestSpec}
import com.expedia.metrics.{MetricData, MetricDefinition}
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils
import org.scalatest.Ignore

import scala.collection.JavaConverters._
import scala.concurrent.duration._

// FIXME Fix this test
@Ignore
class ConstantThresholdBasedE2ETestSpec extends IntegrationTestSpec {
  protected val INTERMEDIATE_TOPIC = "mapped-metrics"
  protected val adMapperStreamConfig = streamConfigProperties(ANOMALY_DETECTOR_MAPPER, "ad-mapper")
  protected val adManagerStreamConfig = streamConfigProperties(ANOMALY_DETECTOR_MANAGER, "ad-manager")

  protected var mappedMetricsTopicConsumerConfig = consumerConfig("mapped-metrics-consumer", "mapped-metrics-detector")
  protected var anomalyTopicConsumerConfig = consumerConfig("anomalies-consumer", "anomaly")


  override def beforeEach() {
    EmbeddedKafka.CLUSTER.createTopic(INPUT_TOPIC)
    EmbeddedKafka.CLUSTER.createTopic(INTERMEDIATE_TOPIC)
    EmbeddedKafka.CLUSTER.createTopic(OUTPUT_TOPIC)
    IntegrationTestUtils.purgeLocalStreamsState(adMapperStreamConfig)
    IntegrationTestUtils.purgeLocalStreamsState(adManagerStreamConfig)
  }

  override def afterEach(): Unit = {
    EmbeddedKafka.CLUSTER.deleteTopic(INPUT_TOPIC)
    EmbeddedKafka.CLUSTER.deleteTopic(INTERMEDIATE_TOPIC)
    EmbeddedKafka.CLUSTER.deleteTopic(OUTPUT_TOPIC)
  }

  "AA with 'Constant Threshold Anomaly Detection' model" should {

    "consume anomalous metrics from input topic, toAnomalyResult and confirm them as anomalies and " +
      "send results to output topic" in {

      Given("a set of anomalous metrics and kafka specific configurations")
      val metrics = generateAnomalousMetrics()
      val adMapperSR = adMapperRunner
      val adManagerSR = adManagerRunner

      When("anomalous metrics are produced in 'input' topic, and kafka-streams topology is started")
      produceSpansAsync(10.millis, metrics)
      adMapperSR.start()
      adManagerSR.start()

      Then("'ad-mapper' should map the metrics to 'constant threshold outlier detector' ")

      val intermediateRecords: List[KeyValue[String, MetricData]] =
        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived[String, MetricData](
          configToProps(mappedMetricsTopicConsumerConfig.getConfig(STREAM)),
          INTERMEDIATE_TOPIC, 1, 15000).asScala.toList // get metricPoints from Kafka's output topic
      intermediateRecords.size shouldEqual 1
      intermediateRecords.foreach(record => {
        record.value.getMetricDefinition.getTags.getKv.get("what") match {
          case "duration" => "success"
          case "latency" => "success"
          case _ => fail("Unexpected metrics in input topic for 'constant threshold outlier detector'")
        }
      })


      Then("'constant threshold outlier detector' should read records from its topic and " +
        "write those anomalous records to output topic")
      val consumerPropAnomalyTopic = configToProps(anomalyTopicConsumerConfig.getConfig(STREAM))
      consumerPropAnomalyTopic.put("JsonPOJOClass", classOf[AnomalyResult])
      val outputRecords: List[KeyValue[String, AnomalyResult]] =
        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived[String, AnomalyResult](
          consumerPropAnomalyTopic,
          OUTPUT_TOPIC, 1, 35000).asScala.toList // get metricPoints from Kafka's output topic
      outputRecords.size shouldEqual 1

      Then("no other intermediate partitions are created after as a result of topology")
      val adminClient: AdminClient = AdminClient.create(adMapperStreamConfig)
      adminClient.listTopics().listings() should not be null
      val topicNames: Iterable[String] = adminClient.listTopics.listings().get().asScala
        .map(topicListing => topicListing.name)

      topicNames.size shouldEqual 3
      topicNames.toSet.contains(INPUT_TOPIC) shouldEqual true
      topicNames.toSet.contains(INTERMEDIATE_TOPIC) shouldEqual true
      topicNames.toSet.contains(OUTPUT_TOPIC) shouldEqual true
    }
  }

  private def adMapperRunner = {
    val httpClient: HttpClient = HttpClients.createDefault()
    val modelServiceConnector: ModelServiceConnector = new ModelServiceConnector(httpClient)
    new KafkaAnomalyDetectorMapper(appConfig(ANOMALY_DETECTOR_MAPPER), new AnomalyDetectorMapper(modelServiceConnector))
  }

  private def adManagerRunner = {
    val manager: AnomalyDetectorManager = new AnomalyDetectorManager(appConfig(ANOMALY_DETECTOR_MANAGER).getConfig(FACTORIES))
    new KafkaAnomalyDetectorManager(appConfig(ANOMALY_DETECTOR_MANAGER), manager)
  }

  private def generateAnomalousMetrics(): List[MetricData] = {
    val latencyMetricDef = new MetricDefinition("latency")
    val latencyMetricData = new MetricData(latencyMetricDef, 2.0, Instant.now().getEpochSecond)

    val failureCountMetricDef = new MetricDefinition("failureCount")
    val failureCountMetricData = new MetricData(failureCountMetricDef, 3.0, Instant.now().getEpochSecond)

    List(
      latencyMetricData,
      failureCountMetricData
    )
  }
}
