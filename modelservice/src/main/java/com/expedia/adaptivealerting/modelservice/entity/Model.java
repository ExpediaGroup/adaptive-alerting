/*
 * Copyright 2018 Expedia Group, Inc.
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

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.Map;

@Data
@Entity
public class Model {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String uuid;
    
    @ManyToOne
    @JoinColumn(name = "type_id")
    private ModelType type;
    
    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> hyperparams;


    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> otherStuff;
    
    private String trainingLocation;
    
    /**
     * DB-driven weak sigma override for models that have this parameter. Allows us to make sensitivity adjustments in
     * response to user feedback when ground truth classifications aren't available.
     */
    private double weakSigmas;
    
    /**
     * DB-driven strong sigma override for models that have this parameter. Allows us to make sensitivity adjustments in
     * response to user feedback when ground truth classifications aren't available.
     */
    private double strongSigmas;
    
    @Column(name = "last_build_ts")
    private Timestamp buildTimestamp;
}
