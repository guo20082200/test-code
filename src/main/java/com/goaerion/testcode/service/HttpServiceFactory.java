package com.goaerion.testcode.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HttpService 工厂（工厂 + 策略模式）：
 * Spring 把所有 HttpService 实现注入为 Map（key = bean 名称），
 * 每个实现就是一种"策略"，本工厂负责根据配置选择使用哪种策略。
 * 未配置时默认使用 restClientHttpServiceImpl。
 */
@Component
public class HttpServiceFactory {

    private final Map<String, HttpService> strategies;
    private final String defaultImplName;

    public HttpServiceFactory(Map<String, HttpService> strategies,
            @Value("${http.service.impl:restClientHttpServiceImpl}") String defaultImplName) {
        this.strategies = strategies;
        this.defaultImplName = defaultImplName;
    }

    /**
     * 获取默认配置的 HttpService 实现
     */
    public HttpService getHttpService() {
        return getHttpService(defaultImplName);
    }

    /**
     * 按名称获取指定的 HttpService 实现
     *
     * @param implName bean 名称，如 jdkHttpServiceImpl / restTemplateHttpServiceImpl / restClientHttpServiceImpl
     */
    public HttpService getHttpService(String implName) {
        HttpService service = strategies.get(implName);
        if (service == null) {
            throw new IllegalStateException(
                    "未找到名为 [" + implName + "] 的 HttpService 实现，可选值: " + strategies.keySet());
        }
        return service;
    }
}
