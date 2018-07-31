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
import com.expedia.adaptivealerting.modelservice.entity.ModelParams;
import com.expedia.adaptivealerting.modelservice.entity.RebuildParams;
import com.expedia.adaptivealerting.modelservice.entity.ThresholdParams;
import com.expedia.adaptivealerting.modelservice.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author kashah
 */
@RestController
public class ModelController {

    @Autowired
    private
    ModelService modelService;

    @RequestMapping(value = "/api/model", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ModelDto> getModel(@RequestParam("metricKey") String metricKey) {
        return modelService.getModels(metricKey);
    }

    @RequestMapping(value = "/api/addModelParams", method = RequestMethod.POST)
    public HttpStatus addModelParams(@RequestBody ModelParams modelParams) {
        modelService.addModelParams(modelParams);
        return HttpStatus.OK;
    }

    @RequestMapping(value = "/api/markToRebuild", method = RequestMethod.PUT)
    public HttpStatus markToRebuild(@RequestBody RebuildParams rebuildParams) {
        modelService.markToRebuild(rebuildParams.getModelUUID(), rebuildParams.getMetricKey(), rebuildParams.getToRebuild());
        return HttpStatus.OK;
    }

    @RequestMapping(value = "/api/updateThreshold", method = RequestMethod.PUT)
    public HttpStatus updateThresholds(@RequestBody ThresholdParams thresholdParams) {
        modelService.updateThresholds(thresholdParams.getModelUUID(), thresholdParams.getMetricKey(), thresholdParams.getThresholds());
        return HttpStatus.OK;
    }


}
