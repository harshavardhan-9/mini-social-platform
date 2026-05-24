package com.social.minisocialplatform.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.UUID;

@Component
public class RequestLoggingFilter
        extends OncePerRequestFilter {

    private final MetricsService metricsService;

    public RequestLoggingFilter(
            MetricsService metricsService
    ) {

        this.metricsService =
                metricsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId =
                UUID.randomUUID().toString();

        long start =
                System.currentTimeMillis();

        request.setAttribute(
                "requestId",
                requestId
        );

        filterChain.doFilter(
                request,
                response
        );

        long latency =
                System.currentTimeMillis()
                        - start;

        metricsService.recordRequest(
                latency
        );

        String log = String.format(
                """
                {
                    "requestId":"%s",
                    "method":"%s",
                    "path":"%s",
                    "status":%d,
                    "latencyMs":%d
                }
                """,
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                latency
        );

        System.out.println(log);
    }
}