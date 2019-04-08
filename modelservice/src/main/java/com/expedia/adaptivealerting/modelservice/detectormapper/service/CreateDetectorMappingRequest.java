package com.expedia.adaptivealerting.modelservice.detectormapper.service;

import com.expedia.adaptivealerting.modelservice.detectormapper.model.ExpressionTree;
import lombok.Data;

@Data
public class CreateDetectorMappingRequest {
    private ExpressionTree conditionExpression;
    private Detector detector;
    private User user;
}


