package com.expedia.adaptivealerting.modelservice.graphite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphiteResult {
    private String[][] datapoints;
    private String target;
    private Tags tags;
}
