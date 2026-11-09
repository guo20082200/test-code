package com.goaerion.testcode.http.quant.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.goaerion.testcode.http.quant.entity.TdSymbol;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * td_symbol 批量写入集成测试：连接真实 PostgreSQL（application.yml 数据源），验证全链路入库。
 * <p>
 * 测试类加 {@code @Transactional}，用例结束后事务自动回滚，不残留测试数据；
 * 测试标的统一使用 UTTD 前缀，避免与真实数据在唯一键 uk_td_symbol 上冲突。
 * <p>
 * 日常构建不运行（依赖内网数据库），手动触发方式：
 * <pre>
 * QUANT_DB_INTEGRATION=true bash mvnw test -Dtest=TdSymbolMapperBatchInsertTest
 * </pre>
 * 普通的 {@code mvnw test} 中该用例会显示为 skipped。
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "QUANT_DB_INTEGRATION", matches = "true")
@SpringBootTest
@Transactional
class TdSymbolMapperBatchInsertTest {

    /** 本用例批量写入的标的条数 */
    private static final int BATCH_SIZE = 5;

    /** 测试标的统一前缀，清理/断言时据此圈定范围 */
    private static final String TEST_PREFIX = "UTTD";

    @Autowired
    private TdSymbolMapper tdSymbolMapper;

    @Test
    void batchInsertThenSelectEnabled() {
        // 1. 构造批量数据：id 不设置，由 ASSIGN_ID（雪花）应用侧生成，匹配表无自增默认值的约定
        List<TdSymbol> symbols = new ArrayList<>();
        for (int i = 1; i <= BATCH_SIZE; i++) {
            TdSymbol symbol = new TdSymbol();
            symbol.setSymbol(TEST_PREFIX + i);
            symbol.setExchange("NASDAQ");
            symbol.setMicCode("XNGS");
            symbol.setAssetType("stock");
            symbol.setInstrumentType("Common Stock");
            symbol.setCurrency("USD");
            symbol.setExchangeTimezone("America/New_York");
            symbol.setIntervalSet("1day");
            symbol.setEnabled(Boolean.TRUE);
            symbols.add(symbol);
        }

        // 2. 批量写入：BaseMapper 无原生 insertBatch，同一事务内循环 insert 即为一次批量提交
        symbols.forEach(tdSymbolMapper::insert);

        // 3. 校验 id 均由应用侧生成回填（int8 装得下雪花值）
        symbols.forEach(s -> assertNotNull(s.getId(), "插入后 id 应被回填"));

        // 4. 校验入库条数：按测试前缀精确统计
        Long count = tdSymbolMapper.selectCount(
                new LambdaQueryWrapper<TdSymbol>().likeRight(TdSymbol::getSymbol, TEST_PREFIX));
        assertEquals(BATCH_SIZE, count, "批量写入后应能查到等量记录");

        // 5. 校验自定义查询 selectEnabledSymbols：新写入的 enabled=TRUE 标的应出现在结果中
        List<TdSymbol> enabledSymbols = tdSymbolMapper.selectEnabledSymbols();
        assertTrue(enabledSymbols.stream().anyMatch(s -> (TEST_PREFIX + 1).equals(s.getSymbol())),
                "selectEnabledSymbols 应包含刚写入的测试标的");

        // 6. 抽查一条记录的字段映射（下划线列 ↔ 驼峰属性）
        TdSymbol loaded = tdSymbolMapper.selectOne(
                new LambdaQueryWrapper<TdSymbol>().eq(TdSymbol::getSymbol, TEST_PREFIX + 1));
        assertNotNull(loaded);
        assertEquals("XNGS", loaded.getMicCode());
        assertEquals("America/New_York", loaded.getExchangeTimezone());
        assertEquals("1day", loaded.getIntervalSet());
    }
}