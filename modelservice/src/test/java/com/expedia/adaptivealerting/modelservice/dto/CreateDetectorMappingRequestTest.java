package com.expedia.adaptivealerting.modelservice.dto;

import com.expedia.adaptivealerting.modelservice.dto.detectormapping.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Detector;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.User;
import com.expedia.adaptivealerting.modelservice.dto.common.Expression;
import com.expedia.adaptivealerting.modelservice.dto.common.Operator;
import com.expedia.adaptivealerting.modelservice.dto.common.Operand;
import com.expedia.adaptivealerting.modelservice.dto.common.Field;

import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import org.apache.commons.math3.analysis.function.Exp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotEquals;

public class CreateDetectorMappingRequestTest {

    private Expression expression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    private void initTestObjects(){
        ObjectMother mom = ObjectMother.instance();
        this.expression = mom.getExpression();
    }

    @Test
    public void testEquals() {
        CreateDetectorMappingRequest createDetectorMappingRequest = new CreateDetectorMappingRequest();
        createDetectorMappingRequest.setExpression(expression);
        createDetectorMappingRequest.setUser(new User("1"));
        createDetectorMappingRequest.setDetector(new Detector(UUID.randomUUID()));
        assertNotEquals(createDetectorMappingRequest, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull_user_request() {
        CreateDetectorMappingRequest createDetectorMappingRequest = new CreateDetectorMappingRequest();
        createDetectorMappingRequest.setExpression(expression);
        createDetectorMappingRequest.setDetector(new Detector(UUID.randomUUID()));
        createDetectorMappingRequest.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull_detector_request() {
        CreateDetectorMappingRequest createDetectorMappingRequest = new CreateDetectorMappingRequest();
        createDetectorMappingRequest.setExpression(expression);
        createDetectorMappingRequest.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull_expression_request() {
        CreateDetectorMappingRequest createDetectorMappingRequest = new CreateDetectorMappingRequest();
        createDetectorMappingRequest.setUser(new User("1"));
        createDetectorMappingRequest.setDetector(new Detector(UUID.randomUUID()));
        createDetectorMappingRequest.validate();
    }
}
