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
package com.expedia.adaptivealerting.anomdetect.rcf;

import com.expedia.metrics.MetricData;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A LIFO structure to hold up to maxSize metric points. This structure is representing shingles used with the
 * Sagemaker's Random Cut Forest algorithm.
 *
 * @author Tatjana Kamenov
 */
public class Shingle {
    
    private static final int DEFAULT_SIZE = 10;
    
    private final LinkedList<MetricData> fifo;
    private final int maxSize;
    
    /**
     * Creates a shingles queue having DEFAULT_SIZE for size.
     */
    public Shingle() {
        this(DEFAULT_SIZE);
    }
    
    /**
     * Creates a shingles queue.
     *
     * @param maxSize the maximum size of the queue (shingle size)
     */
    public Shingle(int maxSize) {
        this.fifo = new LinkedList<>();
        this.maxSize = maxSize;
    }
    
    /**
     * Puts a new MetricPoint to the FIFO list. If list is full, it first removes the least recent Metric point.
     *
     * @param metricData metric point
     */
    public void offer(MetricData metricData) {
        if (isReady()) {
            this.fifo.remove();
        }
        this.fifo.add(metricData);
    }
    
    /**
     * Converts shingle's metric points values to an array of double values.
     *
     * @return An array of doubles with the size of maxSize containing metric point values. The array is empty if
     * metric point queue size is less than shingle's maxSize.
     */
    public Optional<double[]> toValues() {
        if (!isReady()) {
            return Optional.empty();
        } else {
            return Optional.of(this.fifo
                    .stream()
                    .map(mp -> mp.getValue())   // get values only from MetricPoint
                    .mapToDouble(f -> f != null ? f : Float.NaN) // convert list to array of doubles
                    .toArray());
        }
    }
    
    /**
     * Converts shingle's metric points values to CSV format i.e. "1.0,2.0,3.0,4.0"
     *
     * @return Optional containing CSV value. The result is empty if queue size is less than shingle size.
     */
    public Optional<String> toCsv() {
        if (!isReady()) {
            return Optional.empty();
        } else {
            return Optional.of(this.fifo
                    .stream()
                    .map(mp -> Double.toString(mp.getValue())) // get values from MetricPoints
                    .collect(Collectors.joining(",")));
        }
    }
    
    /**
     * Shingle size sent to Random Cut Forest (RCF) algorithm is constant and has the size of maxSize. If queue size
     * is less than shingle size, the queue is not ready yet to be passed to Sagemaker RCF algorithm.
     *
     * @return true if shingle is full sized and is ready to be sent to Sagemaker inference RCF endpoint
     */
    public boolean isReady() {
        return fifo.size() == this.maxSize;
    }
}

