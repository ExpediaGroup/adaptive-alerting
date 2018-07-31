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

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "model_uuid")
    private String modelUUID;

    @Column(name = "hyperparams")
    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> hyperParams;

    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> thresholds;

    @Column(name = "to_rebuild")
    private boolean toRebuild;

    @Column(name = "last_build_ts")
    private Date buildTimestamp;

    @ManyToMany(mappedBy = "models", cascade = CascadeType.ALL)
    private List<Metric> metrics;

    public Model() {

    }

    public Model(String uuid, Map<String, Object> hyperParams, Map<String, Object> thresholds, boolean toRebuild,
            Date buildTimestamp) {
        this.modelUUID = uuid;
        this.hyperParams = hyperParams;
        this.thresholds = thresholds;
        this.toRebuild = toRebuild;
        this.buildTimestamp = buildTimestamp;
    }

    public Model(String uuid, Map<String, Object> hyperParams, Map<String, Object> thresholds, boolean toRebuild,
            Date buildTimestamp, List<Metric> metrics) {
        this.modelUUID = uuid;
        this.hyperParams = hyperParams;
        this.thresholds = thresholds;
        this.toRebuild = toRebuild;
        this.buildTimestamp = buildTimestamp;
        this.metrics = metrics;
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
     * @return the modelUUID
     */
    public String getModelUUID() {
        return modelUUID;
    }

    /**
     * @param modelUUID
     *            the modelUUID to set
     */
    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    /**
     * @return the hyperParams
     */
    public Map<String, Object> getHyperParams() {
        return hyperParams;
    }

    /**
     * @param hyperParams
     *            the hyperParams to set
     */
    public void setHyperParams(Map<String, Object> hyperParams) {
        this.hyperParams = hyperParams;
    }

    /**
     * @return the thresholds
     */
    public Map<String, Object> getThresholds() {
        return thresholds;
    }

    /**
     * @param thresholds
     *            the thresholds to set
     */
    public void setThresholds(Map<String, Object> thresholds) {
        this.thresholds = thresholds;
    }

    /**
     * @return the toRebuild
     */
    public boolean isToRebuild() {
        return toRebuild;
    }

    /**
     * @param toRebuild
     *            the toRebuild to set
     */
    public void setToRebuild(boolean toRebuild) {
        this.toRebuild = toRebuild;
    }

    /**
     * @return the buildTimestamp
     */
    public Date getBuildTimestamp() {
        return buildTimestamp;
    }

    /**
     * @param buildTimestamp
     *            the buildTimestamp to set
     */
    public void setBuildTimestamp(Date buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    /**
     * @return the metrics
     */
    public List<Metric> getMetrics() {
        return metrics;
    }

    /**
     * @param metrics
     *            the metrics to set
     */
    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
}
