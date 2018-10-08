package com.expedia.adaptivealerting.modelservice.entity;

import com.expedia.adaptivealerting.modelservice.util.JpaConverterJson;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * Detector entity.
 *
 * @author shsethi
 */
@Data
@Entity
public class Detector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @ManyToOne
    @JoinColumn(name = "model_type_id")
    private ModelType type;

    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> hyperparams;

    @Column(name = "training_meta")
    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> trainingMetaData;

}
