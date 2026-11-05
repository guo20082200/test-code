package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.dto.twelvedata.TimeSeriesResponse;
import com.goaerion.testcode.http.service.CenterTaskService;
import com.goaerion.testcode.http.service.HttpService;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link HelloController#timeSeries} 单元测试，覆盖所有参数情况：
 * <ul>
 *     <li>默认参数（symbol、interval 的 defaultValue，outputsize 不传）</li>
 *     <li>显式传参（含特殊字符 symbol，如 ETH/BTC）</li>
 *     <li>outputsize 边界值（1 / 5000）与越界值透传</li>
 *     <li>interval 全部 12 种合法取值</li>
 *     <li>非法参数透传语义（非法 interval / 越界 outputsize 由 Twelve Data 远端校验）</li>
 *     <li>类型不匹配（outputsize=abc）返回 400，且不发起下游调用</li>
 * </ul>
 */
@WebMvcTest(HelloController.class)
class HelloControllerTimeSeriesTest {

    /** application.properties 中配置的 Twelve Data 地址与 key */
    private static final String EXPECTED_URL = "https://api.twelvedata.com/time_series";
    private static final String EXPECTED_API_KEY = "7eccf8efb5cf4a2397282c118df0306b";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HttpServiceFactory httpServiceFactory;

    @MockitoBean
    private CenterTaskService centerTaskService;

    private HttpService httpService;

    // 捕获 controller 传给 HttpService.get 的实际参数
    private String capturedUrl;
    private Map<String, Object> capturedParams;
    private Map<String, String> capturedHeaders;
    private Class<?> capturedResponseType;

    @BeforeEach
    void setUp() {
        httpService = mock(HttpService.class);
        when(httpServiceFactory.getHttpService()).thenReturn(httpService);
        when(httpService.get(anyString(), anyMap(), anyMap(), any()))
                .thenAnswer(inv -> {
                    capturedUrl = inv.getArgument(0);
                    capturedParams = inv.getArgument(1);
                    capturedHeaders = inv.getArgument(2);
                    capturedResponseType = inv.getArgument(3);
                    return new TimeSeriesResponse(null, List.of(), "ok");
                });
    }

    @Test
    @DisplayName("不传任何参数：使用默认值 symbol=AAPL、interval=1min，outputsize 不传递")
    void defaultParams() throws Exception {
        mockMvc.perform(get("/test/timeSeries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        assertEquals(EXPECTED_URL, capturedUrl);
        assertEquals("AAPL", capturedParams.get("symbol"));
        assertEquals("1min", capturedParams.get("interval"));
        assertEquals(EXPECTED_API_KEY, capturedParams.get("apikey"));
        // outputsize 为 null，参数 map 中值为 null（HttpService.buildUrl 拼接 URL 时会忽略 null 值参数）
        assertNull(capturedParams.get("outputsize"));
        // 无需额外请求头
        assertTrue(capturedHeaders.isEmpty());
        assertEquals(TimeSeriesResponse.class, capturedResponseType);
    }

    @Test
    @DisplayName("显式传全部参数：symbol 含特殊字符（ETH/BTC）原样透传，outputsize 正常传递")
    void explicitParams() throws Exception {
        mockMvc.perform(get("/test/timeSeries")
                        .param("symbol", "ETH/BTC")
                        .param("interval", "1day")
                        .param("outputsize", "100"))
                .andExpect(status().isOk());

        assertEquals(EXPECTED_URL, capturedUrl);
        assertEquals("ETH/BTC", capturedParams.get("symbol"));
        assertEquals("1day", capturedParams.get("interval"));
        assertEquals(100, capturedParams.get("outputsize"));
        assertEquals(EXPECTED_API_KEY, capturedParams.get("apikey"));
        assertTrue(capturedHeaders.isEmpty());
    }

    @Test
    @DisplayName("interval 全部 12 种合法取值均正常透传")
    void allValidIntervals() throws Exception {
        List<String> intervals = List.of("1min", "5min", "15min", "30min", "45min",
                "1h", "2h", "4h", "8h", "1day", "1week", "1month");
        for (String interval : intervals) {
            mockMvc.perform(get("/test/timeSeries").param("interval", interval))
                    .andExpect(status().isOk());
            assertEquals(interval, capturedParams.get("interval"));
        }
    }

    @ParameterizedTest(name = "outputsize={0} 边界值正常传递")
    @ValueSource(ints = {1, 5000})
    @DisplayName("outputsize 边界值 1 和 5000 正常传递")
    void outputsizeBoundaries(int outputsize) throws Exception {
        mockMvc.perform(get("/test/timeSeries").param("outputsize", String.valueOf(outputsize)))
                .andExpect(status().isOk());
        assertEquals(outputsize, capturedParams.get("outputsize"));
    }

    @ParameterizedTest(name = "outputsize={0} 越界时透传给远端校验")
    @ValueSource(ints = {0, -1, 5001})
    @DisplayName("outputsize 越界值（0 / -1 / 5001）照常透传，由 Twelve Data 校验")
    void outputsizeOutOfRangePassthrough(int outputsize) throws Exception {
        mockMvc.perform(get("/test/timeSeries").param("outputsize", String.valueOf(outputsize)))
                .andExpect(status().isOk());
        assertEquals(outputsize, capturedParams.get("outputsize"));
    }

    @Test
    @DisplayName("非法 interval（0.99min）透传语义：controller 不拦截，由 Twelve Data 返回错误")
    void invalidIntervalPassthrough() throws Exception {
        mockMvc.perform(get("/test/timeSeries").param("interval", "0.99min"))
                .andExpect(status().isOk());
        assertEquals("0.99min", capturedParams.get("interval"));
    }

    @Test
    @DisplayName("外汇对 symbol（EUR/USD）原样透传")
    void forexSymbolPassthrough() throws Exception {
        mockMvc.perform(get("/test/timeSeries")
                        .param("symbol", "EUR/USD")
                        .param("interval", "1week"))
                .andExpect(status().isOk());
        assertEquals("EUR/USD", capturedParams.get("symbol"));
        assertEquals("1week", capturedParams.get("interval"));
    }

    @ParameterizedTest(name = "symbol={0}, interval={1}, outputsize={2} 组合参数")
    @CsvSource({
            "AAPL, 1min, 30",
            "MSFT, 1day, 1",
            "TSLA, 1month, 5000",
            "EUR/USD, 1h, 100",
            "ETH/BTC, 45min, 7"
    })
    @DisplayName("symbol / interval / outputsize 任意组合均正确透传")
    void combinedParams(String symbol, String interval, int outputsize) throws Exception {
        mockMvc.perform(get("/test/timeSeries")
                        .param("symbol", symbol)
                        .param("interval", interval)
                        .param("outputsize", String.valueOf(outputsize)))
                .andExpect(status().isOk());

        assertEquals(symbol, capturedParams.get("symbol"));
        assertEquals(interval, capturedParams.get("interval"));
        assertEquals(outputsize, capturedParams.get("outputsize"));
    }

    @Test
    @DisplayName("outputsize 传非数字（abc）：Spring 参数绑定失败返回 400，且不发起下游调用")
    void outputsizeNotNumber() throws Exception {
        mockMvc.perform(get("/test/timeSeries").param("outputsize", "abc"))
                .andExpect(status().isBadRequest());

        assertNull(capturedUrl, "参数绑定失败时不应发起下游 HTTP 调用");
        verify(httpService, never()).get(anyString(), anyMap(), anyMap(), any());
    }
}
