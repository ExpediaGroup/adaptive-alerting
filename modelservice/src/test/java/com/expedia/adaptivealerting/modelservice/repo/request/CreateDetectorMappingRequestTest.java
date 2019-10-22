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
package com.expedia.adaptivealerting.modelservice.repo.request;

import com.expedia.adaptivealerting.modelservice.entity.Expression;
import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.entity.User;
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
