package com.expedia.adaptivealerting.modelservice.graphite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphiteRequest {

    private String target;

    public Map<String, Object> toParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("target", target);
        return params;
    }
}
