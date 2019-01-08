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
package com.expedia.aquila.detect.service;

import com.expedia.aquila.core.model.AquilaModel;
import com.expedia.aquila.core.model.AquilaRequest;
import com.expedia.aquila.core.model.AquilaResponse;
import com.expedia.aquila.core.repo.AquilaModelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Service
public class AquilaDetectorService {
    
    @Autowired
    private AquilaModelRepo modelRepo;
    
    public AquilaResponse classify(AquilaRequest request) {
        notNull(request, "request can't be null");
        
        final UUID detectorUuid = UUID.fromString(request.getDetectorUuid());
        final AquilaModel aquilaModel = modelRepo.load(detectorUuid);
        return aquilaModel.classify(request);
    }
}
