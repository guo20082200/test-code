package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.config.HttpClientProvider;
import com.goaerion.testcode.http.dto.assetcatalogs.AssetCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.BondsCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CommoditiesCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CryptocurrenciesCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.ForexPairsCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.FundCatalogResponse;
import com.goaerion.testcode.http.service.HttpService;
import com.goaerion.testcode.http.service.impl.RestClientHttpServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Twelve Data 资产目录闭环集成测试：真实发出 HTTP 请求，验证 7 个目录端点全链路可用
 * （不是验证参数拼装，而是验证真实响应的业务语义与 DTO 反序列化契约）。
 * <p>
 * 日常构建不运行（消耗 API 积分、依赖外网），手动触发方式：
 * <pre>
 * TWELVEDATA_INTEGRATION=true bash mvnw test -Dtest=AssetCatalogsIntegrationTest
 * </pre>
 * 普通的 {@code mvnw test} 中该用例会显示为 skipped。
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "TWELVEDATA_INTEGRATION", matches = "true")
class AssetCatalogsIntegrationTest {

    private static final String API_KEY = "7eccf8efb5cf4a2397282c118df0306b";

    private static HttpService httpService;

    @BeforeAll
    static void setUp() {
        // 组装真实依赖（不走 Spring，等价于生产环境的 bean 组装方式）
        HttpClientProvider provider = new HttpClientProvider(false, Duration.ofSeconds(10), Duration.ofSeconds(30));
        httpService = new RestClientHttpServiceImpl(provider, List.of());
    }

    private Map<String, Object> params(Map<String, Object> specific) {
        Map<String, Object> params = new LinkedHashMap<>(specific);
        params.put("apikey", API_KEY);
        return params;
    }

    @Test
    void stocksCatalog() {
        AssetCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/stocks",
                params(Map.of("exchange", "NASDAQ", "outputsize", 10)),
                Map.of(), AssetCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        assertNotNull(response.count());
        assertTrue(response.count() > 0, "NASDAQ 目录不应为空");
        assertFalse(response.data().isEmpty());
        // 全部记录的交易所均为 NASDAQ（验证过滤参数真实生效）
        assertTrue(response.data().stream().allMatch(i -> "NASDAQ".equals(i.exchange())),
                "按 exchange=NASDAQ 过滤后所有记录的交易所应为 NASDAQ");
        // 每条记录的核心字段均非空
        response.data().forEach(i -> {
            assertNotNull(i.symbol());
            assertNotNull(i.name());
            assertNotNull(i.currency());
        });
    }

    @Test
    void forexPairsCatalog() {
        ForexPairsCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/forex_pairs",
                params(Map.of("currency_base", "EUR", "outputsize", 10)),
                Map.of(), ForexPairsCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        assertTrue(response.count() > 0, "EUR 为基础货币的货币对不应为空");
        assertFalse(response.data().isEmpty());
        // 过滤条件真实生效：symbol 均以 EUR/ 开头（注意：currency_base 返回的是货币全名 Euro，而非代码 EUR）
        response.data().forEach(p -> {
            assertTrue(p.symbol().startsWith("EUR/"), "过滤 currency_base=EUR 后 symbol 应以 EUR/ 开头，实际: " + p.symbol());
            assertTrue(p.symbol().contains("/"), "货币对代码应以斜杠分隔，实际: " + p.symbol());
            // 组别应为四种合法取值之一
            assertTrue(List.of("Major", "Minor", "Exotic", "Exotic-Cross").contains(p.currencyGroup()),
                    "非法货币组别: " + p.currencyGroup());
        });
    }

    @Test
    void cryptocurrenciesCatalog() {
        CryptocurrenciesCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/cryptocurrencies",
                params(Map.of("symbol", "BTC/USD")),
                Map.of(), CryptocurrenciesCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        assertFalse(response.data().isEmpty(), "BTC/USD 交易对不应为空");
        // 过滤条件真实生效：所有记录 symbol 均为 BTC/USD，且至少有一个可用交易所
        response.data().forEach(p -> {
            assertEquals("BTC/USD", p.symbol());
            assertFalse(p.availableExchanges().isEmpty(), "可用交易所列表不应为空");
            assertEquals("Bitcoin", p.currencyBase());
        });
    }

    @Test
    void etfsCatalog() {
        AssetCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/etfs",
                params(Map.of("symbol", "SPY")),
                Map.of(), AssetCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        assertFalse(response.data().isEmpty(), "SPY 目录记录不应为空");
        AssetCatalogResponse.Instrument spy = response.data().get(0);
        assertEquals("SPY", spy.symbol());
        // 注意：交易所可能是 IEX/NYSE 等多个上市场所之一（文档示例的 NYSE 已过时），只断言非空
        assertNotNull(spy.exchange());
        assertNotNull(spy.name());
        // 标识符字段应成功反序列化（验证 @JsonProperty 映射契约）
        // 注意：isin 需 ISIN Data 付费附加包，免费套餐返回占位符 request_access_via_add_ons，只断言非空
        assertNotNull(spy.isin());
        assertNotNull(spy.figiCode());
        assertNotNull(spy.cfiCode());
    }

    @Test
    void fundsCatalog() {
        FundCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/funds",
                params(Map.of("symbol", "FXAIX")),
                Map.of(), FundCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        // funds 响应包裹在 result 对象中（与其他目录接口不同）
        assertNotNull(response.result(), "funds 响应应有 result 包装对象");
        assertTrue(response.result().count() > 0);
        assertFalse(response.result().list().isEmpty());
        assertEquals("FXAIX", response.result().list().get(0).symbol());
    }

    @Test
    void commoditiesCatalog() {
        CommoditiesCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/commodities",
                params(Map.of("category", "Precious Metal", "outputsize", 10)),
                Map.of(), CommoditiesCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        assertFalse(response.data().isEmpty(), "大宗商品目录不应为空");
        // 注意：实测 category 过滤参数不严格生效（可能返回其他类别记录），只断言字段完整非空
        response.data().forEach(c -> {
            assertNotNull(c.symbol());
            assertNotNull(c.name());
            assertNotNull(c.category());
            assertNotNull(c.description());
        });
    }

    @Test
    void bondsCatalog() {
        BondsCatalogResponse response = httpService.get(
                "https://api.twelvedata.com/bonds",
                params(Map.of("symbol", "US2Y")),
                Map.of(), BondsCatalogResponse.class);

        assertNotNull(response);
        assertEquals("ok", response.status());
        // bonds 响应包裹在 result 对象中（与 funds 相同）
        assertNotNull(response.result(), "bonds 响应应有 result 包装对象");
        assertTrue(response.result().count() > 0);
        assertFalse(response.result().list().isEmpty());
        BondsCatalogResponse.Bond us2y = response.result().list().get(0);
        assertEquals("US2Y", us2y.symbol());
        assertEquals("USD", us2y.currency());
    }
}
