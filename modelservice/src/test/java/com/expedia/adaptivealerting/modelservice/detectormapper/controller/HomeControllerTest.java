package com.expedia.adaptivealerting.modelservice.detectormapper.controller;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HomeControllerTest {

    @Test
    public void testHomeControllerClass() {
        final HomeController controller = new HomeController();
        assertTrue(controller.home().equals("redirect:swagger-ui.html"));
    }
}
