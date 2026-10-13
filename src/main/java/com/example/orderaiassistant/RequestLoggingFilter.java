package com.example.orderaiassistant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Ivy
 * @date 2026/10/2
 * @description
 */

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();
        boolean completed = false;

        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        try {
            log.info("request_started method={} path={}",
                    request.getMethod(),
                    request.getRequestURI());

            filterChain.doFilter(request, response);
            completed = true;

        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startedAt
            );

            if (completed) {
                log.info(
                        "request_finished httpStatus={} durationMs={}",
                        response.getStatus(),
                        durationMs
                );
            } else {
                // 异常逃出过滤器时，容器可能尚未设置最终 HTTP 状态
                log.error(
                        "request_aborted durationMs={} finalHttpStatus=UNKNOWN",
                        durationMs
                );
            }

            MDC.remove("requestId");
        }
    }
}