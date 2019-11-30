package com.expedia.adaptivealerting.modelservice.acutator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@Component
public class MetricFilter implements Filter {

    @Autowired
    private CustomActuatorMetricService actuatorMetricService;

    @Override
    public void init(final FilterConfig config) {
        if (actuatorMetricService == null) {
            actuatorMetricService = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext()).getBean(CustomActuatorMetricService.class);
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws java.io.IOException, ServletException {
        chain.doFilter(request, response);
        val status = ((HttpServletResponse) response).getStatus();
        actuatorMetricService.increaseCount(status);
    }

    @Override
    public void destroy() {
    }
}