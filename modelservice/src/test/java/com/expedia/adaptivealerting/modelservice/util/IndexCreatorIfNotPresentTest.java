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
package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.mockito.Mockito.when;

public class IndexCreatorIfNotPresentTest {

    @InjectMocks
    private IndexCreatorIfNotPresent creatorUnderTest;

    @Mock
    private ElasticSearchProperties esProperties;

    @Mock
    private ElasticSearchClient esClient;

    // TODO Can't mock this because it's final.
    //  Might want to make some kind of a DAO wrapper around the Elasticsearch client,
    //  including the IndicesClient. [WLW]
//    @Mock
//    private IndicesClient indicesClient;

    @Mock
    private ApplicationReadyEvent appReadyEvent;

    @Before
    public void setUp() {
        this.creatorUnderTest = new IndexCreatorIfNotPresent();
        MockitoAnnotations.initMocks(this);
        initDependencies();
    }

    @Test
    public void testOnApplicationEvent() {
//        creatorUnderTest.onApplicationEvent(appReadyEvent);
        // TODO
    }

    private void initDependencies() {
        when(esProperties.isCreateIndexIfNotFound()).thenReturn(true);
        when(esProperties.getIndexName()).thenReturn("my-index");

//        when(esClient.indices()).thenReturn(indicesClient);
    }
}
