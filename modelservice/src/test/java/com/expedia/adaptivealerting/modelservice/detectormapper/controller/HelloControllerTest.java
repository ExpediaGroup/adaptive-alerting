package com.expedia.adaptivealerting.modelservice.detectormapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    private static final String ENDPOINT = "/api/hello";

    @Value("${test.helloMsg}")
    String defaultMessage;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testHelloWithMessageParamReturnsMessage() throws Exception {
        final String expectedMessage = "test message";

        mvc.perform(get(ENDPOINT).param("message", expectedMessage)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString(expectedMessage)));
    }

    @Test
    public void testHelloWithoutMessageParamReturnsDefaultMessage() throws Exception {
        final String expectedMessage = defaultMessage;

        mvc.perform(get(ENDPOINT)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString(expectedMessage)));
    }
}