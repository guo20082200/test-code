package com.goaerion.testcode.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.goaerion.testcode.config.HttpClientInterceptor;
import com.goaerion.testcode.config.HttpClientProvider;
import com.goaerion.testcode.service.HttpService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * 使用原生 JDK HttpClient（java.net.http）发送 http 请求的实现
 */
@Slf4j
@Service
public class JdkHttpServiceImpl implements HttpService {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private final HttpClientProvider httpClientProvider;

    /** 全局拦截器（Spring 自动注入所有 HttpClientInterceptor Bean），发送前逐个应用 */
    private final List<HttpClientInterceptor> interceptors;

    public JdkHttpServiceImpl(HttpClientProvider httpClientProvider,
            List<HttpClientInterceptor> interceptors) {
        this.httpClientProvider = httpClientProvider;
        this.interceptors = interceptors;
    }

    @Override
    public <T> T get(String url, Map<String, Object> params, Map<String, String> headers, Class<T> responseType) {
        // 参数拼接在 URL 上（query string）
        return exchange(HttpMethod.GET, buildUrl(url, params), null, headers, responseType);
    }

    @Override
    public <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        return exchange(HttpMethod.POST, url, body, headers, responseType);
    }

    @Override
    public <T> T put(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        return exchange(HttpMethod.PUT, url, body, headers, responseType);
    }

    @Override
    public <T> T delete(String url, Map<String, String> headers, Class<T> responseType) {
        return exchange(HttpMethod.DELETE, url, null, headers, responseType);
    }

    /**
     * 统一发送逻辑：构建请求（url、请求头、请求体）→ 发送 → 处理响应
     */
    private <T> T exchange(HttpMethod method, String url, Object body,
            Map<String, String> headers, Class<T> responseType) {
        try {
            // 0. 应用全局拦截器（复制一份可变集合，避免污染调用方/不可变 Map）
            Map<String, String> effectiveHeaders = effectiveHeaders(method.name(), url, headers);

            // 1. 构建http请求（url、请求头、请求体、读超时）
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    // 读超时：等待响应数据的最长时间（per-request 设置）
                    .timeout(httpClientProvider.getReadTimeout())
                    .method(method.name(), bodyPublisher(body));
            effectiveHeaders.forEach(builder::header);
            HttpRequest httpRequest = builder.build();

            log.info("[JDK-HttpClient] 请求: {} {} headers={} body={}",
                    method, url, formatHeaders(effectiveHeaders), body);

            // 2. 发送http请求（http/https 由 URL scheme 自动识别，SSL 策略见 HttpClientProvider）
            HttpResponse<String> httpResponse = httpClientProvider.getHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            log.info("[JDK-HttpClient] 响应: 状态码={}, 响应体={}",
                    httpResponse.statusCode(), httpResponse.body());

            // 3. 处理http响应结果
            return objectMapper.readValue(httpResponse.body(), responseType);
        } catch (IOException | InterruptedException e) {
            log.error("[JDK-HttpClient] 发送http请求失败: {} {}", method, url, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("发送http请求失败: " + url, e);
        }
    }

    /**
     * 复制请求头为可变集合后，逐个应用全局拦截器（拦截器可追加请求头，如 X-Trace-Id）
     */
    private Map<String, String> effectiveHeaders(String method, String url, Map<String, String> headers) {
        Map<String, String> effective = new LinkedHashMap<>(headers);
        interceptors.forEach(interceptor -> interceptor.intercept(method, url, effective));
        return effective;
    }

    /**
     * 有请求体时序列化为 JSON，无请求体（GET/DELETE）时返回 NoBody
     */
    private HttpRequest.BodyPublisher bodyPublisher(Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
    }

    /**
     * 格式化请求头用于日志输出，Authorization 敏感信息脱敏
     */
    private String formatHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(e -> "authorization".equalsIgnoreCase(e.getKey())
                        ? e.getKey() + "=***"
                        : e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
