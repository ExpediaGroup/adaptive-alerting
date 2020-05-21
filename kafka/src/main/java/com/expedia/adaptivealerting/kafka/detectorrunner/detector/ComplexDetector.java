package com.expedia.adaptivealerting.kafka.detectorrunner.detector;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.collect.EvictingQueue;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ComplexDetector implements Detector {

    private EvictingQueue<MetricData> queue = new EvictingQueue<>(1000);

    private double min = Double.MAX_VALUE;

    private double max = 0;

    @Override
    public DetectorResult detect(MetricData metricData) {
        OutlierDetectorResult outlierDetectorResult = new OutlierDetectorResult();
        if (metricData != null) {
            log.info(metricData.toString());
            queue.add(metricData);
            AnomalyThresholds anomalyThresholds = new AnomalyThresholds(100d, 75d, 50d, 25d);
            if (metricData.getValue() < min) {
                min = metricData.getValue();
                outlierDetectorResult.setAnomalyLevel(AnomalyLevel.WEAK);
            } else if (metricData.getValue() > max) {
                max = metricData.getValue();
                outlierDetectorResult.setAnomalyLevel(AnomalyLevel.STRONG);
            } else {
                outlierDetectorResult.setAnomalyLevel(AnomalyLevel.NORMAL);
            }
            if (metricData.getMetricDefinition() != null) {
                log.info(metricData.getMetricDefinition().toString());
            }
            outlierDetectorResult.setThresholds(anomalyThresholds);
        }

        return outlierDetectorResult;
    }

    @Override
    public String getName() {
        return "complex";
    }

    @Override
    public UUID getUuid() {
        return null;
    }

    @Override
    public boolean isTrusted() {
        return false;
    }

}
