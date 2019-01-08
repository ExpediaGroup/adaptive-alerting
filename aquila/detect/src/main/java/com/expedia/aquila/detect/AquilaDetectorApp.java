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
package com.expedia.aquila.detect;

import com.expedia.aquila.core.repo.AquilaModelRepo;
import com.expedia.aquila.core.repo.s3.S3AquilaModelRepo;
import com.expedia.aquila.detect.service.AquilaDetectorService;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Willie Wheeler
 */
@SpringBootApplication
@Slf4j
public class AquilaDetectorApp {
    
    public static void main(String[] args) {
        log.info("Starting AquilaDetectorApp");
        SpringApplication.run(AquilaDetectorApp.class, args);
    }
    
    @Bean
    public Config connectorsConfig() {
        return ConfigFactory.load("connectors.conf");
    }
    
    @Bean
    public AquilaModelRepo modelConnector() {
        final Config modelsConfig = connectorsConfig().getConfig("models");
        final AquilaModelRepo connector = new S3AquilaModelRepo();
        connector.init(modelsConfig);
        return connector;
    }
    
    @Bean
    public AquilaDetectorService aquilaDetectorService() {
        return new AquilaDetectorService();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new MetricsJavaModule());
        return objectMapper;
    }
}
