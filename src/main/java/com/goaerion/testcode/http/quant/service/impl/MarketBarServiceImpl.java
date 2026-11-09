package com.goaerion.testcode.http.quant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.goaerion.testcode.http.quant.entity.TdBar;
import com.goaerion.testcode.http.quant.storage.TdBarMapper;
import com.goaerion.testcode.http.quant.service.MarketBarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * K 线数据服务实现
 */
@Service
@RequiredArgsConstructor
public class MarketBarServiceImpl implements MarketBarService {

    private final TdBarMapper tdBarMapper;

    @Override
    public int saveBars(List<TdBar> bars) {
        if (bars == null || bars.isEmpty()) {
            return 0;
        }
        return tdBarMapper.batchUpsert(bars);
    }

    @Override
    public List<TdBar> recentBars(String symbol, String interval, int limit) {
        return tdBarMapper.selectRecentBars(symbol, interval, limit);
    }

    @Override
    public List<TdBar> barsBetween(String symbol, String interval,
                                   OffsetDateTime start, OffsetDateTime end) {
        return tdBarMapper.selectList(new LambdaQueryWrapper<TdBar>()
                .eq(TdBar::getSymbol, symbol)
                .eq(TdBar::getInterval, interval)
                .ge(TdBar::getBarTime, start)
                .le(TdBar::getBarTime, end)
                .orderByAsc(TdBar::getBarTime));
    }

    @Override
    public TdBar latestBar(String symbol, String interval) {
        List<TdBar> bars = tdBarMapper.selectRecentBars(symbol, interval, 1);
        return bars.isEmpty() ? null : bars.get(0);
    }
}