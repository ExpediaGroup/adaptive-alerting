package com.expedia.adaptivealerting.anomdetect.detectormapper;

import lombok.Data;

@Data
public class Operand {
    private Field field;
    private ExpressionTree expression;
}
