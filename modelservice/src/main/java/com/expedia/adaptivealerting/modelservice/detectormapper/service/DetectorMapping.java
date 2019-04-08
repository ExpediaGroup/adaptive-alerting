package com.expedia.adaptivealerting.modelservice.detectormapper.service;

import com.expedia.adaptivealerting.modelservice.detectormapper.model.ExpressionTree;
import lombok.Data;

import java.util.List;

/**
 * The type Detector mapping.
 *
 * searchIndexes: index of matching metric-tag in request batch of metric-tags
 */
@Data
public class DetectorMapping {
    private String id;
    private Detector detector;
    private ExpressionTree conditionExpression;
    private User user;
    private long lastModifiedTimeInMillis;
    private long createdTimeInMillis;
    private boolean isEnabled;
    private List<Integer> searchIndexes;
}
