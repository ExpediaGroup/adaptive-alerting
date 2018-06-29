package com.expedia.adaptivealerting.kafka.serde;

import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timestamp extractor for MetricPoints
 * similar to class {@link org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp}
 * adding this as com.expedia.www.haystack.commons.kstreams.MetricPointTimestampExtractor doesn't handle this

 * @author shsethi
 */
public class HaystackMetricTimeStampExtractor implements TimestampExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HaystackMetricTimeStampExtractor.class);

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {

        MetricPoint metricPoint = (MetricPoint) (record.value());

        if (metricPoint != null) {
            //The startTime for metricpoints in computed in seconds and hence multiplying by 1000 to create the epochTimeInMs
            return metricPoint.epochTimeInSeconds() * 1000;
        } else {
            // Writes a log WARN message when the record is invalid, but returns -1 as timestamp,
            // which ultimately causes the record to be skipped and not to be processed
            LOGGER.warn("Input record {} will be dropped because it failed at de-serialisation.", record);
            return -1;
        }

    }
}

