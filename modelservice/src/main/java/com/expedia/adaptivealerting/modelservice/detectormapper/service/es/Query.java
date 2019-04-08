package com.expedia.adaptivealerting.modelservice.detectormapper.service.es;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Query {
    private BoolCondition bool;
}
