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
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "metric")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "metric_key")
    private String metricKey;

    @ManyToMany(targetEntity = Model.class, cascade = { CascadeType.ALL })
    @JoinTable(name = "metric_model", joinColumns = { @JoinColumn(name = "metric_id") }, inverseJoinColumns = {
            @JoinColumn(name = "model_id") })
    private List<Model> models = new ArrayList<>();

    public Metric() {

    }

    public Metric(String key) {
        this.metricKey = key;
    }

}
