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
package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;

@Data
public class GraphiteQueryResult {

    /* graphite query result json format:
     *  [ {"datapoints": [ [value1, timestamp1], [value2, timestamp2] ..],
     *  "target": "time-series-key-identier", "tags" : "<tags from graphite function used in the render api>"} ]
     */

    private final String GRAPHITE_RESULT_DATAPOINTS_KEY = "datapoints";
    private final String GRAPHITE_RESULT_TARGET_KEY = "target";
    private final String GRAPHITE_RESULT_TAGS_KEY = "tags";

    private Datapoint datapoint;

    private String target;

    private HashMap<String, String> tags;

    public GraphiteQueryResult() {
        datapoint = new Datapoint(0.0, 0);
        target = "";
        tags = new HashMap<>();
    }

    public void getGraphiteQueryResultFromJson(String jsonGraphiteOutput) {
        JSONTokener graphiteQuery = new JSONTokener(jsonGraphiteOutput);
        JSONArray graphiteResultJsonArray = new JSONArray(graphiteQuery);
        if (graphiteResultJsonArray.length() > 0) {
            JSONObject graphiteResultJsonObject = graphiteResultJsonArray.getJSONObject(0);

            if (graphiteResultJsonObject.getJSONArray(GRAPHITE_RESULT_DATAPOINTS_KEY).length() > 0) {
                String[] datapointString = graphiteResultJsonObject.getJSONArray(GRAPHITE_RESULT_DATAPOINTS_KEY).get(0).toString().split(",");
                if (datapointString.length > 1) {
                    this.datapoint = new Datapoint(Double.parseDouble(datapointString[0].substring(1)), Long.parseLong(datapointString[1].substring(0, datapointString[1].length() - 1)));
                }
            }

            if (graphiteResultJsonObject.getString(GRAPHITE_RESULT_TARGET_KEY) != null) {
                this.target = graphiteResultJsonObject.getString(GRAPHITE_RESULT_TARGET_KEY);
            }

            if (graphiteResultJsonObject.has(GRAPHITE_RESULT_TAGS_KEY) &&
                    graphiteResultJsonObject.get(GRAPHITE_RESULT_TAGS_KEY) !=null) {
                setTagsFromString(graphiteResultJsonObject.get(GRAPHITE_RESULT_TAGS_KEY).toString());
            }
        }
    }

    public boolean validateNullDatapoint(String jsonGraphiteOutput) {
        /* null datapoint cannot be set to default 0.0 at getGraphiteQueryResultFromJson as
        *  this could change time series evaluation
        */
        JSONTokener graphiteQuery = new JSONTokener(jsonGraphiteOutput);
        JSONArray graphiteResultJsonArray = new JSONArray(graphiteQuery);
        if (graphiteResultJsonArray.length() > 0) {
            JSONObject graphiteResultJsonObject = graphiteResultJsonArray.getJSONObject(0);

            if (graphiteResultJsonObject.getJSONArray(GRAPHITE_RESULT_DATAPOINTS_KEY).length() > 0) {
                String[] datapointString = graphiteResultJsonObject.getJSONArray(GRAPHITE_RESULT_DATAPOINTS_KEY).get(0).toString().split(",");
                if (datapointString.length > 1) {
                    return datapointString[0].substring(1).equals("null");
                }
            }
        }
        return false;
    }

    private void setTagsFromString(String tags) {
        JSONObject tagsJSONObject = new JSONObject(tags);
        Iterator<?> keys = tagsJSONObject.keys();

        while(keys.hasNext()) {
            String key = keys.next().toString();
            String value = tagsJSONObject.getString(key);
            this.tags.put(key, value);
        }
    }
}
