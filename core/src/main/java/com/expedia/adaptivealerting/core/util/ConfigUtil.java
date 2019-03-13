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
package com.expedia.adaptivealerting.core.util;

import com.typesafe.config.Config;

import java.util.Properties;

/**
 * Configuration utilities.
 */
public final class ConfigUtil {

    /**
     * Prevent instantiation.
     */
    private ConfigUtil() {
    }

    /**
     * Maps a configuration instance to a properties instance.
     *
     * @param config Configuration instance.
     * @return Properties instance.
     */
    public static Properties toProperties(Config config) {
        final Properties props = new Properties();
        config.entrySet().forEach((entry) -> {
            props.setProperty(entry.getKey(), entry.getValue().unwrapped().toString());
        });
        return props;
    }
}
