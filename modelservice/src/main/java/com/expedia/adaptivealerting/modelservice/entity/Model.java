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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "model")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "model_uuid")
    private String modelUUID;

    @Column(name = "hyperparams")
    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> hyperparms;

    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> thresholds;

    @Column(name = "to_rebuild")
    private boolean toRebuild;

    @Column(name = "last_build_ts")
    private Date buildTimestamp;

    @Column(name = "training_location")
    private Date trainingLocation;

    @ManyToMany(mappedBy = "models", cascade = CascadeType.ALL)
    private List<Metric> metrics = new ArrayList<>();

    public Model() {

    }

    public Model(String uuid, Map<String, Object> hyperparms, Map<String, Object> thresholds, boolean toRebuild,
            Date buildTimestamp) {
        this.modelUUID = uuid;
        this.hyperparms = hyperparms;
        this.thresholds = thresholds;
        this.toRebuild = toRebuild;
        this.buildTimestamp = buildTimestamp;
    }

    public Model(String uuid, Map<String, Object> hyperparms) {
        this.modelUUID = uuid;
        this.hyperparms = hyperparms;
    }
}
