package com.expedia.adaptivealerting.kafka.visualizer;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;

@Slf4j
public class ElasticSearchBulkService implements Runnable{

    private static String HOST = "localhost";
    private static int PORT1 = 9200;
    private static int PORT2 = 9201;
    private static String SCHEME = "http";
    private static String INDEX = "anomalies";
    private static String TYPE = "doc";

    private List<AnomalyModel> anomalyModels;

    public ElasticSearchBulkService(List<AnomalyModel> anomalyModels) {
        this.anomalyModels = anomalyModels;
    }

    public void execute() {
        RestHighLevelClient client = restClientBuilder();
        RequestOptions requestOptions = RequestOptions.DEFAULT;
        BulkResponse bulkResponse = null;

        BulkRequest bulkRequest = buildBulkRequest(this.anomalyModels);
        try {
            bulkResponse = client.bulk(bulkRequest, requestOptions);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(),e);
        }
        if (bulkResponse.hasFailures()) {
            log.error(bulkResponse.buildFailureMessage());
        }
    }

    public RestHighLevelClient restClientBuilder() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(HOST, PORT1, SCHEME),
                        new HttpHost(HOST, PORT2, SCHEME)));
        return client;
    }

    public BulkRequest buildBulkRequest(List<AnomalyModel> anomalyModels){
        BulkRequest bulkRequest = new BulkRequest();
        for (AnomalyModel anomalyModel : anomalyModels) {
            String json = Utility.convertToJson(anomalyModel);
            bulkRequest.add(buildIndexRequest(json));
        }
        return bulkRequest;
    }
    public IndexRequest buildIndexRequest(String json) {
        IndexRequest request = new IndexRequest(INDEX, TYPE);
        request.source(json, XContentType.JSON);
        return request;
    }

    public void run(){
        execute();
    }

}
