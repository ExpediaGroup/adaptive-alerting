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

import lombok.Data;

import javax.persistence.*;

import com.expedia.adaptivealerting.modelservice.util.JpaConverterJson;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String uuid;

    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> hyperparams;

    @Column(name = "training_location")
    private String trainingLocation;

    @Column(name = "last_build_ts")
    private Timestamp buildTimestamp;

    public Model(String uuid, Map<String, Object> hyperparams, String trainingLocation) {
        this.uuid = uuid;
        this.hyperparams = hyperparams;
        this.trainingLocation = trainingLocation;
    }
}
