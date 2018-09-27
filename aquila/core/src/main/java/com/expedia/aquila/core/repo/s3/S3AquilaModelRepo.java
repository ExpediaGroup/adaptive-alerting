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
package com.expedia.aquila.core.repo.s3;

import com.expedia.aquila.core.repo.AbstractAquilaModelRepo;
import com.expedia.aquila.core.repo.PredictionModelRepo;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Slf4j
public class S3AquilaModelRepo extends AbstractAquilaModelRepo {
    
    private S3PredictionModelRepo predModelRepo;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        this.predModelRepo = new S3PredictionModelRepo(config);
    }
    
    @Override
    protected PredictionModelRepo getPredictionModelRepo() {
        return predModelRepo;
    }
}
