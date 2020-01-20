package com.expedia.adaptivealerting.anomdetect.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

    public static String getValueFromProperty(String key) {
        Properties props;
        try {
            props = new PropertiesLoader().getPropValues();
            return props.getProperty(key);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
