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
package com.expedia.adaptivealerting.metrics.functions.source;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;

public class GraphiteQueryResult {

    /* graphite query result json format:
     *  [ {"datapoints": [ [value1, timestamp1], [value2, timestamp2] ..],
     *  "target": "time-series-key-identier", "tags" : "<tags from graphite function used in the render api>"} ]
     */

    private final String GRAPHITE_RESULT_DATAPOINTS_KEY = "datapoints";
    private final String GRAPHITE_RESULT_TARGET_KEY = "target";
    private final String GRAPHITE_RESULT_TAGS_KEY = "tags";

    @Getter
    private Datapoint Datapoint;

    @Getter
    private String Target;

    @Getter
    private HashMap<String, String> Tags;

    public GraphiteQueryResult() {
        Datapoint = new Datapoint(0.0, 0);
        Target = "";
        Tags = new HashMap<>();
    }

    public void getGraphiteQueryResultFromJson(String jsonGraphiteOutput) {
        JSONTokener graphiteQuery = new JSONTokener(jsonGraphiteOutput);
        JSONArray graphiteResultJsonArray = new JSONArray(graphiteQuery);
        JSONObject graphiteResultJsonObject = graphiteResultJsonArray.getJSONObject(0);
        String[] datapointString = graphiteResultJsonObject.getJSONArray(GRAPHITE_RESULT_DATAPOINTS_KEY).get(0).toString().split(",");
        this.Datapoint = new Datapoint(Double.parseDouble(datapointString[0].substring(1)), Long.parseLong(datapointString[1].substring(0, datapointString[1].length() - 1 )));
        this.Target = graphiteResultJsonObject.getString(GRAPHITE_RESULT_TARGET_KEY);
        setTagsFromString(graphiteResultJsonObject.get(GRAPHITE_RESULT_TAGS_KEY).toString());
    }

    private void setTagsFromString(String tags) {
        JSONObject tagsJSONObject = new JSONObject(tags);
        Iterator<?> keys = tagsJSONObject.keys();

        while(keys.hasNext()) {
            String key = keys.next().toString();
            String value = tagsJSONObject.getString(key);
            this.Tags.put(key, value);
        }
    }
}