package com.expedia.adaptivealerting.kafka.visualizer;

import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElasticSearchDataCleanerTest {

    private ElasticSearchClient client;

    @Before
    public void setUp() { client = mock(ElasticSearchClient.class);}

    @Test
    public void testDelete() throws Exception {
        BulkByScrollResponse expected = buildBulkResponse();
        when(client.deleteByQuery(any(DeleteByQueryRequest.class), any()))
                .thenReturn(expected);
        ElasticSearchDataCleaner elasticSearchDataCleaner = new ElasticSearchDataCleaner();
        BulkByScrollResponse actual = elasticSearchDataCleaner.deleteData(client);
        assertEquals(actual,expected);
    }

    @Test
    public void testDeleteIOException() throws Exception {
        when(client.deleteByQuery(any(DeleteByQueryRequest.class), any()))
                .thenThrow(IOException.class);
        ElasticSearchDataCleaner elasticSearchDataCleaner = new ElasticSearchDataCleaner();
        BulkByScrollResponse actual = elasticSearchDataCleaner.deleteData(client);
        assertNull(actual);
    }

    @Test
    public void testDeleteException() throws Exception {
        when(client.deleteByQuery(any(DeleteByQueryRequest.class), any()))
                .thenThrow(Exception.class);
        ElasticSearchDataCleaner elasticSearchDataCleaner = new ElasticSearchDataCleaner();
        BulkByScrollResponse actual = elasticSearchDataCleaner.deleteData(client);
        assertNull(actual);
    }

    public BulkByScrollResponse buildBulkResponse(){
        BulkByScrollResponse bulkByScrollResponse = mock(BulkByScrollResponse.class);
        return bulkByScrollResponse;
    }
}
