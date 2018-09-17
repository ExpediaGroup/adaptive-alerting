package com.expedia.adaptivealerting.modelservice.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static junit.framework.TestCase.assertEquals;

public class HealhControllerTests {

    /* Class under test */
    @InjectMocks
    private HealthController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsActive() {
        boolean isActive = controller.isActive();
        assertEquals(isActive, true);
    }

}
