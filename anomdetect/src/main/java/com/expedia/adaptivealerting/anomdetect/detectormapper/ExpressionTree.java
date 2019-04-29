package com.expedia.adaptivealerting.anomdetect.detectormapper;

import lombok.Data;

import java.util.List;

@Data
public class ExpressionTree {
    private Operator operator;
    List<Operand> operands;
}
