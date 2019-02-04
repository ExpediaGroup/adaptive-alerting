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
import org.apache.http.client.fluent.Content;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * {@link ModelServiceConnector} unit tests.R
 *
 * @author Willie Wheeler
 */
public class ModelResourceServiceConnectorTest {
    private static final String URI_TEMPLATE = "http://example.com/%s";
    
    // Class under test
    private ModelServiceConnector connector;
    
    @Mock
    private HttpClientWrapper httpClient;
    
    // Test objects
    private MetricDefinition metricDefinition;
    private Content modelsContent;
    private Resources<DetectorResource> detectorResources;
    
    // Util
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.connector = new ModelServiceConnector(httpClient, URI_TEMPLATE);
    }
    
    @Test
    public void testConstructorInjection() {
        assertSame(httpClient, connector.getHttpClient());
        assertSame(URI_TEMPLATE, connector.getUriTemplate());
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
    public void testFindDetectors() {
        final Resources<DetectorResource> results = connector.findDetectors(metricDefinition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFindDetectors_metricDefinitionNotNull() {
        connector.findDetectors(null);
    }
    
    private void initTestObjects() throws Exception {
        final Map<String, String> tags = new HashMap<>();
        tags.put("org_id", "1");
        tags.put("mtype", "count");
        tags.put("unit", "");
        tags.put("what", "bookings");
        tags.put("interval", "5");
        
        this.metricDefinition = new MetricDefinition("some-key", new TagCollection(tags), TagCollection.EMPTY);
        
        final List<DetectorResource> models = new ArrayList<>();
        models.add(new DetectorResource("3217d4be-9c33-490f-828e-c976b393b000", new ModelTypeResource("constant-detector")));
        models.add(new DetectorResource("90c37a3c-f6bb-4c00-b41b-191909cccfb7", new ModelTypeResource("ewma-detector")));
        
        this.detectorResources = new Resources<>(models);
        
        final ObjectMapper objectMapper = new ObjectMapper();
        final byte[] modelResourcesBytes = objectMapper.writeValueAsBytes(detectorResources);
        this.modelsContent = new Content(modelResourcesBytes, ContentType.APPLICATION_JSON);
    }
    
    private void initDependencies() throws Exception {
        final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
        final String id = metricTankIdFactory.getId(metricDefinition);
        final String uri = String.format(URI_TEMPLATE, id);
        
        when(httpClient.get(uri)).thenReturn(modelsContent);
    }
}
