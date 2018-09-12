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
package com.expedia.adaptivealerting.modelservice.service.impl;

import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.modelservice.dto.ModelDto;
import com.expedia.adaptivealerting.modelservice.dto.Hyperparams;
import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.entity.Model;
import com.expedia.adaptivealerting.modelservice.repo.MetricRepository;
import com.expedia.adaptivealerting.modelservice.repo.ModelRepository;
import com.expedia.adaptivealerting.modelservice.repo.ModelRepositoryCustom;
import com.expedia.adaptivealerting.modelservice.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author kashah
 * @author shsethi
 */

@Service
public class ModelServiceImpl implements ModelService {

    @Autowired
    private ModelRepositoryCustom modelRepositoryCustom;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Override
    public List<ModelDto> getModels(String metricKey) {
        return modelRepositoryCustom.findModels(metricKey);
    }

    @Override
    public void addHyperparams(String uuid, Hyperparams hyperparams) {

        AssertUtil.notNull(hyperparams.getHyperparams(), "Hyperparams can't be null");
        AssertUtil.notNull(hyperparams.getMetricKey(), "Metric key can't be null");
        AssertUtil.notNull(uuid, "Model UUID can't be null");

        Integer metricId = metricRepository.findIdByMetricKey(hyperparams.getMetricKey());
        Integer modelId = modelRepository.findIdByModelUUID(uuid);

        Metric metric;
        if (metricId == null) {
            metric = new Metric(hyperparams.getMetricKey());
        } else {
            metric = metricRepository.getOne(metricId);
        }

        Model model;
        if (modelId == null) {
            model = new Model(uuid, hyperparams.getHyperparams());
        } else {
            model = modelRepository.getOne(modelId);
        }

        if (metricId == null || modelId == null) {
            model.getMetrics().add(metric);
            metric.getModels().add(model);
            modelRepository.save(model);
        } else {
            throw new RuntimeException("Metric-Model mapping already exits");
        }
    }

    @Override
    public void markToRebuild(String modelUUID, String metricKey, boolean toRebuild) {

        AssertUtil.notNull(modelUUID, "Model UUID can't be null");
        AssertUtil.notNull(metricKey, "Metric key can't be null");

        Integer modelID = modelRepositoryCustom.getModelID(metricKey, modelUUID);
        Model model = modelRepository.getOne(modelID);
        model.setToRebuild(toRebuild);
        modelRepository.save(model);

    }

    @Override
    public void updateThresholds(String modelUUID, String metricKey, Map<String, Object> thresholds) {

        AssertUtil.notNull(modelUUID, "Model UUID can't be null");
        AssertUtil.notNull(thresholds, "Thresholds can't be null");

        Integer modelID = modelRepositoryCustom.getModelID(metricKey, modelUUID);
        Model model = modelRepository.getOne(modelID);
        model.setThresholds(thresholds);
        modelRepository.save(model);
    }
}