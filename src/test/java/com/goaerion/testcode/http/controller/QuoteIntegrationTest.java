package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.config.HttpClientProvider;
import com.goaerion.testcode.http.dto.twelvedata.QuoteResponse;
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
 * Twelve Data quote 闭环集成测试：真实发出 HTTP 请求，验证全链路可用（不是验证参数拼装）。
 * <p>
 * 日常构建不运行（消耗 API 积分、依赖外网），手动触发方式：
 * <pre>
 * TWELVEDATA_INTEGRATION=true bash mvnw test -Dtest=QuoteIntegrationTest
 * </pre>
 * 普通的 {@code mvnw test} 中该用例会显示为 skipped。
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "TWELVEDATA_INTEGRATION", matches = "true")
class QuoteIntegrationTest {

    @Test
    void quoteClosedLoop() {
        // 1. 组装真实依赖（不走 Spring，等价于生产环境的 bean 组装方式）
        HttpClientProvider provider = new HttpClientProvider(false, Duration.ofSeconds(10), Duration.ofSeconds(30));
        HttpService httpService = new RestClientHttpServiceImpl(provider, List.of());

        // 2. 与 controller quote() 相同的参数拼装
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", "AAPL");
        params.put("interval", "1day");
        params.put("apikey", "7eccf8efb5cf4a2397282c118df0306b");

        // 3. 真实发出请求（含 https 证书校验、连接/读超时、URL 编码全链路）
        QuoteResponse response = httpService.get(
                "https://api.twelvedata.com/quote", params, Map.of(), QuoteResponse.class);

        // 4. 断言真实响应：验证反序列化契约与业务语义
        assertNotNull(response, "响应不能为空");

        // 标的信息
        assertEquals("AAPL", response.symbol());
        assertEquals("NASDAQ", response.exchange());
        assertEquals("USD", response.currency());

        // 价格合法性：开高低收均为正数，且 high >= max(open, close) >= min(open, close) >= low
        assertNotNull(response.close(), "close 即最新价，不应为空");
        double open = Double.parseDouble(response.open());
        double high = Double.parseDouble(response.high());
        double low = Double.parseDouble(response.low());
        double close = Double.parseDouble(response.close());
        assertTrue(open > 0 && high > 0 && low > 0 && close > 0, "价格应为正数");
        assertTrue(high >= Math.max(open, close), "high 应大于等于 open/close 的较大值");
        assertTrue(low <= Math.min(open, close), "low 应小于等于 open/close 的较小值");

        // 涨跌幅语义一致：percent_change ≈ (close - previous_close) / previous_close * 100
        if (response.previousClose() != null && response.percentChange() != null) {
            double prevClose = Double.parseDouble(response.previousClose());
            double percentChange = Double.parseDouble(response.percentChange());
            double expected = (close - prevClose) / prevClose * 100;
            assertEquals(expected, percentChange, 0.1, "percent_change 应与价格计算结果一致");
        }

        // 市场状态与 52 周指标（字段存在时才校验）
        if (response.fiftyTwoWeek() != null) {
            double wLow = Double.parseDouble(response.fiftyTwoWeek().low());
            double wHigh = Double.parseDouble(response.fiftyTwoWeek().high());
            assertTrue(wLow <= wHigh, "52 周最低价不应高于最高价");
            assertTrue(low >= wLow && high <= wHigh, "当日 high/low 应落在 52 周区间内");
        }

        // 开盘时间戳应早于等于最新报价时间戳
        if (response.timestamp() != null && response.lastQuoteAt() != null) {
            assertTrue(response.timestamp() <= response.lastQuoteAt(), "K 线开盘时间不应晚于最新报价时间");
        }
    }
}
