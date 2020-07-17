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
package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.entity.Detector.Meta;
import com.expedia.adaptivealerting.modelservice.entity.Detector.DetectorConfig;
import com.expedia.adaptivealerting.modelservice.entity.Detector.TrainingMetaData;
import lombok.val;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class DetectorDataUtil {

    public static Meta buildNewDetectorMeta(Detector detector) {
        Meta metaBlock = buildDetectorMeta(detector);
        Date nowDate = DateUtil.now();
        metaBlock.setDateLastUpdated(nowDate);
        metaBlock.setDateLastAccessed(nowDate);
        return metaBlock;
    }

    public static Meta buildLastUpdatedDetectorMeta(Detector detector) {
        Meta metaBlock = buildDetectorMeta(detector);
        Date nowDate = DateUtil.now();
        metaBlock.setDateLastUpdated(nowDate);
        return metaBlock;
    }

    public static Meta buildLastUsedDetectorMeta(Detector detector) {
        Meta metaBlock = buildDetectorMeta(detector);
        Date nowDate = DateUtil.now();
        metaBlock.setDateLastAccessed(nowDate);
        return metaBlock;
    }

    private static Meta buildDetectorMeta(Detector detector) {
        Meta metaBlock = detector.getMeta();
        return (metaBlock == null) ? new Meta() : detector.getMeta();
    }

    public static TrainingMetaData buildDetectorTrainingMeta(Detector detector) {
        TrainingMetaData metaBlock = detector.getDetectorConfig().getTrainingMetaData();
        return (metaBlock == null) ? new TrainingMetaData() : metaBlock;
    }

    public static TrainingMetaData buildUpdatedRuntimeTrainingMeta(Detector detector, Long nextRun) {
        TrainingMetaData trainingMetaDataBlock = buildDetectorTrainingMeta(detector);
        Date nowDate = DateUtil.now();
        trainingMetaDataBlock.setDateTrainingLastRun(nowDate);
        trainingMetaDataBlock.setDateTrainingNextRun(new Date(nextRun));
        return trainingMetaDataBlock;
    }

    public static DetectorConfig buildMergedDetectorConfig(DetectorConfig existingConfig,
                                                           Optional<DetectorConfig> newConfigOptional) {

        val trainingMetaData = newConfigOptional.map(DetectorConfig::getTrainingMetaData)
            .orElse(existingConfig.getTrainingMetaData());
        val hyperParamsMap = newConfigOptional.map(DetectorConfig::getHyperparams)
            .orElse(existingConfig.getHyperparams());
        val paramsMap = newConfigOptional.map(DetectorConfig::getParams)
            .orElse(existingConfig.getParams());

        return DetectorConfig.builder()
            .hyperparams(hyperParamsMap != null ? new HashMap<>(hyperParamsMap) : new HashMap<>())
            .trainingMetaData(trainingMetaData != null ? trainingMetaData.toBuilder().build(): new TrainingMetaData())
            .params(paramsMap != null ? new HashMap<>(paramsMap) : new HashMap<>())
            .build();
    }
}
