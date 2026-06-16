package com.finance.app.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/h2-console")
                || path.contains("/swagger-ui")
                || path.contains("/v3/api-docs")
                || path.contains("/favicon");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String path = request.getMethod() + " " + request.getRequestURI() + query;

        boolean isMultipart = request.getContentType() != null
                && request.getContentType().startsWith("multipart/");

        if (isMultipart) {
            filterChain.doFilter(request, response);
            long elapsed = System.currentTimeMillis() - start;
            logMultipart(path, response.getStatus(), elapsed);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            int status = responseWrapper.getStatus();

            if (status >= 500) {
                log.error("{} → {} ({}ms)", path, status, elapsed);
            } else if (status >= 400) {
                log.warn("{} → {} ({}ms)", path, status, elapsed);
            } else if (log.isDebugEnabled()) {
                String body = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                log.debug("{} → {} ({}ms) body={}", path, status, elapsed, body);
            } else {
                log.info("{} → {} ({}ms)", path, status, elapsed);
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    private void logMultipart(String path, int status, long elapsed) {
        if (status >= 500) {
            log.error("{} → {} ({}ms) [multipart]", path, status, elapsed);
        } else if (status >= 400) {
            log.warn("{} → {} ({}ms) [multipart]", path, status, elapsed);
        } else {
            log.info("{} → {} ({}ms) [multipart]", path, status, elapsed);
        }
    }
}
