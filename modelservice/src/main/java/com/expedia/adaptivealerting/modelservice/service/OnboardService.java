package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Metric;

/**
 * Onboard Service check whether isOnboarded or need to be onboarded.
 *
 * @author tbahl
 */


public interface OnboardService {

    Boolean isOnboarded(Metric metric);

    Integer onboard(Metric metric);


}