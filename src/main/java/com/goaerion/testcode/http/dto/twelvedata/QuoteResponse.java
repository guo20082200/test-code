package com.goaerion.testcode.http.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Twelve Data quote（实时报价）接口响应体（扁平结构，无 meta/values 包装）
 * <p>接口文档见 doc/TwelveData-quote接口文档.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuoteResponse(
        /** 标的代码，如 AAPL */
        String symbol,
        /** 标的名称 */
        String name,
        /** 交易所名称，如 NASDAQ */
        String exchange,
        /** 交易所市场识别码（MIC） */
        @JsonProperty("mic_code") String micCode,
        /** 计价币种，如 USD */
        String currency,
        /** 当前 K 线开盘时刻（按指定时区显示） */
        String datetime,
        /** 当前 K 线开盘时刻的 Unix 时间戳 */
        Integer timestamp,
        /** 最后一分钟 K 线的 Unix 时间戳 */
        @JsonProperty("last_quote_at") Integer lastQuoteAt,
        /** 开盘价 */
        String open,
        /** 最高价 */
        String high,
        /** 最低价 */
        String low,
        /** 收盘价（即最新价） */
        String close,
        /** 成交量（并非所有标的有此字段） */
        String volume,
        /** 前一根 K 线收盘价 */
        @JsonProperty("previous_close") String previousClose,
        /** 涨跌额：close - previous_close */
        String change,
        /** 涨跌百分比 */
        @JsonProperty("percent_change") String percentChange,
        /** 平均成交量（并非所有标的有此字段） */
        @JsonProperty("average_volume") String averageVolume,
        /** 市场是否开盘中（true 开市 / false 休市） */
        @JsonProperty("is_market_open") Boolean isMarketOpen,
        /** 52 周指标 */
        @JsonProperty("fifty_two_week") FiftyTwoWeek fiftyTwoWeek) {

    /**
     * 52 周高低指标
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FiftyTwoWeek(
            /** 52 周最低价 */
            String low,
            /** 52 周最高价 */
            String high,
            /** 当前价 − 52 周最低价 */
            @JsonProperty("low_change") String lowChange,
            /** 当前价 − 52 周最高价 */
            @JsonProperty("high_change") String highChange,
            /** 相对 52 周最低价的涨跌百分比 */
            @JsonProperty("low_change_percent") String lowChangePercent,
            /** 相对 52 周最高价的涨跌百分比 */
            @JsonProperty("high_change_percent") String highChangePercent,
            /** 52 周最低价到最高价的区间字符串 */
            String range) {
    }
}
