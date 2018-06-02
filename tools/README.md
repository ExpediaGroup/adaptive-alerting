# Adaptive Alerting - Tools

This module provides tools support for common model development needs.

## Data pipelines

We have a data pipeline mini-framework. This allows you, as a model developer, to create runtime data pipelines that
include data sources, filters and sinks without having to run everything through a messaging infrastructure.

These pipelines are intended purely for development purposes as they lack the production-ready features of messaging
systems like Kafka or Kinesis.

Here's a sample pipeline:

```
public class WhiteNoisePipeline {
    
    public static void main(String[] args) {
        final MetricSource source = new WhiteNoiseMetricSource();
        
        final MetricFilter filter = new OutlierDetectorMetricFilter(new PewmaOutlierDetector());
        source.addSubscriber(filter);
        
        final MetricSink consoleSink = new ConsoleLogMetricSink();
        filter.addSubscriber(consoleSink);
    
        final TimeSeries timeSeries = new TimeSeries("white-noise");
        final ApplicationFrame chartFrame = createChartFrame("White Noise", timeSeries);
        final MetricSink chartSink = new ChartSink(timeSeries);
        filter.addSubscriber(chartSink);
        
        showChartFrame(chartFrame);
        source.start();
    }
}
```

Here's the associated visualization:

(Note: We'll add threshold bands and running RMSE shortly.)

See the `com.expedia.adaptivealerting.samples` package for other examples of how to create data pipelines.
