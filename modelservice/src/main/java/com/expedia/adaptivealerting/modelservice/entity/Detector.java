/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @Column(name = "created_by")
    private String createdBy;

}
