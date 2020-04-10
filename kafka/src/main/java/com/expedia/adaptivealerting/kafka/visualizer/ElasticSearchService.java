package com.expedia.adaptivealerting.kafka.visualizer;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ElasticSearchService {

    private static String HOST = "localhost";
    private static int PORT = 9200;
    private static String SCHEME = "http";
    private static String METHOD = "POST";
    private static String END_POINT = "/anomalies/doc";

    public Response execute(String json) {
        RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT, SCHEME));
        RestClient restClient = builder.build();
        Response response = null;
        Request request = new Request(METHOD, END_POINT);

        request.setEntity(new NStringEntity(json, ContentType.APPLICATION_JSON));
        try {
            response = restClient.performRequest(request);
        }
        catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        log.info("response " + response.getStatusLine());
        return response;
    }
}
