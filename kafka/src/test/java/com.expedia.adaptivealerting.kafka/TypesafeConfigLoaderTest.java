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

import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypesafeConfigLoaderTest {
    private TypesafeConfigLoader loader;
    
    @Before
    public void setUp() {
        this.loader = new TypesafeConfigLoader("ad-mapper");
    }
    
    @Test
    public void testLoadBaseConfig() {
        val config = loader.loadBaseConfig();
        assertEquals("kafkasvc:9092", config.getString("streams.bootstrap.servers"));
    }
}
