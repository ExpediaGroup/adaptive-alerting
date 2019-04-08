package com.expedia.adaptivealerting.modelservice.detectormapper.util;

import com.expedia.adaptivealerting.modelservice.detectormapper.model.ExpressionTree;
import com.expedia.adaptivealerting.modelservice.detectormapper.model.Field;
import com.expedia.adaptivealerting.modelservice.detectormapper.model.Operand;
import com.expedia.adaptivealerting.modelservice.detectormapper.model.Operator;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.es.BoolCondition;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.es.MustCondition;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.es.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryUtil {

    private QueryUtil() {}

    public static ExpressionTree buildExpressionTree(Query query) {
        ExpressionTree expressionTree = new ExpressionTree();
        //TODO - derive operator from query. for now hardcoding to AND as this is the only operator supported now.
        expressionTree.setOperator(Operator.AND);
        List<Operand> operands = query.getBool().getMust().stream()
            .map(mustCondition -> {
                Operand operand = new Operand();
                Field field = mustCondition.getMatch().entrySet().stream()
                    .map(match -> new Field(match.getKey(), match.getValue()))
                    .collect(Collectors.toList()).get(0);
                operand.setField(field);
                return operand;
            }).collect(Collectors.toList());
        expressionTree.setOperands(operands);
        return expressionTree;
    }

    public static Query buildQuery(ExpressionTree expressionTree) {
        List<MustCondition> mustConditions = expressionTree.getOperands().stream()
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
