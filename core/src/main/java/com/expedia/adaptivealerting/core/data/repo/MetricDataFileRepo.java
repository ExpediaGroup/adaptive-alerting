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
package com.expedia.adaptivealerting.core.data.repo;

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * File-based {@link MetricDataRepo} implementation.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class MetricDataFileRepo implements MetricDataRepo {
    private static final Logger log = LoggerFactory.getLogger(MetricDataFileRepo.class);
    
    private File baseDir;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        
        final String baseDirStr = config.getString("base.dir");
        notNull(baseDirStr, "Property base.dir must be defined");
        
        final File baseDir = new File(baseDirStr);
        isTrue(baseDir.isDirectory(), "Property base.dir must point to a directory");
        
        this.baseDir = baseDir;
        log.info("Initialized MetricDataFileRepo with baseDir={}", this.baseDir);
    }
    
    public File getBaseDir() {
        return baseDir;
    }
    
    @Override
    public MetricFrame load(Metric metric, String path) {
        notNull(path, "path can't be null");
        
        final File file = new File(baseDir, path);
        try {
            return MetricFrameLoader.loadCsv(metric, new FileInputStream(file), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
