package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.kafka.visualizer.AnomalyConsumer;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class AnomalyVisualizer {

    @Generated
    public static void main(String[] args) {
        new AnomalyConsumer().listen();
    }
}


