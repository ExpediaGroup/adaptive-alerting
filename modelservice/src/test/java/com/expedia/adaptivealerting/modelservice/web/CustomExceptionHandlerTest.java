/*
 * Copyright 2018-2019 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.modelservice.exception.CustomExceptionHandler;
import com.expedia.adaptivealerting.modelservice.repo.LegacyDetectorRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CustomExceptionHandlerTest {

    private static final String DUMMY_USER = "Dummy User";

    @InjectMocks
    private LegacyDetectorController controllerUnderTest;

    private MockMvc mockMvc;

    @Mock
    private LegacyDetectorRepository detectorRepo;

    private UUID someUuid;

    @Before
    public void setUp() {
        this.controllerUnderTest = new LegacyDetectorController();
        mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest)
                .setControllerAdvice(new CustomExceptionHandler(new SimpleMeterRegistry()))
                .build();

        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testFindByUuid_NOT_FOUND_for_missing_uuid() throws Exception {
        when(detectorRepo.findByUuid(anyString())).thenReturn(null);
        mockMvc.perform(get("/api/v2/detectors/findByUuid").param("uuid", someUuid.toString())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Invalid UUID: " + someUuid.toString()));
    }

    // TODO: "/findByCreatedBy" should return 404 NOT_FOUND for invalid user (instead of 400 BAD_REQUEST)
    @Test
    public void testFindByCreatedBy_BAD_REQUEST_for_invalid_user() throws Exception {
        when(detectorRepo.findByCreatedBy(anyString())).thenReturn(null);
        mockMvc.perform(get("/api/v2/detectors/findByCreatedBy").param("user", DUMMY_USER)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid user: " + DUMMY_USER));
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();
    }

}
