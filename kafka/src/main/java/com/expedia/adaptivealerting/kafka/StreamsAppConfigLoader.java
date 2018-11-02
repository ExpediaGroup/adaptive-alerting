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
package com.expedia.adaptivealerting.kafka;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Loads the application configuration.
 *
 * @author Willie Wheeler
 */
public final class StreamsAppConfigLoader {
    
    /**
     * Base configuration path.
     */
    private static final String BASE_APP_CONFIG_PATH = "config/base.conf";
    
    /**
     * Overrides configuration path environment variable.
     */
    private static final String EV_OVERRIDES_CONFIG_PATH = "AA_OVERRIDES_CONFIG_PATH";
    
    /**
     * Fallback configuration key.
     */
    private static final String CK_DEFAULT_APP_CONFIG = "kstream.app.default";
    
    /**
     * Prevent instantiation.
     */
    private StreamsAppConfigLoader() {
    }
    
    public static StreamsAppConfig load(String appKey) {
        notNull(appKey, "appKey can't be null");
        return new StreamsAppConfig(overridesConfig(appKey).withFallback(baseConfig(appKey)));
    }
    
    private static Config baseConfig(String appKey) {
        // This is the configuration we include in the app JAR itself.
        val baseAppConfigs = ConfigFactory.load(BASE_APP_CONFIG_PATH);
        val defaultAppConfig = baseAppConfigs.getConfig(CK_DEFAULT_APP_CONFIG);
        return baseAppConfigs.getConfig(appKey).withFallback(defaultAppConfig);
    }
    
    private static Config overridesConfig(String appKey) {
        // This is externalized configuration.
        String overridesPath = System.getenv(EV_OVERRIDES_CONFIG_PATH);
        if (overridesPath == null) {
            overridesPath = "/config/" + appKey + ".conf";
        }
        return ConfigFactory.load(overridesPath);
    }
}
