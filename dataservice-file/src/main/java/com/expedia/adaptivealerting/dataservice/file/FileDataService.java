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
package com.expedia.adaptivealerting.dataservice.file;

import com.expedia.adaptivealerting.dataservice.AbstractDataService;
import com.expedia.adaptivealerting.dataservice.DataService;
import com.expedia.adaptivealerting.core.data.io.MetricFileInfo;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * File-based {@link DataService} implementation.
 *
 * @author Willie Wheeler
 */
public final class FileDataService extends AbstractDataService {
    private static final Logger log = LoggerFactory.getLogger(FileDataService.class);
    
    private File baseDir;
    
    @Override
    public void init(Config config) {
        super.init(config);
        
        final String baseDirStr = config.getString("baseDir");
        notNull(baseDirStr, "Property baseDir must be defined");
        
        final File baseDir = new File(baseDirStr);
        isTrue(baseDir.isDirectory(), "Property baseDir must point to a directory");
        
        this.baseDir = baseDir;
        
        log.info("Initialized FileDataService with baseDir={}", this.baseDir);
    }
    
    @Override
    protected InputStream toInputStream(MetricFileInfo meta, Instant date) throws IOException {
        return new FileInputStream(toFile(meta, date));
    }
    
    private File toFile(MetricFileInfo meta, Instant date) {
        final String path = meta.getLocation().toMetricFilePath(date);
        final String[] segments = path.split("/");
        
        File file = baseDir;
        for (String segment : segments) {
            file = new File(file, segment);
        }
        return file;
    }
}
