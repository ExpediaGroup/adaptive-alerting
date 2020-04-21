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
import com.expedia.adaptivealerting.modelservice.entity.User;
import com.expedia.adaptivealerting.modelservice.entity.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.DetectorMappingRepository;
import com.expedia.adaptivealerting.modelservice.repo.request.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.repo.response.MatchingDetectorsResponse;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@AutoConfigureMockMvc
public class DetectorMappingControllerTest {

    @Mock
    private DetectorMappingRepository detectorMappingRepo;

    private String id = "adsvade8^szx";
    private String detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
    private String userVal = "test-user";

    @InjectMocks
    private DetectorMappingController controllerUnderTest;

    @Before
    public void setUp() {
        this.controllerUnderTest = new DetectorMappingController();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDetectorMappings_successful() throws IOException {
        DetectorMapping detectorMapping = mockDetectorMapping(id);
        when(detectorMappingRepo.findDetectorMapping(id)).thenReturn(detectorMapping);
        DetectorMapping detectorMappingreturned = controllerUnderTest.getDetectorMapping(id);
        assertNotNull("Response can't be null", detectorMappingreturned);
        assertEquals(UUID.fromString(detectorUuid), detectorMappingreturned.getDetector().getUuid());
        assertEquals(id, detectorMappingreturned.getId());
        assertEquals(userVal, detectorMapping.getUser().getId());
        assertEquals(true, detectorMapping.isEnabled());
    }

    @Test(expected = RecordNotFoundException.class)
    public void testGetDetectorMappings_record_not_found() {
        when(detectorMappingRepo.findDetectorMapping(id)).thenReturn(null);
        DetectorMapping detectorMapping = controllerUnderTest.getDetectorMapping(id);
    }

    @Test(expected = RuntimeException.class)
    public void testGetDetectorMappings_fail() {
        when(detectorMappingRepo.findDetectorMapping(id)).thenReturn(new DetectorMapping());
        DetectorMapping detectorMapping = controllerUnderTest.getDetectorMapping(id);
        assertNotNull("Response can't be null", detectorMapping);
        assertEquals(detectorUuid, detectorMapping.getDetector().getUuid());
        assertEquals(id, detectorMapping.getId());
    }

    @Test
    @ResponseStatus(value = HttpStatus.OK)
    public void testDisableDetectorMappings() {
        controllerUnderTest.disableDeleteDetectorMapping(id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisableDetectorMappings_notNull() {
        controllerUnderTest.disableDeleteDetectorMapping(null);
    }

    @Test
    @ResponseStatus(value = HttpStatus.OK)
    public void testDeleteDetectorMappings() {
        controllerUnderTest.deleteDetectorMapping(id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteDetectorMappings_notNull() {
        controllerUnderTest.deleteDetectorMapping(null);
    }

    @Test
    public void testDetectorMappingSearch() {
        List<DetectorMapping> detectorMappings = mockDetectorMappingsList();
        SearchMappingsRequest searchMappingsRequest = new SearchMappingsRequest();
        searchMappingsRequest.setDetectorUuid(UUID.fromString(detectorUuid));
        searchMappingsRequest.setUserId(userVal);
        when(detectorMappingRepo.search(searchMappingsRequest)).thenReturn(detectorMappings);
        List<DetectorMapping> detectorMappingsResponse = controllerUnderTest.searchDetectorMapping(searchMappingsRequest);
        assertEquals(id, detectorMappingsResponse.get(0).getId());
        assertEquals(detectorUuid, detectorMappingsResponse.get(0).getDetector().getUuid().toString());
        assertEquals("test-user", detectorMappingsResponse.get(0).getUser().getId());
    }

    @Test
    public void testGetLastUpdated_successful() {
        val timeInSecs = 60;
        List<DetectorMapping> mockDetectorMappings = mockDetectorMappingsList();
        when(detectorMappingRepo.findLastUpdated(timeInSecs)).thenReturn(mockDetectorMappings);
        List<DetectorMapping> detectorMappings = controllerUnderTest.findDetectorMapping(timeInSecs);
        assertNotNull("Response can't be null", detectorMappings);
        assertEquals(1, detectorMappings.size());
        assertEquals(UUID.fromString(detectorUuid), detectorMappings.get(0).getDetector().getUuid());
        assertEquals(id, detectorMappings.get(0).getId());
    }

    @Test(expected = RuntimeException.class)
    public void testGetLastUpdated_fail() {
        val timeInSecs = 60;
        when(detectorMappingRepo.findLastUpdated(timeInSecs)).thenThrow(new IOException());
        List<DetectorMapping> detectorMappings = controllerUnderTest.findDetectorMapping(timeInSecs);
        assertNotNull("Response can't be null", detectorMappings);
        assertEquals(0, detectorMappings.size());
    }

    @Test
    public void testFindMatchingByTags() {
        val lookupTime = 60;
        List<Map<String, String>> tagsList = new ArrayList<>();
        MatchingDetectorsResponse mockMatchingDetectorsResponse = mockMatchingDetectorsResponse(lookupTime, detectorUuid);
        when(detectorMappingRepo.findMatchingDetectorMappings(tagsList)).thenReturn(mockMatchingDetectorsResponse);
        MatchingDetectorsResponse matchingDetectorsResult = controllerUnderTest.searchDetectorMapping(tagsList);
        Assert.assertEquals(1, matchingDetectorsResult.getGroupedDetectorsBySearchIndex().size());
        List<Detector> detectors = matchingDetectorsResult.getGroupedDetectorsBySearchIndex().get(1);
        assertEquals(1, detectors.size());
        assertEquals(UUID.fromString(detectorUuid), detectors.get(0).getUuid());
    }

    private DetectorMapping mockDetectorMapping(String id) {
        DetectorMapping detectorMapping = mock(DetectorMapping.class);
        Detector detector = new Detector("ad-manager", UUID.fromString(detectorUuid));
        User user = new User(userVal);
        when(detectorMapping.getDetector()).thenReturn(detector);
        when(detectorMapping.getId()).thenReturn(id);
        when(detectorMapping.getUser()).thenReturn(user);
        when(detectorMapping.isEnabled()).thenReturn(true);
        return detectorMapping;
    }

    private List<DetectorMapping> mockDetectorMappingsList() {
        DetectorMapping detectorMapping = mock(DetectorMapping.class);
        List<DetectorMapping> detectorMappingsList = new ArrayList<>();
        Detector detector = new Detector("ad-manager", UUID.fromString(detectorUuid));
        when(detectorMapping.getDetector()).thenReturn(detector);
        when(detectorMapping.getId()).thenReturn(id);
        when(detectorMapping.getUser()).thenReturn(new User(userVal));
        detectorMappingsList.add(detectorMapping);
        return detectorMappingsList;
    }

    private MatchingDetectorsResponse mockMatchingDetectorsResponse(int lookupTime, String detectorUuid) {
        Map<Integer, List<Detector>> matchingDetectorsResponseMap = new HashMap<>();
        List<Detector> DetectorList = new ArrayList<>();
        DetectorList.add(new Detector("ad-manager", UUID.fromString(detectorUuid)));
        matchingDetectorsResponseMap.put(1, DetectorList);
        MatchingDetectorsResponse matchingDetectorsResponse = new MatchingDetectorsResponse(matchingDetectorsResponseMap, lookupTime);
        return matchingDetectorsResponse;
    }
}
