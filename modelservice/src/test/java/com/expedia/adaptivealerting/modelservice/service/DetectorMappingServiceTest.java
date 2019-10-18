package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.dto.detectormapping.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Detector;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.User;
import com.expedia.adaptivealerting.modelservice.entity.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.repo.DetectorMappingRepository;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DetectorMappingServiceTest {

    @InjectMocks
    private DetectorMappingService detectorMappingService = new DetectorMappingServiceImpl();

    @Mock
    private DetectorMappingRepository detectorMappingRepository;

    private DetectorMapping detectorMapping;
    private MatchingDetectorsResponse matchingDetectorsResponse;
    private List<DetectorMapping> detectorMappings = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testFindMatchingDetectorMappings() {
        val actualMapping = detectorMappingService.findMatchingDetectorMappings(new ArrayList<>());
        assertNotNull(actualMapping);
        assertEquals("aeb4d849-847a-45c0-8312-dc0fcf22b639", actualMapping.getGroupedDetectorsBySearchIndex().get(0).get(0).getUuid().toString());
        assertEquals(10000, actualMapping.getLookupTimeInMillis());
    }

    @Test
    public void testCreateDetectorMapping() {
        val mom = ObjectMother.instance();
        val expr = mom.getExpression();
        val detector = new Detector(UUID.randomUUID());
        val user = new User("yoda");
        val request = new CreateDetectorMappingRequest(expr, detector, user);

        val actualCreateId = detectorMappingService.createDetectorMapping(request);

        assertNotNull(actualCreateId);
        assertEquals("1", actualCreateId);
    }

    @Test
    public void testDeleteDetectorMapping() {
        val detectorMappingService = mock(DetectorMappingService.class);
        doNothing().when(detectorMappingService).deleteDetectorMapping(anyString());
        detectorMappingService.deleteDetectorMapping("aeb4d849-847a-45c0-8312-dc0fcf22b639");
        verify(detectorMappingService, Mockito.times(1)).deleteDetectorMapping("aeb4d849-847a-45c0-8312-dc0fcf22b639");
    }

    @Test
    public void testFindDetectorMapping() {
        val detectorMapping = detectorMappingService.findDetectorMapping("id");
        assertNotNull(detectorMapping);
        assertCheck(detectorMapping);
    }

    @Test
    public void testSearch() {
        val detectorMappings = detectorMappingService.search(new SearchMappingsRequest());
        assertNotNull(detectorMappings);
        assertCheck(detectorMappings.get(0));
    }

    @Test
    public void testFindLastUpdated() {
        val detectorMappings = detectorMappingService.findLastUpdated(1000);
        assertNotNull(detectorMappings);
        assertCheck(detectorMappings.get(0));
    }

    @Test
    public void testDisableDetectorMapping() {
        val detectorMappingService = mock(DetectorMappingService.class);
        doNothing().when(detectorMappingService).disableDetectorMapping(anyString());
        detectorMappingService.disableDetectorMapping("aeb4d849-847a-45c0-8312-dc0fcf22b639");
        verify(detectorMappingService, Mockito.times(1)).disableDetectorMapping("aeb4d849-847a-45c0-8312-dc0fcf22b639");
    }

    private void initTestObjects() {
        val mom = ObjectMother.instance();
        detectorMapping = mom.getDetectorMapping();
        matchingDetectorsResponse = mom.getMatchingDetectorsResponse();
        detectorMappings.add(detectorMapping);
    }

    private void initDependencies() {
        Mockito.when(detectorMappingRepository.findMatchingDetectorMappings(Mockito.any(List.class))).thenReturn(matchingDetectorsResponse);
        Mockito.when(detectorMappingRepository.findDetectorMapping(anyString())).thenReturn(detectorMapping);
        Mockito.when(detectorMappingRepository.search(Mockito.any(SearchMappingsRequest.class))).thenReturn(detectorMappings);
        Mockito.when(detectorMappingRepository.findLastUpdated(Mockito.anyInt())).thenReturn(detectorMappings);
        Mockito.when(detectorMappingRepository.createDetectorMapping(Mockito.any(CreateDetectorMappingRequest.class))).thenReturn("1");
    }

    private void assertCheck(DetectorMapping detectorMapping) {
        Assert.assertEquals("aeb4d849-847a-45c0-8312-dc0fcf22b639", detectorMapping.getDetector().getUuid().toString());
        Assert.assertEquals("test-user", detectorMapping.getUser().getId());
        Assert.assertEquals(10000, detectorMapping.getCreatedTimeInMillis());
        Assert.assertEquals(true, detectorMapping.isEnabled());
    }
}
