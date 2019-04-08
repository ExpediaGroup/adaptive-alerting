package com.expedia.adaptivealerting.modelservice.detectormapper.model;

import lombok.Data;

@Data
public class Operand {
    private Field field;
    private ExpressionTree expression;
}
