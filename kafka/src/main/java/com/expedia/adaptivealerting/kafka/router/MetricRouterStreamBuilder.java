package com.expedia.adaptivealerting.kafka.router;

import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.StreamRunnerBuilder;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Arrays;
import java.util.Collections;

public class MetricRouterStreamBuilder implements StreamRunnerBuilder {
  @Override
  public StreamsRunner build(Config appConfig) {
    final StreamsBuilder builder = createStreamsBuilder(appConfig);

    return AppUtil.createStreamsRunner(appConfig, builder);
  }

  private static StreamsBuilder createStreamsBuilder(Config appConfig) {
    final StreamsBuilder builder = new StreamsBuilder();
    final KStream<String, MetricPoint> metrics = builder.stream(appConfig.getString("topic"));

    metrics.filter(MetricRouterStreamBuilder::isConstant).to("constant-metrics");
    metrics.filter(MetricRouterStreamBuilder::isEwma).to("ewma-metrics");
    metrics.filter(MetricRouterStreamBuilder::isPewma).to("pewma-metrics");
    return builder;
  }

  // TODO: add real routing conditions
  private static boolean isConstant(String key, MetricPoint metricPoint) {
    return Arrays.asList("latency", "duration").contains(metricPoint.metric());
  }

  private static boolean isEwma(String key, MetricPoint metricPoint) {
    return Collections.singletonList("ewma").contains(metricPoint.metric());
  }

  private static boolean isPewma(String key, MetricPoint metricPoint) {
    return Collections.singletonList("pewma").contains(metricPoint.metric());
  }
}
