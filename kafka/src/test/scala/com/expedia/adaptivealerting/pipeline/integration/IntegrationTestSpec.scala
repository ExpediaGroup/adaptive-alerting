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

package com.expedia.adaptivealerting.pipeline.integration

import java.util.Properties
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.expedia.adaptivealerting.kafka.util.AppUtil
import com.expedia.www.haystack.commons.entities.MetricPoint
import com.typesafe.config.{Config, ConfigValue, ConfigValueFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.streams.integration.utils.{EmbeddedKafkaCluster, IntegrationTestUtils}
import org.apache.kafka.streams.{KeyValue, StreamsConfig}
import org.scalatest._

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

object EmbeddedKafka {
  val CLUSTER = new EmbeddedKafkaCluster(1)
  CLUSTER.start()
}

class IntegrationTestSpec extends WordSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  protected var scheduler: ScheduledExecutorService = _
  protected val PUNCTUATE_INTERVAL_MS = 2000
  protected val INPUT_TOPIC = "metrics"
  protected val OUTPUT_TOPIC = "anomalies"
  val STREAM = "streams"

  override def beforeAll() {
    scheduler = Executors.newSingleThreadScheduledExecutor()
  }

  override def afterAll(): Unit = {
    scheduler.shutdownNow()
  }

  protected def produceSpansAsync(produceInterval: FiniteDuration,
                                  metrics: List[MetricPoint]): Unit = {
    var currentTime = System.currentTimeMillis()
    var idx = 0
    scheduler.scheduleWithFixedDelay(() => {
      if (idx < metrics.size) {
        currentTime = currentTime + ((idx * PUNCTUATE_INTERVAL_MS) / (metrics.size - 1))
        val metricPoint = metrics.apply(idx)
        val records = List(new KeyValue[String, MetricPoint](metricPoint.metric, metricPoint)).asJava
        IntegrationTestUtils.produceKeyValuesSynchronouslyWithTimestamp(
          INPUT_TOPIC,
          records,
          producerConfigProperties,
          currentTime)
      }
      idx = idx + 1
    }, 0, produceInterval.toMillis, TimeUnit.MILLISECONDS)
  }

  def appConfig(key: String) : Config = {
    AppUtil.getAppConfig(key).withValue(STREAM + "." + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      ConfigValueFactory.fromAnyRef(EmbeddedKafka.CLUSTER.bootstrapServers))
  }

  def consumerConfig(key: String, groupId: String) : Config = {
    AppUtil.getAppConfig(key)
      .withValue(STREAM + "." + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                          ConfigValueFactory.fromAnyRef(EmbeddedKafka.CLUSTER.bootstrapServers))
      .withValue(STREAM + "." + ConsumerConfig.GROUP_ID_CONFIG,
                          ConfigValueFactory.fromAnyRef(groupId + "-consumer"))
  }

  def streamConfigProperties(key: String, appId: String) : Properties = {
    val streamConfig  = AppUtil.getAppConfig(key).getConfig(STREAM)
      .withValue(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        ConfigValueFactory.fromAnyRef(EmbeddedKafka.CLUSTER.bootstrapServers))
      .withValue(StreamsConfig.APPLICATION_ID_CONFIG,
        ConfigValueFactory.fromAnyRef(appId))
    configToProps(streamConfig)
  }

  def producerConfigProperties() : Properties = {
    val prodConfig = AppUtil.getAppConfig("producer").getConfig(STREAM)
      .withValue(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        ConfigValueFactory.fromAnyRef(EmbeddedKafka.CLUSTER.bootstrapServers))
    configToProps(prodConfig)
  }

  def configToProps(config: Config) = {
    val props = new Properties
    config.entrySet.forEach((entry: java.util.Map.Entry[String, ConfigValue]) => {
      def setProperty(entry: java.util.Map.Entry[String, ConfigValue]) =
        props.setProperty(entry.getKey, entry.getValue.unwrapped.toString)
      setProperty(entry)
    })
    props
  }

}
