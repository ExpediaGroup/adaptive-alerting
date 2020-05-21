package com.expedia.adaptivealerting.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.expedia.adaptivealerting.kafka.detectorrunner")
@SpringBootApplication
@Slf4j
public class DetectorRunner {

    public static void main(String[] args) {
        SpringApplication.run(DetectorRunner.class, args);
    }
}