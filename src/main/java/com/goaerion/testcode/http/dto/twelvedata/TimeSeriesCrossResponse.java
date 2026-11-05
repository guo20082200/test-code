package com.goaerion.testcode.http.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Twelve Data time_series/cross（交叉汇率时间序列）接口响应体
 * <p>接口文档见 doc/TwelveData-time_series_cross接口文档.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeSeriesCrossResponse(
        /** 请求元信息 */
        Meta meta,
        /** 时间序列数据点列表（按时间降序） */
        java.util.List<Value> values,
        /** 响应状态，如 ok / error */
        String status) {

    /**
     * 交叉行情元信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            /** 基础标的代码，如 JPY/USD */
            @JsonProperty("base_instrument") String baseInstrument,
            /** 基础货币 */
            @JsonProperty("base_currency") String baseCurrency,
            /** 基础标的所在交易所，如 PHYSICAL CURRENCY */
            @JsonProperty("base_exchange") String baseExchange,
            /** 数据点时间间隔，如 1min */
            String interval,
            /** 报价标的代码，如 BTC/USD */
            @JsonProperty("quote_instrument") String quoteInstrument,
            /** 报价货币 */
            @JsonProperty("quote_currency") String quoteCurrency,
            /** 报价标的所在交易所，如 Coinbase Pro */
            @JsonProperty("quote_exchange") String quoteExchange) {
    }

    /**
     * 单个时间序列数据点（一根 K 线，交叉行情不含成交量）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(
            /** K 线开盘时刻（交易所本地时间） */
            String datetime,
            /** 开盘价 */
            String open,
            /** 最高价 */
            String high,
            /** 最低价 */
            String low,
            /** 收盘价 */
            String close) {
    }
}
