package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.config.HttpClientProvider;
import com.goaerion.testcode.http.dto.twelvedata.TimeSeriesResponse;
import com.goaerion.testcode.http.service.HttpService;
import com.goaerion.testcode.http.service.impl.RestClientHttpServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Twelve Data time_series 闭环集成测试：真实发出 HTTP 请求，验证全链路可用。
 * <p>
 * 日常构建不运行（消耗 API 积分、依赖外网），手动触发方式：
 * <pre>
 * TWELVEDATA_INTEGRATION=true bash mvnw test -Dtest=TimeSeriesIntegrationTest
 * </pre>
 * 普通的 {@code mvnw test} 中该用例会显示为 skipped。
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "TWELVEDATA_INTEGRATION", matches = "true")
class TimeSeriesIntegrationTest {

    @Test
    void timeSeriesClosedLoop() {
        // 1. 组装真实依赖（不走 Spring，等价于生产环境的 bean 组装方式）
        HttpClientProvider provider = new HttpClientProvider(false, Duration.ofSeconds(10), Duration.ofSeconds(30));
        HttpService httpService = new RestClientHttpServiceImpl(provider, List.of());

        // 2. 与 controller timeSeries() 相同的参数拼装
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", "AAPL");
        params.put("interval", "1min");
        params.put("outputsize", 1);
        params.put("apikey", "7eccf8efb5cf4a2397282c118df0306b");

        // 3. 真实发出请求（含 https 证书校验、连接/读超时、URL 编码全链路）
        TimeSeriesResponse response = httpService.get(
                "https://api.twelvedata.com/time_series", params, Map.of(), TimeSeriesResponse.class);

        // 4. 断言真实响应
        assertNotNull(response, "响应不能为空");
        assertEquals("ok", response.status(), "Twelve Data 应返回成功状态");

        assertNotNull(response.meta());
        assertEquals("AAPL", response.meta().symbol());
        assertEquals("1min", response.meta().interval());
        assertEquals("USD", response.meta().currency());

        assertNotNull(response.values());
        assertTrue(response.values().size() <= 1, "outputsize=1 时最多返回一条数据");
        TimeSeriesResponse.Value first = response.values().get(0);
        assertNotNull(first.datetime());
        assertNotNull(first.open());
        assertNotNull(first.high());
        assertNotNull(first.low());
        assertNotNull(first.close());
    }
}
