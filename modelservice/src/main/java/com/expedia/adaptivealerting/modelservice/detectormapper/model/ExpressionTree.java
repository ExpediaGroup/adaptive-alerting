package com.expedia.adaptivealerting.modelservice.detectormapper.model;

import lombok.Data;

import java.util.List;

@Data
public class ExpressionTree {
    private Operator operator;
    List<Operand> operands;
}
