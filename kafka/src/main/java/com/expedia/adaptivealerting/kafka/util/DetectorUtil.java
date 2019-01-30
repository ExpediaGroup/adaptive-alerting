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
package com.expedia.adaptivealerting.kafka.util;

import com.expedia.adaptivealerting.anomdetect.source.BasicDetectorSource;
import com.expedia.adaptivealerting.anomdetect.source.DefaultDetectorSource;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.source.MultiDetectorSource;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.typesafe.config.Config;
import lombok.val;

import java.util.Arrays;

public final class DetectorUtil {
    private static final String CK_MODEL_SERVICE_URI_TEMPLATE = "model-service-uri-template";
    
    public static DetectorSource buildDetectorSource(Config config) {
        
        // TODO Make the detector source configurable. [WLW]
        val httpClient = new HttpClientWrapper();
        val uriTemplate = config.getString(CK_MODEL_SERVICE_URI_TEMPLATE);
        val connector = new ModelServiceConnector(httpClient, uriTemplate);
        
        // TODO Hm, not sure I want a basic detector fallback for _every_ metric.
        // Probably want to limit this to Haystack metrics for the time being. [WLW]
        val defaultDetectorSource = new DefaultDetectorSource(connector);
        val basicDetectorSource = new BasicDetectorSource();
        val multiDetectorSource = new MultiDetectorSource(Arrays.asList(defaultDetectorSource, basicDetectorSource));
        
        return multiDetectorSource;
    }
}
