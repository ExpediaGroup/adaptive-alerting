package com.expedia.adaptivealerting.anomdetect.source.data.initializer.reconstructbuffer;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;


@Data
@AllArgsConstructor
@Accessors(chain = true)
public final class ReconstructParameters {

    // all intervals are in minutes

    // scale factor related
    private int scaleFactorInterval;

    // data sample related

    // time series related
    private int timeSeriesInterval;
    private int timeSeriesSize;

    public void validate() {
        isTrue(scaleFactorInterval > 0, "Required: scaleFactorInterval >= 0.0");
        isTrue(timeSeriesInterval > 0, "Required: timeSeriesInterval >= 0.0");
        isTrue(timeSeriesSize > 0, "Required: timeSeriesSize >= 0.0");
    }
}
