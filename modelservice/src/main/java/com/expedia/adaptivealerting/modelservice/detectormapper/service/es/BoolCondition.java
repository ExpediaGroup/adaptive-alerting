package com.expedia.adaptivealerting.modelservice.detectormapper.service.es;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BoolCondition {
    private List<MustCondition> must;
}
