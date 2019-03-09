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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.http.client.fluent.Content;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * {@link ModelServiceConnector} unit tests.
 */
public class ModelServiceConnectorTest {
    private static final String CONSTANT_DETECTOR = "constant-detector";
    private static final String EWMA_DETECTOR = "ewma-detector";
    private static final UUID DETECTOR_UUID = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_EMPTY = UUID.randomUUID();
    
    // FIXME The ModelServiceConnector uses this inconsistently. See the notes in that class for more information. [WLW]
    private static final String URI_TEMPLATE = "http://example.com/%s";
    
    private ModelServiceConnector connectorUnderTest;
    private MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    // This is just a basic ObjectMapper rather than a Jackson2ObjectMapper, since we are
    // just writing JSON here. (If we try to use the Jackson2ObjectMapper that we use in the
    // ModelServiceConnector we get an error:
    //
    // com.fasterxml.jackson.databind.JsonMappingException:
    //     Class org.springframework.hateoas.hal.Jackson2HalModule$HalResourcesSerializer
    //     has no default (no arg) constructor
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Mock
    private HttpClientWrapper httpClient;
    
    // Test objects
    private List<DetectorResource> detectorResourceList;
    private List<ModelResource> modelResourceList;
    private MetricDefinition metricDefinition;
    private Content detectorResourcesContent;
    private Content modelResourcesContent;
    private Content modelResourcesContent_empty;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.connectorUnderTest = new ModelServiceConnector(httpClient, URI_TEMPLATE);
    }
    
    @Test
    public void testConstructorInjection() {
        assertSame(httpClient, connectorUnderTest.getHttpClient());
        assertSame(URI_TEMPLATE, connectorUnderTest.getUriTemplate());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_httpClientNotNull() {
        new ModelServiceConnector(null, URI_TEMPLATE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_uriTemplateNotNull() {
        new ModelServiceConnector(httpClient, null);
    }
    
    @Test
    public void testFindDetectors() throws Exception {
        val result = connectorUnderTest.findDetectors(metricDefinition);
        assertEquals(detectorResourceList.size(), result.getContent().size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFindDetectors_metricDefinitionNotNull() throws Exception {
        connectorUnderTest.findDetectors(null);
    }

    @Test
    public void testFindLatestModels() throws Exception {
        val result = connectorUnderTest.findLatestModel(DETECTOR_UUID);
        assertNotNull(result);
    }
    
    @Test
    public void testFindLatestModels_empty() throws Exception {
        val result = connectorUnderTest.findLatestModel(DETECTOR_UUID_EMPTY);
        assertNull(result);
    }

    private void initTestObjects() throws Exception {
        val tags = new HashMap<String, String>();
        tags.put("org_id", "1");
        tags.put("mtype", "count");
        tags.put("unit", "");
        tags.put("what", "bookings");
        tags.put("interval", "5");
        
        this.metricDefinition = new MetricDefinition("some-key", new TagCollection(tags), TagCollection.EMPTY);
        
        // Find detectors
        this.detectorResourceList = new ArrayList<>();
        detectorResourceList.add(new DetectorResource(
                "3217d4be-9c33-490f-828e-c976b393b000",
                CONSTANT_DETECTOR,
                new ModelTypeResource(CONSTANT_DETECTOR)));
        detectorResourceList.add(new DetectorResource(
                "90c37a3c-f6bb-4c00-b41b-191909cccfb7",
                EWMA_DETECTOR,
                new ModelTypeResource(EWMA_DETECTOR)));
        val detectorResources = new DetectorResources(detectorResourceList);
        val detectorResourcesBytes = objectMapper.writeValueAsBytes(detectorResources);
        this.detectorResourcesContent = new Content(detectorResourcesBytes, ContentType.APPLICATION_JSON);
        
        // Find models
        this.modelResourceList = new ArrayList<>();
        modelResourceList.add(new ModelResource());
        val modelResources = new ModelResources(modelResourceList);
        val modelResourcesBytes = objectMapper.writeValueAsBytes(modelResources);
        this.modelResourcesContent = new Content(modelResourcesBytes, ContentType.APPLICATION_JSON);
        
        // Find models - empty list
        val modelResources_empty = new ModelResources(Collections.EMPTY_LIST);
        val modelResourcesBytes_empty = objectMapper.writeValueAsBytes(modelResources_empty);
        this.modelResourcesContent_empty = new Content(modelResourcesBytes_empty, ContentType.APPLICATION_JSON);
    }
    
    private void initDependencies() throws IOException {
        val metricId = metricTankIdFactory.getId(metricDefinition);
        val findDetectorsUri = String.format(URI_TEMPLATE, metricId);
        val findModelsUri = String.format(URI_TEMPLATE, DETECTOR_UUID);
        val findModelsUri_empty = String.format(URI_TEMPLATE, DETECTOR_UUID_EMPTY);
        
        when(httpClient.get(findDetectorsUri)).thenReturn(detectorResourcesContent);
        when(httpClient.get(findModelsUri)).thenReturn(modelResourcesContent);
        when(httpClient.get(findModelsUri_empty)).thenReturn(modelResourcesContent_empty);
    }
}
