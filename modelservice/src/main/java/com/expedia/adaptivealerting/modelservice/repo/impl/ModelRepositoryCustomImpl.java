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
package com.expedia.adaptivealerting.modelservice.repo.impl;

import com.expedia.adaptivealerting.modelservice.dto.ModelDto;
import com.expedia.adaptivealerting.modelservice.entity.JpaConverterJson;
import com.expedia.adaptivealerting.modelservice.repo.ModelRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * @author kashah
 */

@Service
public class ModelRepositoryCustomImpl implements ModelRepositoryCustom {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JpaConverterJson convertor;

    // @formatter:off
    private static final String MODELS_SQL =
            "select "
                    + "  `model_uuid` as modelUUID, "
                    + "  `hyperparams` as hyperParams, "
                    + "  `thresholds` as thresholds, "
                    + "  `to_rebuild` as toRebuild, "
                    + "  `last_build_ts` as buildTimestamp "
                    + "  from "
                    + "    `metric_model` as data "
                    + "  join model on data.`model_id` = model.id"
                    + "  where "
                    + "    data.`metric_id` = (select id from metric as m where m.metric_key = :metricKey) ";


    private static final String MODEL_ID_SQL =
            "select model.id "
                    + "from `metric_model`"
                    + " join model on model_id = model.id"
                    + " where `model_uuid` = :modelUUID and metric_id = (select id"
                    + " from metric"
                    +  " where `metric_key` = :metricKey) ";

    // @formatter:on

    @Override
    public List<ModelDto> findModels(String metricKey) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("metricKey", metricKey);

        RowMapper<ModelDto> mapper = (rs, rowNum) -> {
            String modelUUID = rs.getString("modelUUID");
            boolean toRebuild = rs.getBoolean("toRebuild");
            Instant buildTimestamp = rs.getTimestamp("buildTimestamp").toInstant();
            Object hyperParams = convertor.convertToEntityAttribute(rs.getString("hyperParams"));
            Object thresholds = convertor.convertToEntityAttribute(rs.getString("thresholds"));
            return new ModelDto(modelUUID, hyperParams, thresholds, toRebuild, buildTimestamp);
        };

        return namedParameterJdbcTemplate.query(MODELS_SQL, params, mapper);
    }

    @Override
    public Integer getModelID(String metricKey, String modelUUID) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("metricKey", metricKey);
        params.addValue("modelUUID", modelUUID);
        return namedParameterJdbcTemplate.queryForObject(MODEL_ID_SQL, params, (resultSet, i) -> resultSet.getInt("id"));
    }
}
