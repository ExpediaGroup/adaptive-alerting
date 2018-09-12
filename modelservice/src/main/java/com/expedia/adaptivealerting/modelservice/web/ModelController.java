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
package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.dto.ModelDto;
import com.expedia.adaptivealerting.modelservice.dto.Hyperparams;
import com.expedia.adaptivealerting.modelservice.dto.RebuildParams;
import com.expedia.adaptivealerting.modelservice.dto.ThresholdParams;
import com.expedia.adaptivealerting.modelservice.service.ModelService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author kashah
 * @author shsethi
 */
@RestController
@RequestMapping("/api")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @ApiOperation(value = "Get model info by providing metric key")
    @GetMapping(value = "/model/{metricKey}")
    public List<ModelDto> getModel(@PathVariable String metricKey) {
        return modelService.getModels(metricKey);
    }

    @ApiOperation(value = "Add hyperparams")
    @PutMapping(value = "/models/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addHyperparams(@PathVariable String uuid, @RequestBody Hyperparams hyperparams) {
        modelService.addModelParams(uuid, hyperparams);
        return "Model params saved successfully";
    }

    @ApiOperation(value = "Mark model to rebuild")
    @PutMapping(value = "/rebuildModel/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String markToRebuild(@PathVariable String uuid, @RequestBody RebuildParams rebuildParams) {
        modelService.markToRebuild(uuid, rebuildParams.getMetricKey(),
                rebuildParams.isToRebuild());
        return "Model marked for rebuild";
    }

    @Deprecated
    @ApiOperation(value = "Update threshold for a given model")
    @PutMapping(value = "/updateThreshold/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateThresholds(@PathVariable String uuid, @RequestBody ThresholdParams thresholdParams) {
        modelService.updateThresholds(uuid, thresholdParams.getMetricKey(),
                thresholdParams.getThresholds());
        return "Updated threshold successfully";
    }

}