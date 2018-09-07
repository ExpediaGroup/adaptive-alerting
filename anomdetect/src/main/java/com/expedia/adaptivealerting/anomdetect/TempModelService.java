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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.data.Metric;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Fake model service with hardcoded metric-to-model mappings.
 *
 * @author Willie Wheeler
 * @deprecated Use real model service when it's ready
 */
@Slf4j
public final class TempModelService {
    private TempMetricKeyFactory metricKeyFactory = new TempMetricKeyFactory();
    private Map<String, Set<AnomalyDetectorMeta>> modelMap = new HashMap<>();
    
    public TempModelService() {
        // https://doppler/dashboard/Fptv-WQBGGaD_9knL-qa
        map("haystack-ppv4-duration-tp99-1m", "5159c1b8-94ca-424f-b25c-e9f5bcb2fc51", "ewma-detector");
    
        map("interval=5m,lob=all,mtype=count,site=expedia-com,unit=,what=bookings", "bf3dcb98-1a67-4236-b820-fcfe324475cf", "aquila-detector");
        
        map("interval=5m,lob=hotels,mtype=count,site=expedia-com,unit=,what=bookings", "636e13ed-6882-48cc-be75-56986a3b0179", "aquila-detector");
        map("interval=5m,lob=hotels,mtype=count,site=expedia-com,unit=,what=bookings", "fac1a330-e5ad-4902-b17a-3d6068596a95", "rcf-detector");
        /*

5eb2fb45-6f19-46c0-837c-971b8eb82a95
4fd02447-e469-43a7-a9da-7ec2edf49e35
6202d660-d6b2-4eb6-9cf3-aca28c3fab83
a033706a-051c-4abf-9473-d8a8fbbf2f7f
415215e7-fd04-4899-aafb-9f5f9d4d6e12
669e5c6c-c7dd-4609-9c50-ac5e1316a32a
3f0e89fb-f19f-4f87-b908-f990bec25bc7
f6e9ebe2-8c85-4f26-ac71-00783f28f4bd
c30d01c8-501f-47c9-97b4-d03ff78beb41
966c8947-7371-4e9b-8872-e57be6c90609
cbdcf136-773f-4729-8ccc-339ee3e06f23
3b2aad3d-13c1-410c-93ab-93c7ec99f8cf
4f4e07f9-e681-4c97-89fc-d6e76df04d56
3a303521-e594-4e9d-8276-99eeb265903b
7a0d67ea-f92d-4bc7-bf58-9afc34671fd9
92be65be-3ba8-43d6-8f20-ff38f66a544e
f9e4f5fb-f9ad-4399-8b6c-b649efb62f4a
56926aa6-2a67-4cb8-92d0-a91d714b7da4
32cd9849-5d58-4a78-a92d-d903ca4f7de4
93ae36de-6786-4a47-9043-d0a2da9b4690
58ca2fd0-caab-4ed8-bc47-305bf1b64c75
627d35a5-5006-414f-91c6-00ce343176f6
33c13405-3b78-4578-81f7-0b741f28e113
7fe8adc1-12d6-4975-862c-7a4fe2a6bd48
4b508d41-d2d2-4da4-99bb-69f794beaa35
f260db19-de78-471d-b7e0-11990b3d0379
754254d8-bd5d-4579-ab11-41030f5c2cc6
3c72d256-3b89-4cca-9abb-89ccfe6049e2
1810d395-3c35-4bd0-a262-454570ae4757
eec24ae2-a315-4bf2-8ab8-3e31f7701594
6a26191e-4958-4098-b5b5-a3281fcf6fe7
16cd16f8-22cc-4dbe-b6f1-3cb7a13d6e3e
2d608a7e-b4e9-4520-90b6-0e61298b21fd
26df3e7d-50ad-4192-bb7b-f500136b4d8a
33d3484a-79e3-409b-b07a-79e50dbbcbcd
f4625747-d55c-4ffb-81b2-8a166fecd377
b3c31436-e5a4-4306-b834-6de5af0d20ad
d3e8b894-f02c-4edf-a948-5cb2a7dacb83
941643d2-f605-4eb9-86b6-5e0a14ed3688
d661b585-b68a-4e7f-82b3-6723fe32fbe6
a29435d3-00bc-4940-840a-fa69d4770e0a
3e1cf50c-af29-4583-b68b-b52980fa3fdf
9f2c41df-322b-47d8-98ba-34a2c8fae674
7ac61522-c78e-45e2-b8c9-8e8761293457
ce357b73-61a1-4477-b73a-58a9ee233af9
0b1f4cb8-ebd7-4d38-8d80-d66c8764d58a
9c499be0-3959-4563-ae70-5a5c1ca5ae37
82153ece-d8f0-4557-b2bf-79ae2f36d71c
d3a21625-61d4-4250-8dd8-48c968cab5c6
         */
    }
    
    public Set<AnomalyDetectorMeta> findDetectors(Metric metric) {
        notNull(metric, "metric can't be null");
        final String metricKey = toMetricKey(metric);
        Set<AnomalyDetectorMeta> metas = modelMap.get(metricKey);
        if (metas == null) {
            metas = new HashSet<>();
        }
        log.info("Mapping: metricKey={}, tags.hashCode={}, tags={}",
                metricKey,
                metric.getTags().hashCode(),
                metric.getTags(),
                metas.size());
        return metas;
    }
    
    private String toMetricKey(Metric metric) {
        final Map<String, String> tags = metric.getTags();
        // Handle this one hardcoded example here since I don't know what its tags are. [WLW]
        if (tags.hashCode() == -602511874) {
            // https://doppler/dashboard/Fptv-WQBGGaD_9knL-qa
            return "haystack-airboss-pv3-duration-5m-mean";
        } else {
            return metricKeyFactory.toKey(metric);
        }
    }
    
    private void map(String metricKey, String modelUuid, String modelType) {
        Set<AnomalyDetectorMeta> metas = modelMap.get(metricKey);
        if (metas == null) {
            metas = new HashSet<>();
            modelMap.put(metricKey, metas);
        }
        metas.add(new AnomalyDetectorMeta(UUID.fromString(modelUuid), modelType));
    }
}
