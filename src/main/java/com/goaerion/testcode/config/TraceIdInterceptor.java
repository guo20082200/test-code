package com.goaerion.testcode.config;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * 链路追踪拦截器：为每个请求追加 X-Trace-Id 请求头，便于跨服务串联日志。
 * 已存在 X-Trace-Id 时沿用上游值（putIfAbsent），保证同一次调用链路 ID 一致。
 */
@Component
public class TraceIdInterceptor implements HttpClientInterceptor {

    @Override
    public void intercept(String method, String url, Map<String, String> headers) {
        headers.putIfAbsent("X-Trace-Id", UUID.randomUUID().toString().replace("-", ""));
    }
}
