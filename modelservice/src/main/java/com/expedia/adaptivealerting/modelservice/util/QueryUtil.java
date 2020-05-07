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

import com.expedia.adaptivealerting.modelservice.model.mapping.Expression;
import com.expedia.adaptivealerting.modelservice.model.mapping.Field;
import com.expedia.adaptivealerting.modelservice.model.mapping.Operand;
import com.expedia.adaptivealerting.modelservice.model.mapping.Operator;
import com.expedia.adaptivealerting.modelservice.model.percolator.BoolCondition;
import com.expedia.adaptivealerting.modelservice.model.percolator.MustCondition;
import com.expedia.adaptivealerting.modelservice.model.percolator.Query;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class QueryUtil {

    public static Expression buildExpression(Query query) {
        Expression expression = new Expression();
        //TODO - derive operator from query. for now hardcoding to AND as this is the only operator supported now.
        expression.setOperator(Operator.AND);
        List<Operand> operands = query.getBool().getMust().stream()
                .map(mustCondition -> {
                    Operand operand = new Operand();
                    Field field = mustCondition.getMatch().entrySet().stream()
                            .map(match -> new Field(match.getKey(), match.getValue()))
                            .collect(Collectors.toList()).get(0);
                    operand.setField(field);
                    return operand;
                }).collect(Collectors.toList());
        expression.setOperands(operands);
        return expression;
    }

    public static Query buildQuery(Expression expression) {
        List<MustCondition> mustConditions = expression.getOperands().stream()
                .map(operand -> {
                    Map<String, String> condition = new HashMap<>();
                    condition.put(operand.getField().getKey(), operand.getField().getValue());
                    return new MustCondition(condition);
                })
                .collect(Collectors.toList());
        BoolCondition boolCondition = new BoolCondition(mustConditions);
        return new Query(boolCondition);
    }
}
