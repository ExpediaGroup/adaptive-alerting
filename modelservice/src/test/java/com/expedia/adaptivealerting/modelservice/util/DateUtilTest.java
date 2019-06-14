package com.expedia.adaptivealerting.modelservice.util;

import lombok.SneakyThrows;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import static com.expedia.adaptivealerting.modelservice.util.DateUtil.now;
import static com.expedia.adaptivealerting.modelservice.util.DateUtil.toUtcDateString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateUtilTest {

    private long instMillis = 1478555500000L;
    private String instString = "2016-11-07 21:51:40";
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Test
    public void testNewDate() {
        Date currentDate = now();
        assertNotNull(currentDate);
    }

    @Test
    public void testToUtcDateString() {
        String actual = toUtcDateString(Instant.ofEpochMilli(instMillis));
        assertEquals(instString, actual);
    }

    @Test
    public void testToUTCDate() {
        DateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date actual = DateUtil.toUTCDate(instString);
        try {
            Date expected = format.parse("2016-11-07 21:51:40");
            assertEquals(actual, expected);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = ParseException.class)
    @SneakyThrows
    public void testToUTCDateThrowsException() {
        DateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expected = format.parse("2016-11-07T21:51:40Z");
    }
}
