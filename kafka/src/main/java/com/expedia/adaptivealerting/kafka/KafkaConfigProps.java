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
package com.expedia.adaptivealerting.kafka;

/**
 * Contains standard app names and config properties. "Standard" here means that all (or at least most) Kafka-based AA
 * deployments use them. Properties that appear in multiple places in the codebase have priority, for DRY purposes.
 *
 * @author Willie Wheeler
 */
public interface KafkaConfigProps {
    
    
    // ================================================================================
    // App names
    // ================================================================================
    
    /**
     * Anomaly Detector Mapper KStreams app name.
     */
    String ANOMALY_DETECTOR_MAPPER = "ad-mapper";
    
    /**
     * Anomaly Detector Manager KStreams app name.
     */
    String ANOMALY_DETECTOR_MANAGER = "ad-manager";
    
    /**
     * Anomaly Validator KStreams app name.
     */
    String ANOMALY_VALIDATOR = "anomaly-validator";
    
    
    // ================================================================================
    // App config properties
    // ================================================================================
    
    /**
     * Factories property name.
     */
    String FACTORIES = "factories";
    
    /**
     * Streams property name.
     */
    String STREAMS = "streams";
    
    /**
     * Health status path property name.
     */
    String HEALTH_STATUS_PATH = "health.status.path";
    
    /**
     * Inbound topic property name.
     */
    String INBOUND_TOPIC = "inbound-topic";
    
    /**
     * Outbound topic property name.
     */
    String OUTBOUND_TOPIC = "outbound-topic";
    
    /**
     * Model service URI template property name.
     */
    String MODEL_SERVICE_URI_TEMPLATE = "model-service-uri-template";
}
