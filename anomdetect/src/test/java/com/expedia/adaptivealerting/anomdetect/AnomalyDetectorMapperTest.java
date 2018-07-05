/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.Mpoint;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Willie Wheeler
 */
public final class AnomalyDetectorMapperTest {
    
    // Class under test
    private AnomalyDetectorMapper mapper;
    
    // Test objects
    private Mpoint mpointWithDetectors;
    private Mpoint mpointWithoutDetectors;
    
    @Before
    public void setUp() {
        this.mapper = new AnomalyDetectorMapper();
        
        // TODO For now, this is known to have detectors.
        // We'll need to update this once we un-hardcode the AnomalyDetectorMapper.
        final Metric metricWithDetectors = new Metric();
        metricWithDetectors.putTag("what", "bookings");
        this.mpointWithDetectors = new Mpoint();
        mpointWithDetectors.setMetric(metricWithDetectors);
        
        // TODO For now, this is known to have no detectors. See above.
        final Metric metricWithoutDetectors = new Metric();
        this.mpointWithoutDetectors = new Mpoint();
        mpointWithoutDetectors.setMetric(metricWithoutDetectors);
    }
    
    @Test
    public void testMap_mpointWithDetectors() {
        final Set<MappedMpoint> results = mapper.map(mpointWithDetectors);
        assertFalse(results.isEmpty());
    }
    
    @Test
    public void testMap_mpointWithoutDetectors() {
        final Set<MappedMpoint> results = mapper.map(mpointWithoutDetectors);
        assertTrue(results.isEmpty());
    }
}
