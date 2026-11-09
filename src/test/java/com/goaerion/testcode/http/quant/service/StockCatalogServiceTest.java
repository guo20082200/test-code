package com.goaerion.testcode.http.quant.service;

import com.goaerion.testcode.http.quant.entity.TdStockCatalog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * td_stock_catalog 目录服务集成测试：连接真实 PostgreSQL（application.yml 数据源），
 * 重点验证批量 upsert 的幂等语义（首插 + 重复导入覆盖）与维度查询。
 * <p>
 * 测试类加 {@code @Transactional}，用例结束后事务自动回滚，不残留测试数据；
 * 测试标的统一使用 UTCAT 前缀，避免与真实数据在唯一键上冲突。
 * <p>
 * 日常构建不运行（依赖内网数据库），手动触发方式：
 * <pre>
 * QUANT_DB_INTEGRATION=true bash mvnw test -Dtest=StockCatalogServiceTest
 * </pre>
 * 普通的 {@code mvnw test} 中该用例会显示为 skipped。
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "QUANT_DB_INTEGRATION", matches = "true")
@SpringBootTest
@Transactional
class StockCatalogServiceTest {

    /** 测试标的统一前缀，断言/圈定范围用 */
    private static final String PREFIX = "UTCAT";

    /** 快照时间：固定值便于 countByFetchedAt 精确核对 */
    private static final OffsetDateTime FETCHED_AT = OffsetDateTime.parse("2026-11-05T00:00Z");

    @Autowired
    private StockCatalogService stockCatalogService;

    @Test
    void batchUpsertIdempotentThenQuery() {
        // 1. 首次导入 3 条：id 不设置，由 ASSIGN_ID（雪花）应用侧生成
        stockCatalogService.saveAll(buildBatch("Common Stock"));

        // 2. id 回填校验
        TdStockCatalog first = stockCatalogService.getBySymbol(PREFIX + "1");
        assertNotNull(first, "首插后应能查到记录");
        assertNotNull(first.getId(), "插入后 id 应被回填");
        assertEquals("Apple Inc.", first.getName());
        assertEquals("XNGS", first.getMicCode());
        assertNull(first.getFigiCode(), "源数据 figi_code 为空应存 NULL");

        // 3. 重复导入：同冲突键 (symbol, exchange, mic_code)，name/type 变化应被覆盖，不产生新行
        List<TdStockCatalog> updated = buildBatch("Warrant");
        updated.get(0).setName("Apple Inc. (Updated)");
        updated.get(2).setFigiCode("BBG000B9Y5X2");
        stockCatalogService.saveAll(updated);

        // 4. 幂等校验：仍为 3 条
        assertEquals(3, stockCatalogService.countByFetchedAt(FETCHED_AT), "重复导入不应产生新行");

        // 5. 覆盖校验：name 更新生效、figi_code 补齐生效、type 整体被新快照覆盖
        TdStockCatalog reloaded = stockCatalogService.getBySymbol(PREFIX + "1");
        assertEquals("Apple Inc. (Updated)", reloaded.getName());
        assertEquals("Warrant", reloaded.getType());
        assertEquals("BBG000B9Y5X2", stockCatalogService.getBySymbol(PREFIX + "3").getFigiCode());

        // 6. 维度查询：按市场、按类型
        assertEquals(2, stockCatalogService.listByMic("XNGS").size(), "XNGS 应有 2 条");
        assertEquals(3, stockCatalogService.listByType("Warrant").size(), "覆盖后全部为 Warrant");
        assertNull(stockCatalogService.getBySymbol("NO_SUCH_SYMBOL_XYZ"), "不存在的标的应返回 NULL");
    }

    /**
     * 构造 3 条测试目录：UTCAT1（XNGS）/ UTCAT2（XNCM）/ UTCAT3（XNGS）
     */
    private List<TdStockCatalog> buildBatch(String type) {
        List<TdStockCatalog> list = new ArrayList<>();
        list.add(build(PREFIX + "1", "Apple Inc.", "USD", "NASDAQ", "XNGS", "United States", type, null));
        list.add(build(PREFIX + "2", "Aardvark Therapeutics, Inc.", "USD", "NASDAQ", "XNCM", "United States", type, null));
        list.add(build(PREFIX + "3", "Acadia Pharmaceuticals Inc.", "USD", "NASDAQ", "XNGS", "United States", type, "BBG000BHGMW8"));
        return list;
    }

    private TdStockCatalog build(String symbol, String name, String currency, String exchange,
                                 String micCode, String country, String type, String figiCode) {
        TdStockCatalog catalog = new TdStockCatalog();
        catalog.setSymbol(symbol);
        catalog.setName(name);
        catalog.setCurrency(currency);
        catalog.setExchange(exchange);
        catalog.setMicCode(micCode);
        catalog.setCountry(country);
        catalog.setType(type);
        catalog.setFigiCode(figiCode);
        catalog.setFetchedAt(FETCHED_AT);
        assertTrue(catalog.getId() == null, "id 应由应用侧生成");
        return catalog;
    }
}