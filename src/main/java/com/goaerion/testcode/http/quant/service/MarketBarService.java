package com.goaerion.testcode.http.quant.service;

import com.goaerion.testcode.http.quant.entity.TdBar;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * K 线数据服务：td_bar 的批量入库与查询
 */
public interface MarketBarService {

    /**
     * 批量幂等写入 K 线：以 (symbol, interval, bar_time) 为冲突键，
     * 命中时覆盖 OHLC/复权价/量额（复权修正、历史回补自动覆盖）
     *
     * @param bars K 线列表
     * @return 受影响行数，空列表直接返回 0
     */
    int saveBars(List<TdBar> bars);

    /**
     * 取最近 N 根 K 线，按时间正序返回（可直接用于画图/算指标）
     */
    List<TdBar> recentBars(String symbol, String interval, int limit);

    /**
     * 查询时间范围内的 K 线（左闭右闭），按时间正序返回
     */
    List<TdBar> barsBetween(String symbol, String interval,
                            OffsetDateTime start, OffsetDateTime end);

    /**
     * 取最新一根 K 线，用于判断数据新鲜度
     *
     * @return 最新 K 线，无数据返回 NULL
     */
    TdBar latestBar(String symbol, String interval);
}