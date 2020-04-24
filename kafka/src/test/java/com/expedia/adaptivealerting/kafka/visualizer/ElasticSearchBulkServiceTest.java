package com.expedia.adaptivealerting.kafka.visualizer;

import com.typesafe.config.Config;
import org.apache.http.Header;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElasticSearchBulkServiceTest {

    private ElasticSearchClient client;

    @Before
    public void setUp() {
        client = mock(ElasticSearchClient.class);
    }

    @Test
    public void testRun() throws IOException {
        AnomalyModel anomalyModel = AnomalyModel.newBuilder()
                .key("key")
                .value(100)
                .level("NORMAL")
                .uuid("test")
                .timestamp("date")
                .anomalyThresholds(null)
                .tags(null)
                .build();
        List<AnomalyModel> anomalyModels = new ArrayList<>();
        anomalyModels.add(anomalyModel);
        BulkResponse bulkResponse = buildBulkResponseHappy();

        when(client.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkResponse);
        when(client.close()).thenReturn(true);
        ElasticSearchBulkService elasticSearchBulkService = new ElasticSearchBulkService(anomalyModels);
        elasticSearchBulkService.setElasticSearchClient(client);
        elasticSearchBulkService.run();
        verify(elasticSearchBulkService.getElasticSearchClient(), times(1))
                .bulk(any(BulkRequest.class), any(RequestOptions.class));

    }

    @Test
    public void testRunError() throws IOException {
        AnomalyModel anomalyModel = AnomalyModel.newBuilder()
                .key("key")
                .value(100)
                .level("NORMAL")
                .uuid("test")
                .timestamp("date")
                .anomalyThresholds(null)
                .tags(null)
                .build();
        List<AnomalyModel> anomalyModels = new ArrayList<>();
        anomalyModels.add(anomalyModel);
        BulkResponse bulkResponse = buildBulkResponseError();

        when(client.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkResponse);
        when(client.close()).thenReturn(true);
        ElasticSearchBulkService elasticSearchBulkService = new ElasticSearchBulkService(anomalyModels);
        elasticSearchBulkService.setElasticSearchClient(client);
        elasticSearchBulkService.run();
        verify(elasticSearchBulkService.getElasticSearchClient(), times(1))
                .bulk(any(BulkRequest.class), any(RequestOptions.class));
        verify(bulkResponse,times(1)).buildFailureMessage();
    }

    public BulkResponse buildBulkResponseHappy() {

        BulkResponse bulkResponse = mock(BulkResponse.class);
        when(bulkResponse.hasFailures()).thenReturn(false);
        return bulkResponse;
    }

    public BulkResponse buildBulkResponseError() {

        BulkResponse bulkResponse = mock(BulkResponse.class);
        when(bulkResponse.hasFailures()).thenReturn(true);
        return bulkResponse;
    }

}
