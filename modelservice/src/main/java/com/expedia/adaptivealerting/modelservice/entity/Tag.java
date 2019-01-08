package com.expedia.adaptivealerting.modelservice.entity;

import com.expedia.adaptivealerting.modelservice.util.JpaConverterJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

/**
 * Tag Entity.
 *
 * @author tbahl
 */


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ukey")
    private String ukey;

    @Column(name = "uvalue")
    private String uvalue;


}

