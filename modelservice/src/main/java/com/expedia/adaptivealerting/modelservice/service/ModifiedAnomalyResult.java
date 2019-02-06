package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedAnomalyResult {

    private long epochSeconds;
    private AnomalyLevel level;
    private Double originalValue;
    private Double predictedValue;
}
