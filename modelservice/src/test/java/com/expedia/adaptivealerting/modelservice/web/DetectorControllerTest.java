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
import com.expedia.adaptivealerting.modelservice.tracing.Trace;
import com.expedia.www.haystack.client.SpanContext;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
    private HttpHeaders httpHeaders = new HttpHeaders();
    private Trace trace = Mockito.mock(Trace.class);
    private Tracer noOpsTracer;

    @Before
    public void setUp() {
        this.controllerUnderTest = new DetectorController();
        mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).setHandlerExceptionResolvers(new ExceptionHandlerExceptionResolver()).build();

        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        httpHeaders.add("test-header-key", "test-header-value");
        val metrics = new NoopMetricsRegistry();
        val dispatcher = new NoopDispatcher();
        noOpsTracer = new Tracer.Builder(metrics, "testTrace", dispatcher).build();
    }

    @Test
    public void testCreateDetector() {
        val testDetectorMappingSpanContext = new SpanContext(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID());
        val testChildSpan = noOpsTracer.buildSpan("create-detector").asChildOf(testDetectorMappingSpanContext).start();
        when(trace.extractParentSpan(httpHeaders)).thenReturn(testDetectorMappingSpanContext);
        when(trace.startSpan("create-detector", testDetectorMappingSpanContext)).thenReturn(testChildSpan);
        val uuidStr = controllerUnderTest.createDetector(legalParamsDetector, httpHeaders);
        val uuid = UUID.fromString(uuidStr);
        assertNotNull(uuid);
    }

    @Test
    public void testFindByUuid() {
        val testDetectorMappingSpanContext = new SpanContext(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID());
        val testChildSpan = noOpsTracer.buildSpan("find-detector-by-uuid").asChildOf(testDetectorMappingSpanContext).start();
        when(trace.extractParentSpan(httpHeaders)).thenReturn(testDetectorMappingSpanContext);
        when(trace.startSpan("find-detector-by-uuid", testDetectorMappingSpanContext)).thenReturn(testChildSpan);
        val actualDetector = controllerUnderTest.findByUuid(someUuid.toString(), httpHeaders);
        assertNotNull(actualDetector);
    }


    @Test(expected = RecordNotFoundException.class)
    public void testFindByUuid_record_not_found_null_response() {
        val testDetectorMappingSpanContext = new SpanContext(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID());
        val testChildSpan = noOpsTracer.buildSpan("find-detector-by-uuid").asChildOf(testDetectorMappingSpanContext).start();
        when(trace.extractParentSpan(httpHeaders)).thenReturn(testDetectorMappingSpanContext);
        when(trace.startSpan("find-detector-by-uuid", testDetectorMappingSpanContext)).thenReturn(testChildSpan);
        when(detectorService.findByUuid(anyString())).thenReturn(null);
        controllerUnderTest.findByUuid(someUuid.toString(), httpHeaders);
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
        Map<String, String> requestBody = Collections.singletonMap("detectorUuid", someUuid.toString());
        controllerUnderTest.updatedDetectorLastUsed(requestBody);
        verify(detectorService, times(1)).updateDetectorLastUsed(someUuid.toString());
    }

    @Test
    public void testDeleteDetector() {
        val someUuidStr = someUuid.toString();
        controllerUnderTest.deleteDetector(someUuidStr);
        verify(detectorService, times(1)).deleteDetector(someUuidStr);
    }

    @Test
    public void testFindByNextRun() {
        val testDetectorMappingSpanContext = new SpanContext(UUID.randomUUID(), UUID.randomUUID(),
            UUID.randomUUID());
        val testChildSpan = noOpsTracer.buildSpan("find-detectors-to-train-next").asChildOf(testDetectorMappingSpanContext).start();
        when(trace.extractParentSpan(httpHeaders)).thenReturn(testDetectorMappingSpanContext);
        when(trace.startSpan("find-detectors-to-train-next", testDetectorMappingSpanContext)).thenReturn(testChildSpan);

        controllerUnderTest.getNextDetectorsToTrain(httpHeaders);
        verify(detectorService, times(1)).getDetectorsToBeTrained();
    }

    @Test
    public void testUpdateTrainingRunTime() {
        val timestamp = System.currentTimeMillis();
        val uuid = this.someUuid.toString();
        val testDetectorMappingSpanContext = new SpanContext(uuid, uuid, uuid);
        val testChildSpan = noOpsTracer.buildSpan("update-detector-training-time").asChildOf(testDetectorMappingSpanContext).start();
        when(trace.extractParentSpan(httpHeaders)).thenReturn(testDetectorMappingSpanContext);
        when(trace.startSpan("update-detector-training-time", testDetectorMappingSpanContext)).thenReturn(testChildSpan);

        controllerUnderTest.updateDetectorTrainingTime(uuid, timestamp, httpHeaders);
        verify(detectorService, times(1)).updateDetectorTrainingTime(uuid, timestamp);
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
