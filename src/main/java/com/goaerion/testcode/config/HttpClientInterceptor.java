package com.goaerion.testcode.config;

import java.util.Map;

/**
 * 全局 HTTP 拦截器 SPI：在请求发送前回调，可读取请求信息、追加请求头。
 * <p>生效方式：实现本接口并声明为 Spring Bean，三个 HttpService 实现
 * （JDK HttpClient / RestTemplate / RestClient）会自动注入所有拦截器并逐个应用。
 * <p>典型用途：链路追踪 ID 透传、灰度标记、统一加签等。
 */
@FunctionalInterface
public interface HttpClientInterceptor {

    /**
     * @param method  HTTP 方法（GET/POST/...）
     * @param url     请求地址（GET 已含 query string）
     * @param headers 可变的请求头集合，实现类可往里追加请求头
     */
    void intercept(String method, String url, Map<String, String> headers);
}
