package com.expedia.adaptivealerting.modelservice.dto;

import com.expedia.adaptivealerting.modelservice.dto.common.Expression;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Detector;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.User;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.Assert.assertNotEquals;

public class CreateDetectorMappingRequestTest {
    private Expression expression;
    private Detector detector;
    private User user;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    private void initTestObjects(){
        val mom = ObjectMother.instance();
        this.expression = mom.getExpression();
        this.detector = new Detector(UUID.randomUUID());
        this.user = new User("yoda");
    }

    @Test
    public void testEquals() {
        val requestUnderTest = new CreateDetectorMappingRequest(expression, detector, user);
        assertNotEquals(requestUnderTest, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_nullUser() {
        val requestUnderTest = new CreateDetectorMappingRequest(expression, detector, null);
        requestUnderTest.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_nullDetector() {
        val requestUnderTest = new CreateDetectorMappingRequest(expression, null, user);
        requestUnderTest.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_nullExpression() {
        val requestUnderTest = new CreateDetectorMappingRequest(null, detector, user);
        requestUnderTest.validate();
    }
}
