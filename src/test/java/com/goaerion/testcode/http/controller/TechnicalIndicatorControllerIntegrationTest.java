package com.goaerion.testcode.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Set;

import com.goaerion.testcode.http.dto.twelvedata.IndicatorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@link TechnicalIndicatorController} 集成测试：发送真实 HTTP 请求到 Twelve Data 远端。
 * <p>与单元测试（{@code TechnicalIndicatorControllerTest}，mock HttpService）互补，
 * 集成测试验证 mock 盖不住的部分：
 * <ul>
 *   <li>application.yml + application-twelvedata.yml 的 spring.config.import 配置链与 @Value 真实注入</li>
 *   <li>HttpServiceFactory 选中真实实现（restClientHttpServiceImpl）并真实发出 HTTPS 请求</li>
 *   <li>响应 JSON 能被 {@link IndicatorResponse}（record + @JsonIgnoreProperties）正确反序列化</li>
 * </ul>
 *
 * <p><b>消耗说明</b>：Twelve Data 按 1 credit/symbol 计费，本类真实调用仅 2 个指标用例（sma、rsi，各 1 credit），
 * 且 {@code outputsize=2} 限制返回条数；名单与白名单校验用例不消耗 credits。
 *
 * <p><b>运行方式</b>：标记 {@code @Tag("integration")}，surefire 已按 excludedGroups 排除，
 * 日常 {@code mvn test} 不会执行（不消耗 credits、不依赖外网）；显式运行：
 * <pre>
 *   mvn test -Dgroups=integration        # 整个命令行
 *   或 IDE 中直接运行本测试类
 * </pre>
 * API key 来自 application-twelvedata.yml（环境变量 TWELVEDATA_APIKEY 优先）。
 */
@Tag("integration")
@SpringBootTest
@DisplayName("TechnicalIndicatorController 集成测试（真实请求，消耗 credits）")
class TechnicalIndicatorControllerIntegrationTest {

        @Autowired
        private TechnicalIndicatorController controller;

        @Test
        @DisplayName("真实调用 sma：返回 ok，values 含 sma 字段，meta 回显计算参数")
        void realRequest_sma() {
                IndicatorResponse response = controller.indicator("sma", Map.of(
                                "symbol", "AAPL",
                                "interval", "1day",
                                "time_period", "9",
                                "outputsize", "2"));

                assertThat(response.status()).isEqualTo("ok");
                assertThat(response.code()).isNull();
                // meta：请求回显与指标计算参数
                assertThat(response.meta())
                                .containsEntry("symbol", "AAPL")
                                .containsEntry("interval", "1day");
                assertThat(response.meta().get("indicator")).as("meta.indicator 回显计算参数").isNotNull();
                // values：非空且每个数据点含指标名字段（反序列化成功的直接证据）
                assertThat(response.values()).isNotEmpty();
                response.values().forEach(point -> {
                        assertThat(point).containsKey("datetime");
                        assertThat(point).as("数据点 %s 含 sma 值字段", point).containsKey("sma");
                });
        }

        @Test
        @DisplayName("真实调用 rsi（专有参数 time_period）：返回 ok，values 含 rsi 字段")
        void realRequest_rsiWithCustomPeriod() {
                IndicatorResponse response = controller.indicator("rsi", Map.of(
                                "symbol", "AAPL",
                                "interval", "1day",
                                "time_period", "14",
                                "outputsize", "2"));

                assertThat(response.status()).isEqualTo("ok");
                assertThat(response.values()).isNotEmpty();
                response.values().forEach(point -> {
                        assertThat(point).containsKey("datetime");
                        assertThat(point).as("数据点 %s 含 rsi 值字段", point).containsKey("rsi");
                        // RSI 取值范围 0~100（宽松边界，仅验证量纲，不断言具体数值）
                        double rsi = Double.parseDouble(String.valueOf(point.get("rsi")));
                        assertThat(rsi).isBetween(0.0, 100.0);
                });
        }

        @Test
        @DisplayName("名单接口：102 个指标、7 大类（不消耗 credits）")
        void indicatorsList() {
                Map<String, Object> result = controller.indicators();

                assertThat(result.get("total")).isEqualTo(102);
                long categoryCount = result.values().stream().filter(v -> v instanceof Set).count();
                assertThat(categoryCount).isEqualTo(7);
        }

        @Test
        @DisplayName("白名单前置拦截：未知指标抛 400，不发真实远端请求（不消耗 credits）")
        void unknownIndicator_blockedBeforeRemoteCall() {
                assertThatThrownBy(() -> controller.indicator("not_an_indicator",
                                Map.of("symbol", "AAPL", "interval", "1day")))
                                .isInstanceOf(ResponseStatusException.class)
                                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                                                .isEqualTo(HttpStatus.BAD_REQUEST));
        }
}