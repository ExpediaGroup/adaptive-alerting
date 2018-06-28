package com.expedia.adaptivealerting.anomdetect.randomcutforest.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesCache
{
    private final Properties properties = new Properties();

    private PropertiesCache()
    {

        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("randomcutforest.properties"))
        {
            properties.load(in);
        }
        catch (IOException e)
        {
            throw new FailedToLoadPropertiesException(e);
        }
    }

    private static class InstanceHolder
    {
        private static final PropertiesCache INSTANCE = new PropertiesCache();
    }

    public static PropertiesCache getInstance()
    {
        return InstanceHolder.INSTANCE;
    }

    public String get(String key)
    {
        return properties.getProperty(key);
    }
}