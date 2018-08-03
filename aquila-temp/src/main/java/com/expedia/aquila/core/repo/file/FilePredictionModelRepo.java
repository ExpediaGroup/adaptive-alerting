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
package com.expedia.aquila.core.repo.file;

import com.expedia.aquila.core.model.PredictionModel;
import com.expedia.aquila.core.repo.PredictionModelRepo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * File-based repository for Aquila prediction models.
 *
 * @author Willie Wheeler
 */
public final class FilePredictionModelRepo implements PredictionModelRepo {
    private File baseDir;
    private ObjectMapper mapper;
    
    public FilePredictionModelRepo(File baseDir) {
        notNull(baseDir, "baseDir can't be null");
        isTrue(baseDir.isDirectory(), "baseDir must be a directory");
        this.baseDir = baseDir;
        this.mapper = new ObjectMapper();
    }
    
    public File getBaseDir() {
        return baseDir;
    }
    
    @Override
    public void save(UUID detectorUuid, PredictionModel predModel) {
        notNull(detectorUuid, "detectorUuid can't be null");
        notNull(predModel, "predModel can't be null");
        
        final File file = getPredictionModelFile(detectorUuid);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, predModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public PredictionModel load(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");
        
        final File file = getPredictionModelFile(detectorUuid);
        try {
            return mapper.readValue(file, PredictionModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private File getPredictionModelFile(UUID detectorUuid) {
        return new File(baseDir, "aquila-" + detectorUuid + ".json");
    }
}
