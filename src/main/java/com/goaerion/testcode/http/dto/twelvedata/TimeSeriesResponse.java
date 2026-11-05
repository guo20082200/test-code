package com.goaerion.testcode.http.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Twelve Data time_series（时间序列）接口响应体
 * <p>接口文档见 doc/TwelveData-time_series接口文档.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeSeriesResponse(
        /** 请求元信息 */
        Meta meta,
        /** 时间序列数据点列表 */
        java.util.List<Value> values,
        /** 响应状态，如 ok / error */
        String status) {

    /**
     * 标的元信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            /** 标的代码，如 AAPL */
            String symbol,
            /** 数据点时间间隔，如 1min */
            String interval,
            /** 交易币种，如 USD */
            String currency,
            /** 交易所所在时区 */
            @JsonProperty("exchange_timezone") String exchangeTimezone,
            /** 交易所名称，如 NASDAQ */
            String exchange,
            /** 交易所市场识别码（MIC） */
            @JsonProperty("mic_code") String micCode,
            /** 资产类别，如 Common Stock */
            String type) {
    }

    /**
     * 单个时间序列数据点（一根 K 线）
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
            String close,
            /** 成交量（部分标的无此字段） */
            String volume) {
    }
}
