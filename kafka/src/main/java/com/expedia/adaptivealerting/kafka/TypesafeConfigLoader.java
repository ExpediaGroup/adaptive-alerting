/*
 * Copyright 2018-2019 Expedia Group, Inc.
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
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Loads the Typesafe base configuration from the classpath.
 */
@Slf4j
public class TypesafeConfigLoader {

    /**
     * Base configuration path.
     */
    private static final String BASE_APP_CONFIG_PATH = "config/base.conf";

    /**
     * Fallback configuration key.
     */
    private static final String CK_KSTREAM_APP_DEFAULT_CONFIG = "kstream.app.default";

    /**
     * Overrides configuration path environment variable.
     */
    private static final String EV_OVERRIDES_CONFIG_PATH = "AA_OVERRIDES_CONFIG_PATH";

    private String appKey;

    public TypesafeConfigLoader(String appKey) {
        notNull(appKey, "appKey can't be null");
        this.appKey = appKey;
    }

    public Config loadBaseConfig() {
        log.info("Loading base configuration: appKey={}", appKey);
        val baseAppConfigs = ConfigFactory.load(BASE_APP_CONFIG_PATH);
        val defaultAppConfig = baseAppConfigs.getConfig(CK_KSTREAM_APP_DEFAULT_CONFIG);
        return baseAppConfigs.getConfig(appKey).withFallback(defaultAppConfig);
    }

    public Config loadOverridesConfig() {
        String overridesPath = System.getenv(EV_OVERRIDES_CONFIG_PATH);
        if (overridesPath == null) {
            overridesPath = "/config/" + appKey + ".conf";
        }

        val overridesFile = new File(overridesPath);
        if (!overridesFile.exists()) {
            log.info("No overrides configuration found: appKey={}, overridesFile={}", appKey, overridesFile);
            return null;
        }

        log.info("Loading overrides configuration: appKey={}, overridesFile={}", appKey, overridesFile);
        return ConfigFactory.parseFile(overridesFile).getConfig(appKey);
    }

    public Config loadMergedConfig() {
        val baseConfigLoader = new TypesafeConfigLoader(appKey);
        val baseConfig = baseConfigLoader.loadBaseConfig();
        val overridesConfig = baseConfigLoader.loadOverridesConfig();
        return overridesConfig == null ? baseConfig : overridesConfig.withFallback(baseConfig);
    }
}
