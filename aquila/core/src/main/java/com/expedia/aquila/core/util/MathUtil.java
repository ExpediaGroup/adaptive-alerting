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
package com.expedia.aquila.core.util;

/**
 * Math utility functions, isolated here to avoid cluttering up the main code.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class MathUtil {
    
    /**
     * Prevent instantiation.
     */
    private MathUtil() {
    }
    
    public static double[] incrAndLog(double[] data) {
        final int n = data.length;
        final double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = Math.log(data[i] + 1.0);
        }
        return result;
    }
    
    public static double[] exp(double[] data) {
        final int n = data.length;
        final double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = Math.exp(data[i]);
        }
        return result;
    }
    
    public static double[] expAndDecr(double[] data) {
        final int n = data.length;
        final double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = Math.exp(data[i]) - 1.0;
        }
        return result;
    }
    
    /**
     * @param data Data series.
     * @param windowSize WMA window size.
     * @return Weighted moving average.
     */
    public static double[] weightedMovingAverage(double[] data, int windowSize) {
        if (windowSize % 2 == 0) {
            throw new IllegalArgumentException("k must be odd");
        }
        if (windowSize > data.length) {
            throw new IllegalArgumentException("k must be <= n");
        }
        
        final int n = data.length;
        final int halfK = windowSize / 2;
        
        final double[] result = new double[n];
        
        for (int i = 0; i < n; i++) {
            
            // Desired indices
            int lowerIndex = Math.max(0, i - halfK);
            int upperIndex = Math.min(n - 1, i + halfK);
            
            // Shrink upper if needed
            int lowerDiff = i - lowerIndex;
            if (lowerDiff < halfK) {
                upperIndex = Math.min(n - 1, upperIndex + (halfK - lowerDiff));
            }
            
            // Grow lower if needed
            int upperDiff = (n - 1) - upperIndex;
            if (upperDiff < halfK) {
                lowerIndex = Math.max(0, lowerIndex - (halfK - upperDiff));
            }
            
            // Calculate the maximum distance as this determines the weights
            int maxDist = Math.max(i - lowerIndex, upperIndex - i);
            
            // Calculate weighted moving average
            double weightedSum = 0.0;
            int denom = 0;
            for (int j = lowerIndex; j <= upperIndex; j++) {
                int dist = Math.abs(i - j);
                int weight = (maxDist - dist) + 1;
                denom += weight;
                weightedSum += (weight * data[j]);
            }
            result[i] = weightedSum / denom;
        }
        
        return result;
    }
}
