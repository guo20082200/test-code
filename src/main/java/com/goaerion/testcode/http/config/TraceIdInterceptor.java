package com.goaerion.testcode.http.config;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 链路追踪拦截器：为每个请求追加 X-Trace-Id 请求头，便于跨服务串联日志。
 * 已存在 X-Trace-Id 时沿用上游值（putIfAbsent），保证同一次调用链路 ID 一致。
 */
@Slf4j
@Component
public class TraceIdInterceptor implements HttpClientInterceptor {

    @Override
    public void intercept(String method, String url, Map<String, String> headers) {
        String traceId = headers.computeIfAbsent("X-Trace-Id",
                key -> UUID.randomUUID().toString().replace("-", ""));
        log.info("[TraceId] {} {} traceId={}", method, url, traceId);
    }
}
