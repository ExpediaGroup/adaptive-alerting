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
package com.expedia.adaptivealerting.anomdetect.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Calculates a running median.
 */
@Slf4j
public final class RunningMedian {
    private final PriorityQueue<Double> lowerHalf = new PriorityQueue<>(Collections.reverseOrder());
    private final PriorityQueue<Double> upperHalf = new PriorityQueue<>();

    public void add(double value) {

        // Add the value to one or the other heap
        if (upperHalf.isEmpty() || value <= upperHalf.peek()) {
            lowerHalf.add(value);
        } else {
            upperHalf.add(value);
        }

        // If either half gets too big, move an element to the other half
        if (lowerHalf.size() > upperHalf.size() + 1) {
            upperHalf.add(lowerHalf.remove());
        } else if (upperHalf.size() > lowerHalf.size() + 1) {
            lowerHalf.add(upperHalf.remove());
        }
    }

    public double getMedian() {
        if (upperHalf.isEmpty() && lowerHalf.isEmpty()) {
            throw new RuntimeException("Add values before getting the median");
        }

        if (upperHalf.size() > lowerHalf.size()) {
            return upperHalf.peek();
        } else if (lowerHalf.size() > upperHalf.size()) {
            return lowerHalf.peek();
        } else {
            return 0.5 * (lowerHalf.peek() + upperHalf.peek());
        }
    }
}
