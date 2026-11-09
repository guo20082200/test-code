package com.goaerion.testcode.http.quant.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Twelve Data 技术指标接口探针：调 sma/rsi/macd/bbands 四个代表接口，
 * 将原始响应 pretty JSON 落盘到 doc/quant/technical-indicators/probe/ 供研读。
 * <p>
 * 研究目的：先看"标准答案"（远端算好的指标序列），后续用 ta4j 本地复算并对照验证。
 * 参数口径：MSFT / interval=1day / outputsize=50 / adjust=none（与库内 td_bar 未复权口径对齐，
 * 避免 API 默认 adjust=splits 的拆股复权导致两边不可比）；各指标 time_period 等参数取文档默认值。
 * <p>
 * 注意：真实外网调用，每个接口消耗 1 credit/symbol，共 4 credits；
 * 路由为 https://api.twelvedata.com/{指标名}（见各指标 md 文档的请求示例），
 * 配置项 twelvedata.technical-indicators-send-url 是另一聚合端点，探针未使用。
 * <p>
 * 手动触发方式：
 * <pre>
 * QUANT_API_PROBE=true mvn test -Dtest=TechnicalIndicatorProbeTest
 * </pre>
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "QUANT_API_PROBE", matches = "true")
@SpringBootTest
class TechnicalIndicatorProbeTest {

    /** Twelve Data API base（指标路由 = base + /{指标名}） */
    private static final String BASE_URL = "https://api.twelvedata.com";

    /** 落盘目录：与指标文档同树，便于对照研读 */
    private static final Path OUTPUT_DIR = Path.of("doc/quant/technical-indicators/probe");

    /** 研究标的与口径（库内已有 MSFT 5000 根 1day，可对照） */
    private static final String SYMBOL = "MSFT";
    private static final String INTERVAL = "1day";
    private static final int OUTPUTSIZE = 50;

    @Autowired
    private HttpServiceFactory httpServiceFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${twelvedata.apikey}")
    private String twelvedataApiKey;

    @Test
    void probeSma() throws Exception {
        // SMA(20, close)：单输出序列，最易与 ta4j SMA 对齐
        Map<String, Object> extra = Map.of("time_period", 20);
        JsonNode root = fetchAndSave("sma", extra);

        assertCommonShape(root, "sma");
        assertEquals("20", root.path("meta").path("indicator").path("time_period").asText(),
                "meta 应回显 time_period=20");
    }

    @Test
    void probeRsi() throws Exception {
        // RSI(14, close)：Wilder 平滑，ta4j 的 RSI 同口径
        Map<String, Object> extra = Map.of("time_period", 14);
        JsonNode root = fetchAndSave("rsi", extra);

        assertCommonShape(root, "rsi");
        assertTrue(Double.parseDouble(firstValueField(root, "rsi")) >= 0
                        && Double.parseDouble(firstValueField(root, "rsi")) <= 100,
                "rsi 值应在 [0, 100]");
    }

    @Test
    void probeMacd() throws Exception {
        // MACD(12, 26, 9, close)：三输出（macd/signal；hist 默认不返回，见 macd.md）
        Map<String, Object> extra = Map.of(
                "fast_period", 12,
                "slow_period", 26,
                "signal_period", 9);
        JsonNode root = fetchAndSave("macd", extra);

        assertCommonShape(root, "macd");
        assertNotNull(firstValueField(root, "signal"), "values 应含 signal 字段");
    }

    @Test
    void probeBbands() throws Exception {
        // BBANDS(20, sd=2, SMA, close)：三输出 upper/middle/lower
        Map<String, Object> extra = Map.of(
                "time_period", 20,
                "sd", 2,
                "ma_type", "SMA");
        JsonNode root = fetchAndSave("bbands", extra);

        assertCommonShape(root, "bbands");
        double upper = Double.parseDouble(firstValueField(root, "upper_band"));
        double middle = Double.parseDouble(firstValueField(root, "middle_band"));
        double lower = Double.parseDouble(firstValueField(root, "lower_band"));
        assertTrue(upper > middle && middle > lower, "应满足 upper > middle > lower");
    }

    /**
     * 通用请求参数 + 专有参数合并后 GET，响应 pretty JSON 落盘并返回
     */
    private JsonNode fetchAndSave(String route, Map<String, Object> extraParams) throws Exception {
        // 1. 组装参数：LinkedHashMap 保证落盘文件里能从 params 注释看出请求口径
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", SYMBOL);
        params.put("interval", INTERVAL);
        params.put("outputsize", OUTPUTSIZE);
        params.put("adjust", "none");
        params.put("apikey", twelvedataApiKey);
        params.putAll(extraParams);

        // 2. Twelve Data 鉴权走 query 参数，无需请求头；String.class 拿原始 JSON 文本
        String raw = httpServiceFactory.getHttpService()
                .get(BASE_URL + "/" + route, params, Map.of(), String.class);

        // 3. 解析为树（pretty 输出用），失败即说明响应不是合法 JSON
        JsonNode root = objectMapper.readTree(raw);

        // 4. 落盘：文件头写入请求参数，正文为 pretty JSON
        Files.createDirectories(OUTPUT_DIR);
        StringBuilder out = new StringBuilder();
        out.append("// GET ").append(BASE_URL).append("/").append(route).append('\n');
        out.append("// params: ").append(params.keySet().stream()
                        .map(k -> k + "=" + ("apikey".equals(k) ? "***" : params.get(k)))
                        .reduce((a, b) -> a + "&" + b).orElse(""))
                .append("\n\n");
        out.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        Path target = OUTPUT_DIR.resolve("msft_1day_" + route + ".json");
        Files.writeString(target, out.toString());

        return root;
    }

    /** 公共结构断言：status=ok、values 数组存在且 ≤ outputsize 条 */
    private void assertCommonShape(JsonNode root, String indicator) {
        assertEquals("ok", root.path("status").asText(), indicator + " 应返回 status=ok");
        JsonNode values = root.path("values");
        assertTrue(values.isArray() && !values.isEmpty(), indicator + " values 应非空");
        assertTrue(values.size() <= OUTPUTSIZE, "values 条数不应超过 outputsize");
        assertNotNull(values.get(0).path("datetime"), "每个数据点应有 datetime");
    }

    /** 取首条 value 的指定字段文本（order 默认 desc，首条即最新一根） */
    private String firstValueField(JsonNode root, String field) {
        JsonNode node = root.path("values").get(0).path(field);
        assertNotNull(node, "values[0] 应含字段 " + field);
        return node.asText();
    }
}