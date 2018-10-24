package com.expedia.adaptivealerting.modelservice.entity.projection;

import com.expedia.adaptivealerting.modelservice.entity.Model;
import com.expedia.adaptivealerting.modelservice.entity.ModelType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author shsethi
 */
@Projection(name="modelWithDetectorType", types = Model.class)
public interface ModelProjection {

    Long getId();

    @Value("#{target.detector.getType()}")
    ModelType getDetectorType();

    @Value("#{target.detector.getUuid()}")
    String getUuid();

    Map<String, Object> getParams();

    Double getWeakSigmas();

    Double getStrongSigmas();

    Timestamp getBuildTimestamp();
}
