package com.goaerion.testcode.http.quant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.goaerion.testcode.http.quant.entity.TdBar;
import com.goaerion.testcode.http.quant.storage.TdBarMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * msft_1day.txt → td_bar 一次性数据装载测试。
 * <p>
 * 与回滚式测试不同：本用例<b>故意不加 @Transactional</b>，数据真实落库（这正是用例目的）；
 * 写入走 {@code batchUpsert}（ON CONFLICT (symbol, "interval", bar_time) DO UPDATE），
 * 幂等可重复执行，重复运行覆盖行情列、保留首插 id。
 * <p>
 * 数据口径（取自文件头注释）：MSFT / interval=1day / NASDAQ / currency=USD /
 * timezone=America/New_York / bars=5000；日线 datetime 为交易所交易日，
 * 按交易所时区 0 点转 UTC 入库（冬令时 EST=UTC-5，夏令时自动处理）。
 * <p>
 * 技术要点：td_bar.id 无默认值，batchUpsert 为自定义 XML 方法、不走 BaseMapper 填充，
 * id 需用 IdWorker 雪花手动生成。
 * <p>
 * 手动触发方式（依赖内网数据库）：
 * <pre>
 * QUANT_DB_INTEGRATION=true mvn test -Dtest=MsftDailyBarImportTest
 * </pre>
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "QUANT_DB_INTEGRATION", matches = "true")
@SpringBootTest
class MsftDailyBarImportTest {

    /** 批量 upsert 每批条数 */
    private static final int BATCH_SIZE = 500;

    /** 文件头 bars=5000，导入后库内 (MSFT, 1day) 的预期条数 */
    private static final int EXPECTED_BARS = 5000;

    private static final String SYMBOL = "MSFT";
    private static final String INTERVAL = "1day";

    /** 文件头 timezone=America/New_York：日线按交易所日期 0 点对齐后转 UTC */
    private static final ZoneId EXCHANGE_ZONE = ZoneId.of("America/New_York");

    @Autowired
    private MarketBarService marketBarService;

    @Autowired
    private TdBarMapper tdBarMapper;

    @Test
    void importMsftDailyBars() throws Exception {
        // 1. 逐行解析 TSV（跳过 # 注释与 datetime 表头行），分批 upsert
        Path source = Path.of("doc/quant/market_data/msft_1day.txt");
        List<TdBar> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0;

        for (String line : Files.readAllLines(source)) {
            if (line.isBlank() || line.startsWith("#") || line.startsWith("datetime\t")) {
                continue;
            }
            String[] cols = line.trim().split("\t", -1);
            if (cols.length < 6) {
                continue;
            }
            batch.add(build(cols));
            if (batch.size() == BATCH_SIZE) {
                total += marketBarService.saveBars(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            total += marketBarService.saveBars(batch);
        }

        // 2. 条数核对：upsert 首插与重复执行均返回全部行数（DO UPDATE 命中会计数）
        assertEquals(EXPECTED_BARS, total, "受影响行数应等于 bars=5000");
        assertEquals(EXPECTED_BARS, tdBarMapper.selectCount(new LambdaQueryWrapper<TdBar>()
                        .eq(TdBar::getSymbol, SYMBOL)
                        .eq(TdBar::getInterval, INTERVAL)),
                "库内 (MSFT, 1day) 应有 5000 根");

        // 3. 抽查最新一根（文件首行 2026-11-04，冬令时 0 点 = 05:00Z）
        TdBar latest = marketBarService.latestBar(SYMBOL, INTERVAL);
        assertNotNull(latest, "MSFT 日线应已入库");
        assertEquals(Instant.parse("2026-11-04T05:00:00Z"), latest.getBarTime().toInstant());
        assertEquals(0, latest.getClose().compareTo(new BigDecimal("555.79999")));
        assertEquals(0, latest.getOpen().compareTo(new BigDecimal("543.70001")));
        assertEquals(0, latest.getVolume().compareTo(new BigDecimal("50759800")));
        assertEquals("USD", latest.getCurrency());

        // 4. 抽查最早一根（文件末行 2006-12-20，约 20 年日线）
        Instant firstDay = LocalDate.parse("2006-12-20").atStartOfDay(EXCHANGE_ZONE).toInstant();
        List<TdBar> earliest = marketBarService.barsBetween(SYMBOL, INTERVAL,
                firstDay.atZone(EXCHANGE_ZONE).toOffsetDateTime(),
                firstDay.atZone(EXCHANGE_ZONE).toOffsetDateTime());
        assertEquals(1, earliest.size(), "2006-12-20 应恰好有 1 根");
        assertEquals(0, earliest.get(0).getOpen().compareTo(new BigDecimal("29.99000")));
        assertEquals(0, earliest.get(0).getClose().compareTo(new BigDecimal("30.09000")));

        // 5. id 为应用侧雪花生成（外部指定主键约定）
        assertNotNull(latest.getId(), "id 应随 upsert 写入");
    }

    /**
     * TSV 6 列 → 实体：datetime/open/high/low/close/volume；
     * 本文件无复权价与成交额（credits=1 未复权口径），adj_close/amount 留空
     */
    private TdBar build(String[] cols) {
        TdBar bar = new TdBar();
        bar.setId(IdWorker.getId());
        bar.setSymbol(SYMBOL);
        bar.setInterval(INTERVAL);
        bar.setBarTime(LocalDate.parse(cols[0]).atStartOfDay(EXCHANGE_ZONE).toOffsetDateTime());
        bar.setOpen(new BigDecimal(cols[1]));
        bar.setHigh(new BigDecimal(cols[2]));
        bar.setLow(new BigDecimal(cols[3]));
        bar.setClose(new BigDecimal(cols[4]));
        bar.setVolume(new BigDecimal(cols[5]));
        bar.setCurrency("USD");
        return bar;
    }
}