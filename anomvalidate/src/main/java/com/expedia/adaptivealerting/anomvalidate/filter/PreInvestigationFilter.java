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
package com.expedia.adaptivealerting.anomvalidate.filter;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;

import java.util.Arrays;
import java.util.List;

public class PreInvestigationFilter implements InvestigationFilter {
    private static final List<AnomalyLevel> KEEP_LEVELS = Arrays.asList(AnomalyLevel.WEAK, AnomalyLevel.STRONG);

    // TODO: make configurable
    public boolean keep(AnomalyResult anomalyResult) {
        return anomalyResult != null && KEEP_LEVELS.contains(anomalyResult.getAnomalyLevel());
    }
}
