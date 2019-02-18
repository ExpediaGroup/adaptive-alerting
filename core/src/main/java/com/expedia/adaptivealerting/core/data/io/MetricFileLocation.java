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
package com.expedia.adaptivealerting.core.data.io;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public final class MetricFileLocation {
    private String metricDir;
    private String datePattern;
    private String fileExtension;
    private DateTimeFormatter dateTimeFormatter;
    
    public MetricFileLocation(String metricDir, String datePattern, String fileExtension) {
        this.metricDir = metricDir;
        this.datePattern = datePattern;
        this.fileExtension = fileExtension;
        
        this.dateTimeFormatter = DateTimeFormatter
                .ofPattern(datePattern)
                .withZone(ZoneOffset.UTC);
    }
    
    public String getMetricDir() {
        return metricDir;
    }
    
    public String getDatePattern() {
        return datePattern;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public String toMetricFilePath(Instant date) {
        notNull(date, "date can't be null");
        
        StringBuilder builder = new StringBuilder(metricDir)
                .append("/")
                .append(dateTimeFormatter.format(date));
        
        if (fileExtension != null) {
            builder
                    .append(".")
                    .append(fileExtension);
        }
        
        return builder.toString();
    }
}
