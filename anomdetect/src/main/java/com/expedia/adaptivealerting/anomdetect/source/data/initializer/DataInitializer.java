package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;

public interface DataInitializer {
    void initializeDetector(MappedMetricData metricData, Detector detector);

}
