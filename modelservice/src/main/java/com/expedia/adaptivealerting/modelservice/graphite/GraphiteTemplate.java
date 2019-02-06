package com.expedia.adaptivealerting.modelservice.graphite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class GraphiteTemplate {

    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private GraphiteProperties props;

    public GraphiteResult[] getMetricData(String target) {
        GraphiteRequest request = new GraphiteRequest(target);
        Map<String, Object> params = request.toParams();
        return restTemplate.getForObject(props.getUrlTemplate(), GraphiteResult[].class, params);
    }
}
