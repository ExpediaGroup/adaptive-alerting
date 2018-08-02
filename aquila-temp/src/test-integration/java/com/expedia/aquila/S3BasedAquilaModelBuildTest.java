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
package com.expedia.aquila;

/**
 * Integration test for file-based Aquila model builds. This includes the following
 *
 * <ul>
 * <li>Load training data from the file system</li>
 * <li>Train an Aquila model</li>
 * <li>Store the model to the file system</li>
 * <li>Load the model into an anomaly detector</li>
 * <li>Load test data from the file system</li>
 * <li>Run the test data through the model</li>
 * </ul>
 *
 * @author Willie Wheeler
 */
public class S3BasedAquilaModelBuildTest extends AbstractAquilaModelBuildTest {
    
    public S3BasedAquilaModelBuildTest() {
        super("application-s3.conf");
    }
}
