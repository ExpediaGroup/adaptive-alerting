package com.expedia.adaptivealerting.modelservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * MetricTagMapping Entity.
 *
 * @author tbahl
 */

@Table(name = "metric_tag_mapper")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricTagMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

}
