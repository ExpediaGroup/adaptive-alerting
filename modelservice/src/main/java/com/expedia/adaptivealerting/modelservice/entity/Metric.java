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

import javax.persistence.*;
import java.util.List;

@Entity
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "metric_key")
    private String metricKey;

    @ManyToMany(targetEntity = Model.class, cascade = { CascadeType.ALL })
    @JoinTable(name = "metric_model", joinColumns = { @JoinColumn(name = "metric_id") }, inverseJoinColumns = {
            @JoinColumn(name = "model_id") })
    private List<Model> models;

    public Metric() {

    }

    public Metric(String key) {
        this.metricKey = key;
    }

    public Metric(String key, List<Model> models) {
        this.metricKey = key;
        this.models = models;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the metricUUID
     */
    public String getMetricKey() {
        return metricKey;
    }

    /**
     * @param metricKey
     *            the metricUUID to set
     */
    public void setMetricKey(String metricKey) {
        this.metricKey = metricKey;
    }

    /**
     * @return the models
     */
    public List<Model> getModels() {
        return models;
    }

    /**
     * @param models
     *            the models to set
     */
    public void setModels(List<Model> models) {
        this.models = models;
    }
}
