package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.modelservice.exception.CustomExceptionHandler;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.DetectorRepository;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private DetectorController controllerUnderTest;

    private MockMvc mockMvc;

    @Mock
    private DetectorRepository detectorRepo;

    @Mock
    private DetectorDocument detector;

    @Mock
    private List<DetectorDocument> detectors;

    private UUID someUuid;
    private DetectorDocument legalParamsDetector;

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
    public void testFindByUuid_record_not_found() {
        when(detectorRepo.findByUuid(anyString())).thenReturn(null);
        controllerUnderTest.findByUuid(someUuid.toString());
    }

    @Test
    public void testFindByUuid_fail() throws Exception {
        mockMvc.perform(get("/findByUuid").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError());
    }

    @Test
    public void testFindByCreatedBy() {
        when(detectorRepo.findByCreatedBy(anyString())).thenReturn(detectors);
        val actualDetectors = controllerUnderTest.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_exception_FindByCreatedBy() {
        val actualDetectors = controllerUnderTest.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
    }

    @Test
    public void testFindByCreatedBy_fail() throws Exception {
        mockMvc.perform(get("/findByCreatedBy").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        val actualDetectors = controllerUnderTest.getLastUpdatedDetectors(5);
        assertNotNull(actualDetectors);
        assertSame(detectors, actualDetectors);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLastUpdatedDetectors_fail() {
        when(detectorRepo.getLastUpdatedDetectors(anyLong())).thenReturn(null);
        controllerUnderTest.getLastUpdatedDetectors(5);
    }

    @Test
    public void testUpdateDetector() {
        controllerUnderTest.updateDetector(someUuid.toString(), legalParamsDetector);
    }

    @Test
    public void testToggleDetector() {
        controllerUnderTest.toggleDetector(someUuid.toString(), true);
    }

    @Test
    public void testTrustDetector() {
        controllerUnderTest.trustDetector(someUuid.toString(), true);
    }

    @Test
    public void testDeleteDetector() {
        val someUuidStr = someUuid.toString();
        controllerUnderTest.deleteDetector(someUuidStr);
        verify(detectorRepo, times(1)).deleteDetector(someUuidStr);
    }

    private void initTestObjects() {
        this.someUuid = UUID.randomUUID();
        val mom = ObjectMother.instance();
        this.legalParamsDetector = mom.getDetectorDocument();
        legalParamsDetector.setUuid(someUuid);
    }

    private void initDependencies() {
        when(detectorRepo.createDetector(any(DetectorDocument.class))).thenReturn(someUuid);
        when(detectorRepo.findByUuid(someUuid.toString())).thenReturn(detector);
        when(detectorRepo.getLastUpdatedDetectors(anyLong())).thenReturn(detectors);
    }
}
