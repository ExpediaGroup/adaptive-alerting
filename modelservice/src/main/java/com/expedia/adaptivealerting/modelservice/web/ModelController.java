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
import com.expedia.adaptivealerting.modelservice.dto.ModelParams;
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

    @ApiOperation(value = "Add model hyper params")
    @PostMapping(value = "/addModelParams", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addModelParams(@RequestBody ModelParams modelParams) {
        modelService.addModelParams(modelParams);
        return "Model params saved successfully";
    }

    @ApiOperation(value = "Mark model to rebuild")
    @PutMapping(value = "/markToRebuild", produces = MediaType.APPLICATION_JSON_VALUE)
    public String markToRebuild(@RequestBody RebuildParams rebuildParams) {
        modelService.markToRebuild(rebuildParams.getModelUUID(), rebuildParams.getMetricKey(),
                rebuildParams.getToRebuild());
        return "Model marked for rebuild";
    }

    @ApiOperation(value = "Update threshold for a given model")
    @PutMapping(value = "/updateThreshold", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateThresholds(@RequestBody ThresholdParams thresholdParams) {
        modelService.updateThresholds(thresholdParams.getModelUUID(), thresholdParams.getMetricKey(),
                thresholdParams.getThresholds());
        return "Updated threshold successfully";
    }

}