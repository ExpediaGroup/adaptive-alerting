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
import com.expedia.adaptivealerting.modelservice.dto.RebuildParams;
import com.expedia.adaptivealerting.modelservice.dto.ThresholdParams;
import com.expedia.adaptivealerting.modelservice.service.ModelService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import com.expedia.adaptivealerting.modelservice.dto.ModelParams;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertSame;

public class ModelControllerTests {

    /* Class under test */
    @InjectMocks
    private ModelController controller;

    @Mock
    private ModelService modelService;

    @Before
    public void setUp() {
        this.controller = new ModelController();
        MockitoAnnotations.initMocks(this);
        when(modelService.getModels(anyString())).thenReturn(new ArrayList<ModelDto>());
    }

    @Test
    public void testGetModel() {
        List<ModelDto> models = controller.getModel("testkey");
        assertNotNull(models);
    }

    @Test
    public void testAddModelParams() {
        ModelParams params = new ModelParams();
        HttpStatus status = controller.addModelParams(params);
        assertSame(HttpStatus.OK, status);
    }

    @Test
    public void testmarkToRebuild() {
        RebuildParams params = new RebuildParams();
        HttpStatus status = controller.markToRebuild(params);
        assertSame(HttpStatus.OK, status);
    }

    @Test
    public void testUpdateThresholds() {
        ThresholdParams params = new ThresholdParams();
        HttpStatus status = controller.updateThresholds(params);
        assertSame(HttpStatus.OK, status);
    }
}