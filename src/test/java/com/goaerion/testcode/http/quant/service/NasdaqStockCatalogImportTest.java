package com.goaerion.testcode.http.quant.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.goaerion.testcode.http.quant.entity.TdStockCatalog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * nasdaq_stocks.txt → td_stock_catalog 一次性数据装载测试。
 * <p>
 * 与 {@link StockCatalogServiceTest} 不同：本用例<b>故意不加 @Transactional</b>，
 * 数据真实落库（这正是用例目的）；批量写入走 {@code batchUpsert}（ON CONFLICT DO UPDATE），
 * 幂等可重复执行，重复运行时已有行被新快照覆盖而非报错。
 * <p>
 * 技术要点：{@code batchUpsert} 是自定义 XML 方法，不走 BaseMapper.insert 流程，
 * MyBatis-Plus 的 ASSIGN_ID 主键填充对其不生效，因此 id 必须在装载时手动生成（IdWorker 雪花）。
 * <p>
 * 手动触发方式（依赖内网数据库）：
 * <pre>
 * QUANT_DB_INTEGRATION=true bash mvnw test -Dtest=NasdaqStockCatalogImportTest
 * </pre>
 * 普通的 {@code mvnw test} 中该用例会显示为 skipped。
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "QUANT_DB_INTEGRATION", matches = "true")
@SpringBootTest
class NasdaqStockCatalogImportTest {

    /** 批量 upsert 每批条数 */
    private static final int BATCH_SIZE = 500;

    /** 源文件头 listings=4520，导入后按快照时间核对的预期条数 */
    private static final int EXPECTED_TOTAL = 4520;

    /** 快照时间，取自文件头 fetched=2026-11-05 */
    private static final OffsetDateTime FETCHED_AT = OffsetDateTime.parse("2026-11-05T00:00Z");

    @Autowired
    private StockCatalogService stockCatalogService;

    @Test
    void importNasdaqStocksIntoCatalog() throws Exception {
        // 1. 逐行解析 TSV（跳过 # 注释与 symbol 表头行），分批 upsert
        Path source = Path.of("doc/quant/market_data/nasdaq_stocks.txt");
        List<TdStockCatalog> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0;

        for (String line : Files.readAllLines(source)) {
            if (line.isBlank() || line.startsWith("#") || line.startsWith("symbol\t")) {
                continue;
            }
            String[] cols = line.split("\t", -1);
            if (cols.length < 8) {
                continue;
            }
            batch.add(build(cols));
            if (batch.size() == BATCH_SIZE) {
                total += stockCatalogService.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            total += stockCatalogService.saveAll(batch);
        }

        // 2. 条数核对：upsert 首插与重复执行均返回全部行数（DO UPDATE 命中会计数）
        assertEquals(EXPECTED_TOTAL, total, "受影响行数应等于 listings 数");
        assertEquals(EXPECTED_TOTAL, stockCatalogService.countByFetchedAt(FETCHED_AT),
                "按快照时间统计应等于 listings 数");

        // 3. 抽查一条：字段映射正确性
        TdStockCatalog aapl = stockCatalogService.getBySymbol("AAPL");
        assertNotNull(aapl, "AAPL 应已入库");
        assertEquals("Apple Inc.", aapl.getName());
        assertEquals("NASDAQ", aapl.getExchange());
        assertEquals("XNGS", aapl.getMicCode());
        assertEquals("USD", aapl.getCurrency());
        assertEquals("Common Stock", aapl.getType());
        assertEquals("BBG000B9Y5X2", aapl.getFigiCode());
    }

    /**
     * TSV 8 列 → 实体：symbol/name/currency/exchange/mic_code/country/type/figi_code；
     * 空串转 NULL（figi_code 部分标的为空）；id 手动雪花生成（自定义 upsert 不走 MP 填充）
     */
    private TdStockCatalog build(String[] cols) {
        TdStockCatalog catalog = new TdStockCatalog();
        catalog.setId(IdWorker.getId());
        catalog.setSymbol(cols[0]);
        catalog.setName(blankToNull(cols[1]));
        catalog.setCurrency(blankToNull(cols[2]));
        catalog.setExchange(cols[3]);
        catalog.setMicCode(blankToNull(cols[4]));
        catalog.setCountry(blankToNull(cols[5]));
        catalog.setType(blankToNull(cols[6]));
        catalog.setFigiCode(blankToNull(cols[7]));
        catalog.setFetchedAt(FETCHED_AT);
        return catalog;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}