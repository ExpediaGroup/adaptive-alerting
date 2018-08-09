package com.expedia.adaptivealerting.modelservice.util;

import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    public static void sleep(int mins) throws InterruptedException {
        TimeUnit.MINUTES.sleep(mins);
    }
}
