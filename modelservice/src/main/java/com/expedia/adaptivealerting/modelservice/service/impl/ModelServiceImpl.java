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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedia.adaptivealerting.modelservice.dto.ModelDto;
import com.expedia.adaptivealerting.modelservice.repo.ModelRepositoryCustom;
import com.expedia.adaptivealerting.modelservice.service.ModelService;

/**
 * @author kashah
 *
 */

@Service
public class ModelServiceImpl implements ModelService{
    
    @Autowired
    ModelRepositoryCustom modelRepositoryCustom;
    
    @Override
    public List<ModelDto> getModels(String metricKey){
        return modelRepositoryCustom.findModels(metricKey);   
    }

}
