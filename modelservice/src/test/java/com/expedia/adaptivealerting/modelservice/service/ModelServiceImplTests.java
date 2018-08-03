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
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.dto.ModelDto;
import com.expedia.adaptivealerting.modelservice.entity.Model;
import com.expedia.adaptivealerting.modelservice.repo.ModelRepository;
import com.expedia.adaptivealerting.modelservice.repo.ModelRepositoryCustom;
import com.expedia.adaptivealerting.modelservice.service.impl.ModelServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import com.expedia.adaptivealerting.modelservice.dto.ModelParams;

/**
 * @author kashah
 *
 */
public class ModelServiceImplTests {

    /* Class under test */
    @InjectMocks
    private ModelServiceImpl service;

    @Mock
    private ModelRepositoryCustom modelRepositoryCustom;

    @Mock
    private ModelRepository modelRepository;

    private List<ModelDto> modelDtoList;

    @Before
    public void setUp() throws Exception {
        this.service = new ModelServiceImpl();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testGetModel() {
        List<ModelDto> actualmodels = service.getModels("testKey");
        assertEquals(actualmodels.size(), modelDtoList.size());
    }

    @Test
    public void testAddModelParams() {
        service.addModelParams(new ModelParams());
    }

    @Test
    public void testMarkToRebuild() {
        service.markToRebuild("uuid1", "key1", true);
    }

    @Test
    public void testUpdateThresholds() {
        service.updateThresholds("uuid1", "key1", new HashMap<>());
    }

    private void initTestObjects() {

        this.modelDtoList = new ArrayList<ModelDto>();
        ModelDto model1 = new ModelDto();
        model1.setBuildTimestamp(Instant.parse("2017-03-01T00:00:00Z"));
        model1.setHyperparams(new HashMap<>());
        model1.setModelUUID("uuid1");
        model1.setThresholds(new HashMap<>());

        ModelDto model2 = new ModelDto();
        model2.setBuildTimestamp(Instant.parse("2017-03-03T00:00:00Z"));
        model2.setHyperparams(new HashMap<>());
        model2.setModelUUID("uuid2");
        model2.setThresholds(new HashMap<>());

        modelDtoList.add(model1);
        modelDtoList.add(model2);

    }

    private void initDependencies() {
        when(modelRepositoryCustom.findModels(anyString())).thenReturn(modelDtoList);
        when(modelRepository.getModelById(anyInt())).thenReturn(new Model());
    }
}
