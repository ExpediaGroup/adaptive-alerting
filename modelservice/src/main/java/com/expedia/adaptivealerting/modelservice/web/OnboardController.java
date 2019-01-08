package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.repo.TagRepository;
import com.expedia.adaptivealerting.modelservice.service.OnboardService;
import com.expedia.adaptivealerting.modelservice.util.JpaConverterJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for OnboardService.
 *
 * @author tbahl
 */


@Slf4j
@RestController
public class OnboardController {

    @Autowired
    private OnboardService onboardService;

    @Autowired
    private JpaConverterJson jpaConverterJson;

    @Autowired
    private TagRepository tagRepository;

    @PostMapping(path = "/isOnboarded")
    public Boolean isOnboarded(@RequestBody Metric metric) {
        return onboardService.isOnboarded(metric);
    }

    @PostMapping(path = "/onboard")
    public Integer onboard(@RequestBody Metric metric) {
        return onboardService.onboard(metric);
    }
}

