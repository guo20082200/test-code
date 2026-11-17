package com.goaerion.testcode.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.goaerion.testcode.http.dto.twelvedata.IndicatorResponse;
import com.goaerion.testcode.http.service.HttpService;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@link TechnicalIndicatorController} 单元测试
 * <p>测试依据：doc/quant/technical-indicators/ 目录下的 102 个技术指标文档。
 * 覆盖点：
 * <ul>
 *   <li>透传逻辑：URL 拼接（indicator-base-url + 指标名）、查询参数全量透传、apiKey 服务端覆盖</li>
 *   <li>白名单校验：未知指标返回 400，每个分类的代表指标可正常请求</li>
 *   <li>响应透传：成功与出错（code/message）响应原样返回</li>
 *   <li>指标名单：/indicators 返回 7 大类共 102 个，且与文档目录下的 md 文件一一对应</li>
 *   <li>HTTP 层（MockMvc standalone）：路由映射与状态码</li>
 * </ul>
 * 不发真实 HTTP 请求：HttpService 以 Mockito mock，通过真实 HttpServiceFactory 组装。
 */
@DisplayName("TechnicalIndicatorController 单元测试")
class TechnicalIndicatorControllerTest {

        private static final String BASE_URL = "https://api.twelvedata.com";
        private static final String API_KEY = "test-api-key";

        private HttpService httpService;
        private TechnicalIndicatorController controller;
        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                // mock 底层 HttpService，套真实工厂（同时覆盖工厂按默认名称取实现的逻辑）
                httpService = mock(HttpService.class);
                controller = new TechnicalIndicatorController(
                                new HttpServiceFactory(Map.of("restClientHttpServiceImpl", httpService),
                                                "restClientHttpServiceImpl"));
                // 注入 @Value 字段（无 Spring 容器，手动赋值）
                ReflectionTestUtils.setField(controller, "twelvedataIndicatorBaseUrl", BASE_URL);
                ReflectionTestUtils.setField(controller, "twelvedataApiKey", API_KEY);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

        private void stubGet(IndicatorResponse response) {
                when(httpService.get(anyString(), anyMap(), anyMap(), eq(IndicatorResponse.class)))
                                .thenReturn(response);
        }

        private IndicatorResponse okResponse() {
                return new IndicatorResponse(
                                Map.of("symbol", "AAPL", "interval", "1day"),
                                List.of(Map.of("datetime", "2025-11-14", "value", "225.0")),
                                "ok", null, null);
        }

        @Test
        @DisplayName("indicator：查询参数全量透传并追加服务端 apiKey，URL = base + / + 指标名")
        @SuppressWarnings("unchecked")
        void indicator_requestParamsPassthrough_andApiKeyAppended() {
                IndicatorResponse expected = okResponse();
                stubGet(expected);

                Map<String, String> queryParams = new LinkedHashMap<>();
                queryParams.put("symbol", "AAPL");
                queryParams.put("interval", "1day");
                queryParams.put("time_period", "20");
                queryParams.put("series_type", "close");
                IndicatorResponse actual = controller.indicator("sma", queryParams);

                // 1. 返回值即 HttpService 的反序列化结果，原样透传
                assertThat(actual).isSameAs(expected);

                // 2. URL 与请求头
                ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
                ArgumentCaptor<Map<String, String>> headersCaptor = ArgumentCaptor.forClass(Map.class);
                verify(httpService).get(eq(BASE_URL + "/sma"), paramsCaptor.capture(),
                                headersCaptor.capture(), eq(IndicatorResponse.class));
                assertThat(headersCaptor.getValue()).isEmpty();

                // 3. 查询参数全量透传 + apiKey 由服务端追加
                assertThat(paramsCaptor.getValue())
                                .containsEntry("symbol", "AAPL")
                                .containsEntry("interval", "1day")
                                .containsEntry("time_period", "20")
                                .containsEntry("series_type", "close")
                                .containsEntry("apikey", API_KEY)
                                .hasSize(5);
        }

        @Test
        @DisplayName("indicator：请求方携带的 apiKey 被服务端配置覆盖，防止客户端伪造")
        @SuppressWarnings("unchecked")
        void indicator_requestApiKey_overriddenByServerConfig() {
                stubGet(okResponse());

                Map<String, String> queryParams = new LinkedHashMap<>();
                queryParams.put("symbol", "AAPL");
                queryParams.put("interval", "1day");
                queryParams.put("apikey", "forged-key");
                controller.indicator("ema", queryParams);

                ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
                verify(httpService).get(eq(BASE_URL + "/ema"), paramsCaptor.capture(),
                                anyMap(), eq(IndicatorResponse.class));
                assertThat(paramsCaptor.getValue())
                                .containsEntry("apikey", API_KEY)
                                .hasSize(3);
        }

        @Test
        @DisplayName("indicator：不在白名单的指标名抛 400，不发起远端请求")
        void indicator_unknownIndicator_throwsBadRequest() {
                assertThatThrownBy(() -> controller.indicator("not_an_indicator",
                                Map.of("symbol", "AAPL", "interval", "1day")))
                                .isInstanceOf(ResponseStatusException.class)
                                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                                                .isEqualTo(HttpStatus.BAD_REQUEST))
                                .hasMessageContaining("不支持的技术指标: not_an_indicator");
                verify(httpService, times(0)).get(anyString(), anyMap(), anyMap(), any());
        }

        @Test
        @DisplayName("indicator：7 大类各取一个代表指标，URL 按 指标名 拼接")
        void indicator_representativeOfEachCategory_buildsCorrectUrl() {
                stubGet(okResponse());

                List<String> representatives = List.of(
                                "sma",          // 01 重叠研究
                                "rsi",          // 02 动量指标
                                "obv",          // 03 成交量指标
                                "atr",          // 04 波动率指标
                                "typprice",     // 05 价格变换
                                "ht_sine",      // 06 周期指标
                                "stddev");      // 07 统计函数
                for (String indicator : representatives) {
                        controller.indicator(indicator, Map.of("symbol", "AAPL", "interval", "1day"));
                }

                ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
                verify(httpService, times(7)).get(urlCaptor.capture(), anyMap(), anyMap(),
                                eq(IndicatorResponse.class));
                assertThat(urlCaptor.getAllValues())
                                .containsExactlyInAnyOrderElementsOf(representatives.stream()
                                                .map(name -> BASE_URL + "/" + name).toList());
        }

        @Test
        @DisplayName("indicator：远端出错响应（code/message/status）原样透传给调用方")
        void indicator_errorResponse_passedThroughAsIs() {
                IndicatorResponse error = new IndicatorResponse(null, null, "error", 429, "quota exceeded");
                stubGet(error);

                IndicatorResponse actual = controller.indicator("rsi",
                                Map.of("symbol", "AAPL", "interval", "1day"));

                assertThat(actual).isSameAs(error);
        }

        @Test
        @DisplayName("indicators：返回 7 大类共 102 个指标，各类数量与文档一致")
        void indicators_returns102AndSevenCategoriesWithExpectedSizes() {
                Map<String, Object> result = controller.indicators();

                assertThat(result.get("total")).isEqualTo(102);
                Map<String, Integer> expectedSizes = Map.of(
                                "01-overlap-studies 重叠研究", 21,
                                "02-momentum-indicators 动量指标", 34,
                                "03-volume-indicators 成交量指标", 4,
                                "04-volatility-indicators 波动率指标", 5,
                                "05-price-transform 价格变换", 18,
                                "06-cycle-indicators 周期指标", 5,
                                "07-statistic-functions 统计函数", 15);
                for (Map.Entry<String, Integer> entry : expectedSizes.entrySet()) {
                        assertThat((Set<?>) result.get(entry.getKey()))
                                        .as("分类 %s 的指标数量", entry.getKey())
                                        .hasSize(entry.getValue());
                }
        }

        @Test
        @DisplayName("白名单与文档目录一一对应：doc/quant/technical-indicators/0*/*.md 共 102 个")
        void whitelist_exactlyMatchesDocDirectory() {
                Path base = Paths.get("doc", "quant", "technical-indicators");
                assertThat(base).isDirectory();

                // 1. 扫描文档目录：7 个分类子目录（0* 开头）下的全部指标 md（排除各自的 README.md）
                Set<String> docIndicators = new HashSet<>();
                int categoryCount;
                try (Stream<Path> entries = Files.list(base)) {
                        List<Path> categoryDirs = entries
                                        .filter(Files::isDirectory)
                                        .filter(dir -> dir.getFileName().toString().startsWith("0"))
                                        .toList();
                        categoryCount = categoryDirs.size();
                        for (Path dir : categoryDirs) {
                                try (Stream<Path> files = Files.list(dir)) {
                                        files.map(f -> f.getFileName().toString())
                                                        .filter(name -> name.endsWith(".md"))
                                                        .filter(name -> !"README.md".equals(name))
                                                        .map(name -> name.substring(0, name.length() - 3))
                                                        .forEach(docIndicators::add);
                                }
                        }
                }
                assertThat(categoryCount).as("文档分类数").isEqualTo(7);

                // 2. 代码侧名单取自 indicators() 返回的 7 个分类集合的并集
                Map<String, Object> listed = controller.indicators();
                Set<String> codeIndicators = listed.values().stream()
                                .filter(v -> v instanceof Set)
                                .map(v -> (Set<?>) v)
                                .flatMap(Set::stream)
                                .map(String::valueOf)
                                .collect(Collectors.toSet());

                // 3. 双向一致：文档里的每个指标都在白名单中，白名单里也没有文档之外的多余项
                assertThat(docIndicators).hasSize(102);
                assertThat(codeIndicators).as("与文档目录指标一一对应").isEqualTo(docIndicators);
        }

        @Test
        @DisplayName("HTTP 层：GET /test/indicator/{indicator} 合法指标返回 200 与远端数据")
        void mockMvc_indicator_ok() throws Exception {
                stubGet(okResponse());

                mockMvc.perform(get("/test/indicator/rsi")
                                .queryParam("symbol", "AAPL")
                                .queryParam("interval", "1day"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("\"status\":\"ok\"")));
                verify(httpService).get(eq(BASE_URL + "/rsi"), anyMap(), anyMap(), eq(IndicatorResponse.class));
        }

        @Test
        @DisplayName("HTTP 层：GET /test/indicator/{indicator} 未知指标返回 400")
        void mockMvc_unknownIndicator_badRequest() throws Exception {
                mockMvc.perform(get("/test/indicator/not_an_indicator")
                                .queryParam("symbol", "AAPL")
                                .queryParam("interval", "1day"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("HTTP 层：GET /test/indicators 返回 200 与 102 个指标名单")
        void mockMvc_indicatorsList_ok() throws Exception {
                mockMvc.perform(get("/test/indicators"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("\"total\":102")));
        }
}