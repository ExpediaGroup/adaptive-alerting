package com.expedia.adaptivealerting.modelservice.web;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.modelservice.dao.es.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.dao.es.ElasticSearchConfig;
import com.expedia.adaptivealerting.modelservice.dao.es.ElasticSearchDetectorMappingService;
import com.expedia.adaptivealerting.modelservice.model.*;
import lombok.val;
import org.elasticsearch.common.document.DocumentField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectorMappingControllerTest {

    @Mock
    private MetricRegistry metricRegistry;

    private ElasticSearchClient elasticSearchClient;
    @Mock
    private ElasticSearchConfig elasticSearchConfig;
    @Mock
    private ElasticSearchDetectorMappingService detectorMappingService;

    // Class under test
    @InjectMocks
    private DetectorMappingController controller;

    @Before
    public void setUp() {
        this.controller = new DetectorMappingController();
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void beforeTest() {
        when(metricRegistry.timer(any())).thenReturn(mock(Timer.class));
        when(metricRegistry.counter(any())).thenReturn(mock(Counter.class));
        detectorMappingService = new ElasticSearchDetectorMappingService(metricRegistry);
        ReflectionTestUtils.setField(detectorMappingService, "elasticSearchClient", elasticSearchClient);
        ReflectionTestUtils.setField(detectorMappingService, "elasticSearchConfig", elasticSearchConfig);
    }

    @Test
    public void testgetDetectorMappings_successful() throws IOException {
        val id = "adsvade8^szx";
        DetectorMapping detectorMapping = mockDetectorMapping(id);
        when(detectorMappingService.findDetectorMapping(id)).thenReturn(detectorMapping);
        DetectorMapping detectorMappingreturned = controller.getDetectorMapping(id);
        assertNotNull("Response can't be null", detectorMappingreturned);
        assertEquals(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"), detectorMappingreturned.getDetector().getId());
        assertEquals(id, detectorMappingreturned.getId());
        assertEquals("test-user", detectorMapping.getUser().getId().toString());
        assertEquals(true, detectorMapping.isEnabled());
    }

    @Test(expected = RuntimeException.class)
    public void testgetDetectorMappings_fail() throws IOException {
        val id = "adsvade8^szx";
        when(detectorMappingService.findDetectorMapping(id)).thenReturn(new DetectorMapping());
        DetectorMapping detectorMappingreturned = controller.getDetectorMapping(id);
        assertNotNull("Response can't be null", detectorMappingreturned);
        assertEquals("aeb4d849-847a-45c0-8312-dc0fcf22b639", detectorMappingreturned.getDetector().getId());
        assertEquals(id, detectorMappingreturned.getId());
    }

    @Test
    public void testdisableDetectorMappings() throws IOException {
        val id = "adsvade8^szx";
        ResponseEntity responseEntity = controller.disableDeleteDetectorMapping(id);
        assertNotNull("Response can't be null", responseEntity);
        assertEquals(new ResponseEntity(HttpStatus.OK), responseEntity);
    }

    @Test
    public void testdeleteDetectorMappings() throws IOException {
        val id = "adsvade8^szx";
        ResponseEntity responseEntity = controller.deleteDetectorMapping(id);
        assertNotNull("Response can't be null", responseEntity);
        assertEquals(new ResponseEntity(HttpStatus.OK), responseEntity);
    }

    @Test
    public void testgetLastUpdated_successful() throws IOException {
        val id = "adsvade8^szx";
        val TimeinSecs = 60;
        List<DetectorMapping> mockeddetectorMappingsList = mockDetectorMappingsList();
        when(detectorMappingService.findLastUpdated(TimeinSecs)).thenReturn(mockeddetectorMappingsList);
        List<DetectorMapping> listofdetectorMappingsreturned = controller.findDetectorMapping(TimeinSecs);
        assertNotNull("Response can't be null", listofdetectorMappingsreturned);
        assertEquals(1, listofdetectorMappingsreturned.size());
        assertEquals(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"), listofdetectorMappingsreturned.get(0).getDetector().getId());
        assertEquals("adsvade8^szx", listofdetectorMappingsreturned.get(0).getId());
    }

    @Test(expected = RuntimeException.class)
    public void testgetLastUpdated_fail() throws IOException {
        val TimeinSecs = 60;
        when(detectorMappingService.findLastUpdated(TimeinSecs)).thenThrow(new IOException());
        List<DetectorMapping> listofdetectorMappingsreturned = controller.findDetectorMapping(TimeinSecs);
        assertNotNull("Response can't be null", listofdetectorMappingsreturned);
        assertEquals(0, listofdetectorMappingsreturned.size());
    }

    private DetectorMapping mockDetectorMapping(String id) {
        DetectorMapping detectorMapping = mock(DetectorMapping.class);
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        Detector detector = new Detector(UUID.fromString(detectorUuid));
        User user = new User("test-user");
        when(detectorMapping.getDetector()).thenReturn(detector);
        when(detectorMapping.getId()).thenReturn(id);
        when(detectorMapping.getUser()).thenReturn(user);
        when(detectorMapping.isEnabled()).thenReturn(true);
        return detectorMapping;
    }

    private List<DetectorMapping> mockDetectorMappingsList() {
        DetectorMapping detectorMapping = mock(DetectorMapping.class);
        List<DetectorMapping> detectorMappingsList = new ArrayList<DetectorMapping>();
        val id = "adsvade8^szx";
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        Detector detector = new Detector(UUID.fromString(detectorUuid));
        when(detectorMapping.getDetector()).thenReturn(detector);
        when(detectorMapping.getId()).thenReturn(id);
        detectorMappingsList.add(detectorMapping);
        return detectorMappingsList;
    }
}