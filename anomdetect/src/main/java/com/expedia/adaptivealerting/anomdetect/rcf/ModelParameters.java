package com.expedia.adaptivealerting.anomdetect.rcf;

import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Tatjana Kamenov
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ModelParameters {
    String awsRegion;
    String endpoint;
    Integer shingleSize;
    Float strongScoreCutoff;
    Float weakScoreCutoff;
}
