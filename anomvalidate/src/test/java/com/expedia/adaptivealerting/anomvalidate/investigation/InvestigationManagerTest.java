package com.expedia.adaptivealerting.anomvalidate.investigation;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.InvestigationResult;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.*;

public class InvestigationManagerTest {
    private static final String ENDPOINT_PATH = "/endpoint";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private String getEndpoint() {
        return String.format("http://localhost:%d%s", wireMockRule.port(), ENDPOINT_PATH);
    }

    @Test
    public void investigateDoesntFailOnNull() {
        InvestigationManager im = new InvestigationManager(getEndpoint(), null);

        AnomalyResult result = im.investigate(null);

        assertNull(result);
    }

    @Test
    public void investigateHasEmptyInvestigationWithNoEndpoint() {
        InvestigationManager im = new InvestigationManager(null, null);

        AnomalyResult result = im.investigate(new AnomalyResult());

        assertEquals(0, result.getInvestigationResults().size());
    }

    @Test
    public void investigateHasEmptyInvestigationWhenInvestigationFails() {
        stubFor(post(urlEqualTo(ENDPOINT_PATH)).willReturn(aResponse().withStatus(500)));
        InvestigationManager im = new InvestigationManager(getEndpoint(), null);

        AnomalyResult result = im.investigate(new AnomalyResult());

        assertEquals(0, result.getInvestigationResults().size());
    }

    @Test
    public void investigateSucceedsWithValidResponse() {
        stubFor(post(urlEqualTo(ENDPOINT_PATH)).willReturn(okForJson(validJsonResponse())));
        InvestigationManager im = new InvestigationManager(getEndpoint(), null);

        AnomalyResult result = im.investigate(new AnomalyResult());

        assertEquals(1, result.getInvestigationResults().size());
    }

    @Test
    public void investigateHasEmptyInvestigationWhenInvestigationTimesout() {
        stubFor(post(urlEqualTo(ENDPOINT_PATH)).willReturn(okForJson(validJsonResponse()).withFixedDelay(100)));
        InvestigationManager im = new InvestigationManager(getEndpoint(), 1);

        AnomalyResult result = im.investigate(new AnomalyResult());

        assertEquals(0, result.getInvestigationResults().size());
    }

    private List<InvestigationResult> validJsonResponse() {
        return Collections.singletonList(new InvestigationResult());
    }
}
