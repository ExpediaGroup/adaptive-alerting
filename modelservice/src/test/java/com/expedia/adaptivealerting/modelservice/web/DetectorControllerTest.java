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

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.service.DetectorService;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class DetectorControllerTest {

    @Spy
    @InjectMocks
    private DetectorController controllerUnderTest;

    private MockMvc mockMvc;

    @Mock
    private DetectorService detectorService;

    @Mock
    private Detector detector;

    @Mock
    private List<Detector> detectors;

    private UUID someUuid;
    private Detector legalParamsDetector;

    @Before
    public void setUp() {
        this.controllerUnderTest = new DetectorController();
        mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).setHandlerExceptionResolvers(new ExceptionHandlerExceptionResolver()).build();

        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testCreateDetector() {
        val uuidStr = controllerUnderTest.createDetector(legalParamsDetector);
        val uuid = UUID.fromString(uuidStr);
        assertNotNull(uuid);
    }

    @Test
    public void testFindByUuid() {
        val actualDetector = controllerUnderTest.findByUuid(someUuid.toString());
        assertNotNull(actualDetector);
    }

    @Test(expected = RecordNotFoundException.class)
    public void testFindByUuid_record_not_found_null_response() {
        when(detectorService.findByUuid(anyString())).thenReturn(null);
        controllerUnderTest.findByUuid(someUuid.toString());
    }

    @Test
    public void testFindByCreatedBy() {
        when(detectorService.findByCreatedBy(anyString())).thenReturn(detectors);
        val actualDetectors = controllerUnderTest.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
    }

    @Test(expected = RecordNotFoundException.class)
    public void test_FindByCreatedBy_illegal_args() {
        when(detectorService.findByCreatedBy(anyString())).thenReturn(null);
        controllerUnderTest.findByCreatedBy("kashah");
    }

    @Test(expected = RecordNotFoundException.class)
    public void testFindByCreatedBy_illegal_args1() {
        when(detectorService.findByCreatedBy(anyString())).thenReturn(new ArrayList<>());
        controllerUnderTest.findByCreatedBy("kashah");
    }

    @Test
    public void testUrlsWithoutFullPath_fail() throws Exception {
        mockMvc.perform(get("/findByUuid").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/findByCreatedBy").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError());
    }

    @Test
    public void testToggleDetector() {
        controllerUnderTest.toggleDetector(someUuid.toString(), true);
        verify(detectorService, times(1)).toggleDetector(someUuid.toString(), true);
    }

    @Test
    public void testTrustDetector() {
        controllerUnderTest.trustDetector(someUuid.toString(), true);
        verify(detectorService, times(1)).trustDetector(someUuid.toString(), true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        val actualDetectors = controllerUnderTest.getLastUpdatedDetectors(5);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test
    public void testGetLastUsedDetectors() {
        val actualDetectors = controllerUnderTest.getLastUsedDetectors(5);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test
    public void testUpdateDetector() {
        controllerUnderTest.updateDetector(someUuid.toString(), legalParamsDetector);
        verify(detectorService, times(1)).updateDetector(someUuid.toString(), legalParamsDetector);
    }

    @Test
    public void testUpdatedDetectorLastUsed() {
        Map<String, String> requestBody = Collections.singletonMap("uuid", someUuid.toString());
        controllerUnderTest.updatedDetectorLastUsed(requestBody);
        verify(detectorService, times(1)).updateDetectorLastUsed(someUuid.toString());
    }

    @Test
    public void testDeleteDetector() {
        val someUuidStr = someUuid.toString();
        controllerUnderTest.deleteDetector(someUuidStr);
        verify(detectorService, times(1)).deleteDetector(someUuidStr);
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();
        val mom = ObjectMother.instance();
        this.legalParamsDetector = mom.buildDetector();
        legalParamsDetector.setUuid(someUuid);
    }

    private void initDependencies() {
        when(detectorService.createDetector(any(Detector.class))).thenReturn(someUuid);
        when(detectorService.findByUuid(someUuid.toString())).thenReturn(detector);
        when(detectorService.getLastUpdatedDetectors(anyLong())).thenReturn(detectors);
        when(detectorService.getLastUsedDetectors(anyInt())).thenReturn(detectors);
    }
}
