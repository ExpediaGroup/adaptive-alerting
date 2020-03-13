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
package com.expedia.adaptivealerting.anomdetect.filter.chain;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import lombok.NonNull;

import java.util.List;
import java.util.ListIterator;

public class PostDetectionFilterChain {
    @NonNull
    private ListIterator<PostDetectionFilter> filtersIterator;

    public PostDetectionFilterChain(List<PostDetectionFilter> filters) {
        this.filtersIterator = filters.listIterator();
    }

    /**
     * Calls the next PostDetectionFilter in the chain, or else returns the result directly (if this is the final filter in the chain).
     *
     * @param detectorResult the DetectorResult
     */
    public DetectorResult doFilter(@NonNull DetectorResult detectorResult) {
        if (filtersIterator.hasNext()) {
            PostDetectionFilter f = filtersIterator.next();
            return f.doFilter(detectorResult, this);
        } else {
            return detectorResult;
        }
    }
}
