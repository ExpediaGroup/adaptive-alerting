package com.expedia.adaptivealerting.modelservice.exception;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExceptionResponseTest {

    @Test
    public void testEquals() {
        ExceptionResponse e1 = new ExceptionResponse(new Date(), "message", "details");
        ExceptionResponse e2 = new ExceptionResponse(new Date(), "message", "details");
        ExceptionResponse e3 = new ExceptionResponse(new Date(), "message1", "details1");

        assertEquals(e1, e2);
        assertEquals(e2, e1);
        assertNotEquals(e3, e1);
        assertNotEquals(e1, null);
    }
}
