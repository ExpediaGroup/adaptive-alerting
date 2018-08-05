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
package com.expedia.adaptivealerting.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Data frame for time series metric data.
 *
 * @author Willie Wheeler
 */
public class MetricFrame {
    private final List<Mpoint> mpoints;
    
    public MetricFrame() {
        this.mpoints = new ArrayList<>();
    }
    
    /**
     * Creates a new metric frame from an array of {@link Mpoint}s.
     *
     * @param mpoints Metric point array.
     */
    public MetricFrame(Mpoint[] mpoints) {
        notNull(mpoints, "mpoints can't be null");
        this.mpoints = Arrays.asList(mpoints);
    }
    
    public MetricFrame(List<Mpoint> mpoints) {
        notNull(mpoints, "mpoints can't be null");
        this.mpoints = mpoints;
    }
    
    /**
     * Returns the number of metric points in the frame.
     *
     * @return Number of metric points in the frame.
     */
    public int getNumRows() {
        return mpoints.size();
    }
    
    /**
     * Returns the {@link Mpoint} at the given row index.
     *
     * @param index Row index.
     * @return The corresponding metric point.
     */
    public Mpoint getMetricPoint(int index) {
        isTrue(index >= 0, "Required: index >= 0");
        return mpoints.get(index);
    }
    
    public List<Mpoint> getMetricPoints() {
        return mpoints;
    }
    
    /**
     * Returns a list iterator for this frame. Intended to support data streaming.
     *
     * @return List iterator for this frame.
     */
    public ListIterator<Mpoint> listIterator() {
        return mpoints.listIterator();
    }
}
