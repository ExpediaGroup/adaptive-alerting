package com.expedia.adaptivealerting.anomdetect.util;

import lombok.Generated;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Generated
public class PropertyValues {
    private static final String CONFIG_FILE_NAME = "config.properties";

    public Properties getPropValues() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + CONFIG_FILE_NAME + "' not found in the classpath");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
        return prop;
    }
}