package com.expedia.adaptivealerting.kafka.detector;

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.detector.ConstantThresholdOutlierDetector;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;

import static com.expedia.adaptivealerting.core.detector.ConstantThresholdOutlierDetector.RIGHT_TAILED;

public class KafkaConstantThresholdOutlierDetector {

    public static void main(String[] args) {

        // FIXME Create a map of these (see KafkaEwmaOutlierDetector for more details).
        final OutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, 0.95, 0.99);

        DetectorUtil.startStreams(detector, "constant-outlier-detector", "constant-metrics");
    }
}
