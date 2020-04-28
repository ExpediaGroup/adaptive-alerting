/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.kafka.visualizer;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
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
        anomalyModel = AnomalyModel.newBuilder(anomalyModel).build();
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
