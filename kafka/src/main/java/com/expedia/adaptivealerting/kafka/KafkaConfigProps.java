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
    public static final String ANOMALY_DETECTOR_MAPPER = "anomaly-detector-mapper";
    
    /**
     * Metric Router KStreams app name.
     *
     * @deprecated MetricRouter is going away in favor of AnomalyDetectorMapper
     */
    @Deprecated
    public static final String METRIC_ROUTER = "metric-router";
    
    /**
     * Anomaly Detector Manager KStreams app name.
     */
    public static final String ANOMALY_DETECTOR_MANAGER = "anomaly-detector-manager";
    
    /**
     * Anomaly Validator KStreams app name.
     */
    public static final String ANOMALY_VALIDATOR = "anomaly-validator";
    
    
    // ================================================================================
    // App config properties
    // ================================================================================
    
    /**
     * Factories property name.
     */
    public static final String FACTORIES = "factories";
    
    /**
     * Streams property name.
     */
    public static final String STREAMS = "streams";
    
    /**
     * Health status path property name.
     */
    public static final String HEALTH_STATUS_PATH = "health.status.path";
    
    /**
     * Inbound topic property name.
     */
    public static final String INBOUND_TOPIC = "inbound-topic";
    
    /**
     * Outbound topic property name.
     */
    public static final String OUTBOUND_TOPIC = "outbound-topic";
    
    /**
     * JSON POJO class property name.
     */
    public static final String JSON_POJO_CLASS = "JsonPojoClass";
}
