#!/usr/bin/env bash

#  Copyright 2018-2019 Expedia Group, Inc.
#
#       Licensed under the Apache License, Version 2.0 (the "License");
#       you may not use this file except in compliance with the License.
#      You may obtain a copy of the License at
#
#           http://www.apache.org/licenses/LICENSE-2.0
#
#       Unless required by applicable law or agreed to in writing, software
#       distributed under the License is distributed on an "AS IS" BASIS,
#       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#       See the License for the specific language governing permissions and
#       limitations under the License.

echo "--- Waiting for the Model Service API to become available before inserting sample consumerDetectorMapping and mapping..."

# Do not post sample consumerDetectorMapping to API until elasticsearch indexes pre-created by the modelservice
# Prevents race condition where index mappings are incorrectly auto-created by the API post before the modelservice can build them
while [[ "$index_status" != *"consumerDetectorMappings"* && "$index_status" != *"detector_mappings"* ]]
do
    index_status=$(curl -s X GET http://elasticsearch:9200/_cat/indices?v)
    sleep 1
done

# Prepare Sample Detector
until detectoruuid=$(curl -s -X POST \
    http://modelservice:8008/api/v2/consumerDetectorMappings \
    -H 'Content-Type: application/json' \
    -d '{
            "createdBy": "sampleuser",
            "type": "constant-consumerDetectorMapping",
            "detectorConfig": {
                "hyperparams": {
                    "strategy": "percentile",
                    "upper_weak_multiplier": "0",
                    "upper_strong_multiplier": "0"
                },
                "trainingMetaData": {},
                "params": {
                    "type": "RIGHT_TAILED",
                    "thresholds": {
                        "upperWeak": 125,
                        "upperStrong": 155
                    }
                }
            },
            "enabled": true,
            "trusted": true
            }')
do
    sleep 1
done

# Prepare Sample Detector Mapping
until mapid=$(curl -s -X POST \
    http://modelservice:8008/api/detectorMappings \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache' \
    -d '{
            "consumerDetectorMapping": {
                "uuid": "'$detectoruuid'"
            },
            "expression": {
            "operator": "AND",
            "operands": [
                {
                "field": {
                    "key": "env",
                    "value": "prod"
                },
                "expression": null
                },
                {
                "field": {
                    "key": "region",
                    "value": "primary"
                },
                "expression": null
                },
                {
                "field": {
                    "key": "m_application",
                    "value": "sample-web"
                },
                "expression": null
                },
                {
                "field": {
                    "key": "what",
                    "value": "samplemetric"
                },
                "expression": null
                }
            ]
            },
            "user": {
                "id": "sampleuser"
            }
        }')
do
    sleep 1
done

echo "--- Created Detector $detectoruuid with Mapping $mapid"
