package com.goaerion.testcode.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.goaerion.testcode.config.HttpClientInterceptor;
import com.goaerion.testcode.config.HttpClientProvider;
import com.goaerion.testcode.service.HttpService;

import lombok.extern.slf4j.Slf4j;

/**
 * 使用 RestClient 发送 http 请求的实现。
 * 底层复用 HttpClientProvider 的 JDK HttpClient（http/https 由 URL scheme
 * 自动识别，SSL/超时统一配置）。
 */
@Slf4j
@Service
public class RestClientHttpServiceImpl implements HttpService {

        private final RestClient restClient;

        /** 全局拦截器（Spring 自动注入所有 HttpClientInterceptor Bean），发送前逐个应用 */
        private final List<HttpClientInterceptor> interceptors;

        public RestClientHttpServiceImpl(HttpClientProvider httpClientProvider,
                        List<HttpClientInterceptor> interceptors) {
                this.restClient = RestClient.builder()
                                .requestFactory(httpClientProvider.createRequestFactory())
                                .build();
                this.interceptors = interceptors;
        }

        @Override
        public <T> T get(String url, Map<String, Object> params, Map<String, String> headers, Class<T> responseType) {
                // 参数拼接在 URL 上（query string）
                String fullUrl = buildUrl(url, params);
                Map<String, String> effectiveHeaders = effectiveHeaders("GET", fullUrl, headers);

                RestClient.RequestHeadersSpec<?> spec = restClient.get()
                                .uri(java.net.URI.create(fullUrl));
                effectiveHeaders.forEach(spec::header);

                log.info("[RestClient] GET 请求: url={} headers={}", fullUrl, formatHeaders(effectiveHeaders));
                T result = spec.retrieve().body(responseType);
                log.info("[RestClient] GET 响应: {}", result);
                return result;
        }

        @Override
        public <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseType) {
                Map<String, String> effectiveHeaders = effectiveHeaders("POST", url, headers);

                RestClient.RequestBodySpec requestSpec = restClient.post()
                                .uri(url)
                                .contentType(MediaType.APPLICATION_JSON);
                effectiveHeaders.forEach(requestSpec::header);

                log.info("[RestClient] POST 请求: url={} headers={} body={}", url, formatHeaders(effectiveHeaders), body);
                T result = requestSpec.body(body).retrieve().body(responseType);
                log.info("[RestClient] POST 响应: {}", result);
                return result;
        }

        @Override
        public <T> T put(String url, Object body, Map<String, String> headers, Class<T> responseType) {
                Map<String, String> effectiveHeaders = effectiveHeaders("PUT", url, headers);

                RestClient.RequestBodySpec requestSpec = restClient.put()
                                .uri(url)
                                .contentType(MediaType.APPLICATION_JSON);
                effectiveHeaders.forEach(requestSpec::header);

                log.info("[RestClient] PUT 请求: url={} headers={} body={}", url, formatHeaders(effectiveHeaders), body);
                T result = requestSpec.body(body).retrieve().body(responseType);
                log.info("[RestClient] PUT 响应: {}", result);
                return result;
        }

        @Override
        public <T> T delete(String url, Map<String, String> headers, Class<T> responseType) {
                Map<String, String> effectiveHeaders = effectiveHeaders("DELETE", url, headers);

                RestClient.RequestHeadersSpec<?> spec = restClient.delete()
                                .uri(url);
                effectiveHeaders.forEach(spec::header);

                log.info("[RestClient] DELETE 请求: url={} headers={}", url, formatHeaders(effectiveHeaders));
                T result = spec.retrieve().body(responseType);
                log.info("[RestClient] DELETE 响应: {}", result);
                return result;
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
