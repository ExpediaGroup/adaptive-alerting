package com.expedia.adaptivealerting.modelservice.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class MetricDetectorMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @ManyToOne
    @JoinColumn(name = "detector_id")
    private Detector detector;

    public static class AppInfo {
    }
}
