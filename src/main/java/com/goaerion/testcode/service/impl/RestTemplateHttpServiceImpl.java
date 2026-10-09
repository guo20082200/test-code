package com.goaerion.testcode.service.impl;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.goaerion.testcode.config.HttpClientInterceptor;
import com.goaerion.testcode.config.HttpClientProvider;
import com.goaerion.testcode.service.HttpService;

import lombok.extern.slf4j.Slf4j;

/**
 * 使用 RestTemplate 发送 http 请求的实现。
 * 底层复用 HttpClientProvider 的 JDK HttpClient（http/https 由 URL scheme
 * 自动识别，SSL/超时统一配置）。
 */
@Slf4j
@Service
public class RestTemplateHttpServiceImpl implements HttpService {

    private final RestTemplate restTemplate;

    /** 全局拦截器（Spring 自动注入所有 HttpClientInterceptor Bean），发送前逐个应用 */
    private final List<HttpClientInterceptor> interceptors;

    public RestTemplateHttpServiceImpl(HttpClientProvider httpClientProvider,
            List<HttpClientInterceptor> interceptors) {
        this.restTemplate = new RestTemplate(httpClientProvider.createRequestFactory());
        this.interceptors = interceptors;
    }

    @Override
    public <T> T get(String url, Map<String, Object> params, Map<String, String> headers, Class<T> responseType) {
        // 参数拼接在 URL 上（query string），用 URI 传入避免 RestTemplate 对已编码字符二次编码
        return exchange(HttpMethod.GET, URI.create(buildUrl(url, params)), null, headers, responseType);
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
     * 统一发送逻辑：组装请求头和请求体（HttpEntity）→ exchange 发送 → 取出响应体
     */
    private <T> T exchange(HttpMethod method, Object url, Object body,
            Map<String, String> headers, Class<T> responseType) {
        // 0. 应用全局拦截器（复制一份可变集合，避免污染调用方/不可变 Map）
        String urlForLog = url.toString();
        Map<String, String> effectiveHeaders = effectiveHeaders(method.name(), urlForLog, headers);

        // 1. 写发送http请求的请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        if (body != null) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
        effectiveHeaders.forEach(httpHeaders::set);

        // 2. 写发送http请求的请求体（HttpEntity = 请求头 + 请求体）
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, httpHeaders);

        log.info("[RestTemplate] 请求: {} {} headers={} body={}",
                method, urlForLog, formatHeaders(effectiveHeaders), body);

        // 3. 发送http请求
        // 4. 处理http响应结果
        // url 可能是 String 或 URI：传 URI 时 RestTemplate 不会对已编码字符二次编码
        ResponseEntity<T> responseEntity = url instanceof URI uri
                ? restTemplate.exchange(uri, method, httpEntity, responseType)
                : restTemplate.exchange((String) url, method, httpEntity, responseType);

        log.info("[RestTemplate] 响应: 状态码={}, 响应体={}",
                responseEntity.getStatusCode().value(), responseEntity.getBody());
        return responseEntity.getBody();
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
