/*
 * Copyright 2018 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.anomdetect.rcf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesCache {
    private final Properties properties = new Properties();
    
    private PropertiesCache() {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("randomcutforest.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new FailedToLoadPropertiesException(e);
        }
    }
    
    private static class InstanceHolder {
        private static final PropertiesCache INSTANCE = new PropertiesCache();
    }
    
    public static PropertiesCache getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    public String get(String key) {
        return properties.getProperty(key);
    }
}
