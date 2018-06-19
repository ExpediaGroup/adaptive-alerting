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

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult
import com.expedia.adaptivealerting.core.util.MetricPointUtil.metricPoint
import com.expedia.adaptivealerting.kafka.detector.KafkaConstantThresholdOutlierDetector
import com.expedia.adaptivealerting.kafka.router.MetricRouter
import com.expedia.adaptivealerting.pipeline.integration.{EmbeddedKafka, IntegrationTestSpec}
import com.expedia.www.haystack.commons.entities.MetricPoint
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class ConstantThresholdBasedE2ETestSpec extends IntegrationTestSpec {
  protected val INTERMEDIATE_TOPIC = "constant-metrics"
  protected val metricRouterStreamConfig = streamConfigProperties("metric-router", "metric-router")
  protected val constantThresholdStreamConfig = streamConfigProperties("constant-detector", "constant-threshold-detector")
  protected var constantThresholdTopicConsumerConfig = consumerConfig("constant-detector-consumer", "constant-threshold-detector")
  protected var anomalyTopicConsumerConfig = consumerConfig("anomalies-consumer","anomaly")


  override def beforeEach() {
    EmbeddedKafka.CLUSTER.createTopic(INPUT_TOPIC)
    EmbeddedKafka.CLUSTER.createTopic(INTERMEDIATE_TOPIC)
    EmbeddedKafka.CLUSTER.createTopic(OUTPUT_TOPIC)
    IntegrationTestUtils.purgeLocalStreamsState(metricRouterStreamConfig)
    IntegrationTestUtils.purgeLocalStreamsState(constantThresholdStreamConfig)
  }

  override def afterEach(): Unit = {
    EmbeddedKafka.CLUSTER.deleteTopic(INPUT_TOPIC)
    EmbeddedKafka.CLUSTER.deleteTopic(INTERMEDIATE_TOPIC)
    EmbeddedKafka.CLUSTER.deleteTopic(OUTPUT_TOPIC)
  }

  "AA with 'Constant Threshold Anomaly Detection' model" should {

    "consume anomalous metrics from input topic, classify and confirm them as anomalies and " +
      "send results to output topic" in {

      Given("a set of anomalous metrics and kafka specific configurations")
      val metrics = generateAnomalousMetrics()
      val metricRouterSR = metricRouterStreamRunner
      val constantThresholdSR = constantThresholdDetectorStreamRunner

      When("anomalous metrics are produced in 'input' topic, and kafka-streams topology is started")
      produceSpansAsync(10.millis, metrics)
      metricRouterSR.start()
      constantThresholdSR.start()

      Then("'metric router' should route the metrics to 'constant threshold outlier detector' model's input topic")

      val intermediateRecords: List[KeyValue[String, MetricPoint]] =
        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived[String, MetricPoint](
          configToProps(constantThresholdTopicConsumerConfig.getConfig(STREAM)),
          INTERMEDIATE_TOPIC, 2, 15000).asScala.toList // get metricPoints from Kafka's output topic
      intermediateRecords.size shouldEqual 2
      intermediateRecords.foreach(record => {
        record.value.metric match {
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
          OUTPUT_TOPIC, 2, 35000).asScala.toList // get metricPoints from Kafka's output topic
      outputRecords.size shouldEqual 2

      Then("no other intermediate partitions are created after as a result of topology")
      val adminClient: AdminClient = AdminClient.create(metricRouterStreamConfig)
      adminClient.listTopics().listings() should not be null
      val topicNames: Iterable[String] = adminClient.listTopics.listings().get().asScala
        .map(topicListing => topicListing.name)

      topicNames.size shouldEqual 3
      topicNames.toSet.contains(INPUT_TOPIC) shouldEqual true
      topicNames.toSet.contains(INTERMEDIATE_TOPIC) shouldEqual true
      topicNames.toSet.contains(OUTPUT_TOPIC) shouldEqual true
    }
  }

  private def metricRouterStreamRunner = {
    new MetricRouter.StreamRunnerBuilder().build(appConfig("metric-router"))
  }

  private def constantThresholdDetectorStreamRunner = {
    new KafkaConstantThresholdOutlierDetector.StreamRunnerBuilder().build(appConfig("constant-detector"))
  }

  private def generateAnomalousMetrics() : List[MetricPoint] = {
    List(
      metricPoint("latency", Instant.now(), 2),
      metricPoint("duration", Instant.now(), 3)
    )
  }
}
