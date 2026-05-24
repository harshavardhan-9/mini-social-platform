package com.social.minisocialplatform.controller;

import com.social.minisocialplatform.observability.MetricsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(
            MetricsService metricsService
    ) {

        this.metricsService =
                metricsService;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {

        Map<String, Object> metrics =
                new HashMap<>();

        metrics.put(
                "totalRequests",
                metricsService.getTotalRequests()
        );

        metrics.put(
                "averageLatencyMs",
                metricsService.getAverageLatency()
        );

        return metrics;
    }
}