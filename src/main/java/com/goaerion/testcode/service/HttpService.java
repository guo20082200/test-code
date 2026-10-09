package com.goaerion.testcode.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 通用 http 服务。
 * <p>http / https 均支持：各实现类按 URL 中的 scheme（http:// 或 https://）自动选择协议，
 * 代码中无需（也不应）针对协议做特殊处理。
 * <p>注意：若 https 目标使用自签名证书（内网环境常见），
 * 配置 {@code http.ssl.trust-all=true} 可跳过证书校验（见 HttpClientProvider）。
 */
public interface HttpService {

    /**
     * 发送 GET 请求，参数拼接在 URL 上（query string）
     *
     * @param url          请求地址（不含 query string）
     * @param params       查询参数，拼接在 URL 上，值为 null 的参数会被忽略
     * @param headers      动态请求头，由调用方组装
     * @param responseType 响应结果类型，由实现类负责反序列化
     * @param <T>          响应结果类型
     * @return 反序列化后的响应结果
     */
    <T> T get(String url, Map<String, Object> params, Map<String, String> headers, Class<T> responseType);

    /**
     * 发送 POST 请求（JSON）
     *
     * @param url          请求地址
     * @param body         请求体，任意结构，由实现类负责序列化为 JSON
     * @param headers      动态请求头，由调用方组装
     * @param responseType 响应结果类型，由实现类负责反序列化
     * @param <T>          响应结果类型
     * @return 反序列化后的响应结果
     */
    <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseType);

    /**
     * 发送 PUT 请求（JSON）
     *
     * @param url          请求地址
     * @param body         请求体，任意结构，由实现类负责序列化为 JSON
     * @param headers      动态请求头，由调用方组装
     * @param responseType 响应结果类型，由实现类负责反序列化
     * @param <T>          响应结果类型
     * @return 反序列化后的响应结果
     */
    <T> T put(String url, Object body, Map<String, String> headers, Class<T> responseType);

    /**
     * 发送 DELETE 请求
     *
     * @param url          请求地址
     * @param headers      动态请求头，由调用方组装
     * @param responseType 响应结果类型，由实现类负责反序列化
     * @param <T>          响应结果类型
     * @return 反序列化后的响应结果
     */
    <T> T delete(String url, Map<String, String> headers, Class<T> responseType);

    /**
     * 将查询参数拼接到 URL 上（GET 风格），参数名和参数值会进行 URL 编码。
     * 放在接口中作为 default 方法，供各实现类复用。
     */
    default String buildUrl(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        // URL 已带 ? 时用 & 续接参数
        String separator = url.contains("?") ? "&" : "?";
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            sb.append(separator)
                    .append(URLEncoder.encode(String.valueOf(entry.getKey()), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8)
                            .replace("+", "%20"));
            separator = "&";
        }
        return sb.toString();
    }
}
