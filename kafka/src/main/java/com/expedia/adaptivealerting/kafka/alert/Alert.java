package com.expedia.adaptivealerting.kafka.alert;

import lombok.Data;

import java.util.Map;

/**
 * @deprecated Remove alerting from AA.
 */
@Data
public class Alert {
    private String name;
    private Map<String, String> labels;
    private Map<String, String> annotations;

    /**
     * time in epoch seconds.
     */
    private long creationTime;

    private String generatorURL;
}
