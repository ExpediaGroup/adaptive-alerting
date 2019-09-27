package com.expedia.adaptivealerting.kafka.processor;

import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.mapper.MapperResult;
import com.expedia.adaptivealerting.metricprofiler.MetricProfiler;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;

@RequiredArgsConstructor
@Data
public class MetricProfilerTransformerSupplier implements TransformerSupplier<String, MetricData, KeyValue<String, MetricData>> {

    @NonNull
    private MetricProfiler metricProfiler;

    @NonNull
    private final String stateStoreName;

    @Override
    public Transformer<String, MetricData, KeyValue<String, MetricData>> get() {
        return new MetricProfilerTransformer(metricProfiler, stateStoreName);
    }
}
