package com.expedia.adaptivealerting.modelservice.dto.percolator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PercolatorMetricProfiling {
    // Prefixing variable names with 'aa_' to reserve these fields to be used in ES mappings.
    public static final String AA_PREFIX = "aa_";
    public static final String STATIONARY_KEYWORD = AA_PREFIX + "isStationary";
    public static final String QUERY_KEYWORD = AA_PREFIX + "query";
    public static final String PROFILING_TIME_KEYWORD = AA_PREFIX + "profilingTime";

    @JsonProperty(QUERY_KEYWORD)
    private Query query;
    @JsonProperty(STATIONARY_KEYWORD)
    private Boolean isStationary;
    @JsonProperty(PROFILING_TIME_KEYWORD)
    private long profilingTime;

}
