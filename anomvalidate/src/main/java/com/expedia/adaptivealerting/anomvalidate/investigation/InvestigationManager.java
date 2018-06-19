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
package com.expedia.adaptivealerting.anomvalidate.investigation;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.InvestigationResult;

import java.util.List;
import java.util.Vector;

public class InvestigationManager {
    private InvestigatorLookupService investigatorLookupService;

    public InvestigationManager(InvestigatorLookupService investigatorLookupService) {
        this.investigatorLookupService = investigatorLookupService;
    }
    public AnomalyResult investigate(AnomalyResult anomalyResult) {
        List<AnomalyInvestigator> investigators = investigatorLookupService.getInvestigators(anomalyResult.getMetric());
        List<InvestigationResult> results = new Vector<>();
        for (AnomalyInvestigator investigator : investigators) { // TODO: should be able to operate concurrently
            results.addAll(investigator.investigate(anomalyResult));
        }
        anomalyResult.setInvestigationResults(results);
        return anomalyResult;
    }
}
