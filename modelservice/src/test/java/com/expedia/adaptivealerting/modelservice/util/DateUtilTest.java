package com.expedia.adaptivealerting.modelservice.util;

import lombok.val;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import static com.expedia.adaptivealerting.modelservice.util.DateUtil.toUtcDateString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DateUtilTest {
    private long instMillis = 1478555500000L;
    private String instString = "2016-11-07 21:51:40";
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Test(expected = InvocationTargetException.class)
    public void testConstructorIsPrivate() throws Exception {
        // https://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
        val constructor = DateUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testToUtcDate() {
        val format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date actual = DateUtil.toUtcDate(instString);
        try {
            val expected = format.parse("2016-11-07 21:51:40");
            assertEquals(actual, expected);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUtcDate_nullDateStr() {
        DateUtil.toUtcDate(null);
    }

    @Test(expected = RuntimeException.class)
    public void testToUtcDate_invalidDateString() {
        DateUtil.toUtcDate("i_am_not_a_parseable_date");
    }

    @Test
    public void testToDateString() {
        val instant = Instant.parse("2000-01-01T00:00:00Z");
        val actual = DateUtil.toDateString(instant);
        assertEquals("2000-01-01 00:00:00", actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToDateString_nullInstant() {
        DateUtil.toDateString(null);
    }

    @Test
    public void testToUtcDateString() {
        val actual = toUtcDateString(Instant.ofEpochMilli(instMillis));
        assertEquals(instString, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUtcDateString_nullInstant() {
        DateUtil.toUtcDateString(null);
    }

    @Test
    public void testNow() {
        assertNotNull(DateUtil.now());
    }
}
