package com.expedia.adaptivealerting.modelservice.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class MetricModelMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private Model model;
}
