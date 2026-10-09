package com.goaerion.testcode.config;

import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 共享的 JDK HttpClient 提供者（单例，线程安全）。
 * <p>
 * 统一管理底层 HttpClient 的创建配置：
 * <ul>
 * <li>SSL 策略：{@code http.ssl.trust-all=true} 时跳过 https 证书校验（仅建议内网/测试环境）</li>
 * <li>超时分级：连接超时 {@code http.timeout.connect}、读超时 {@code http.timeout.read}</li>
 * </ul>
 */
@Slf4j
@Component
public class HttpClientProvider {

    private final HttpClient httpClient;

    /** 读超时：等待响应数据的最长时间 */
    private final Duration readTimeout;

    public HttpClientProvider(
            @Value("${http.ssl.trust-all:false}") boolean trustAll,
            @Value("${http.timeout.connect:10s}") Duration connectTimeout,
            @Value("${http.timeout.read:30s}") Duration readTimeout) {
        this.readTimeout = readTimeout;
        HttpClient.Builder builder = HttpClient.newBuilder()
                // 连接超时：建立 TCP/TLS 连接的最长时间（JDK HttpClient 全局生效）
                .connectTimeout(connectTimeout);
        if (trustAll) {
            builder.sslContext(createTrustAllSslContext());
            log.warn("已开启 SSL 信任所有证书模式（http.ssl.trust-all=true），存在中间人攻击风险，仅限内网/测试环境使用！");
        }
        this.httpClient = builder.build();
        log.info("HttpClient 初始化完成: 连接超时={}s, 读超时={}s",
                connectTimeout.toSeconds(), readTimeout.toSeconds());
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * 创建统一配置的请求工厂（供 RestClient / RestTemplate 复用）：
     * 底层复用共享 HttpClient，并应用读超时（等待响应数据的最长时间）
     */
    public JdkClientHttpRequestFactory createRequestFactory() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    /**
     * 构建信任所有证书的 SSLContext（跳过证书校验）
     */
    private SSLContext createTrustAllSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("创建信任所有证书的 SSLContext 失败", e);
        }
    }
}
