package com.nitax.valueplusbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final int MAX_BODY_LOG_LENGTH = 10_000;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (shouldSkip(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    String requestId = UUID.randomUUID().toString().substring(0, 8);
    long startTime = System.currentTimeMillis();

    logRequestMetadata(request, requestId);

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      try {
        long duration = System.currentTimeMillis() - startTime;
        logBodies(wrappedRequest, wrappedResponse, requestId, duration);
      } catch (Exception e) {
        log.warn("[{}] Failed to log request/response bodies: {}", requestId, e.getMessage());
      }
      wrappedResponse.copyBodyToResponse();
    }
  }

  private void logRequestMetadata(HttpServletRequest request, String requestId) {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String fullPath = queryString != null ? uri + "?" + queryString : uri;
    String clientIp = getClientIp(request);
    String headers =
        Collections.list(request.getHeaderNames()).stream()
            .filter(h -> !h.equalsIgnoreCase("authorization") && !h.equalsIgnoreCase("cookie"))
            .map(h -> h + ": " + request.getHeader(h))
            .collect(Collectors.joining(", "));

    log.info(
        "[{}] --> {} {} | IP: {} | Headers: [{}]", requestId, method, fullPath, clientIp, headers);
  }

  private void logBodies(
      ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response,
      String requestId,
      long duration) {

    int status = response.getStatus();
    String uri = request.getRequestURI();

    String requestBody = readBody(request.getContentAsByteArray(), request.getContentType());
    String responseBody = readBody(response.getContentAsByteArray(), response.getContentType());

    if (!requestBody.isEmpty()) {
      log.info("[{}] Request body: {}", requestId, requestBody);
    }
    log.info(
        "[{}] <-- {} {} | {}ms | Response body: {}",
        requestId,
        status,
        uri,
        duration,
        responseBody.isEmpty() ? "(empty)" : responseBody);
  }

  private String readBody(byte[] content, String contentType) {
    if (content == null || content.length == 0) {
      return "";
    }
    if (isBinaryContent(contentType)) {
      return "(binary " + contentType + ", " + content.length + " bytes)";
    }
    String body = new String(content, StandardCharsets.UTF_8);
    if (body.length() > MAX_BODY_LOG_LENGTH) {
      return body.substring(0, MAX_BODY_LOG_LENGTH) + "...[truncated, total=" + body.length() + "]";
    }
    return body;
  }

  private boolean isBinaryContent(String contentType) {
    if (contentType == null) {
      return false;
    }
    String lower = contentType.toLowerCase();
    return lower.startsWith("image/")
        || lower.startsWith("video/")
        || lower.startsWith("audio/")
        || lower.contains("octet-stream")
        || lower.contains("multipart/form-data")
        || lower.contains("pdf")
        || lower.contains("zip")
        || lower.contains("spreadsheetml")
        || lower.contains("ms-excel");
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }

  private boolean shouldSkip(String uri) {
    return uri.contains("/actuator")
        || uri.contains("/swagger")
        || uri.contains("/v3/api-docs")
        || uri.endsWith(".css")
        || uri.endsWith(".js")
        || uri.endsWith(".ico")
        || uri.endsWith(".html")
        || uri.endsWith(".png");
  }
}
