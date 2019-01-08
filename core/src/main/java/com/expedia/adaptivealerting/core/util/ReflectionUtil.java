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

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Reflection utilities.
 *
 * @author Willie Wheeler
 */
public final class ReflectionUtil {
    
    /**
     * Prevent instantiation.
     */
    private ReflectionUtil() {
    }
    
    public static Class classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Object newInstance(String className) {
        notNull(className, "className can't be null");
        return newInstance(classForName(className));
    }
    
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
