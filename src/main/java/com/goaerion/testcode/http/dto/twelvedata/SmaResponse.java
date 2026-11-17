package com.goaerion.testcode.http.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Twelve Data sma（简单移动平均线）接口响应体
 * <p>接口文档见 doc/quant/technical-indicators/01-overlap-studies/sma.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SmaResponse(
        /** 请求元信息 */
        Meta meta,
        /** SMA 时间序列数据点列表 */
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
            String type,
            /** 指标计算信息 */
            Indicator indicator) {
    }

    /**
     * 指标计算信息（回显本次计算所用的参数）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Indicator(
            /** 技术指标名称，如 SMA - Simple Moving Average */
            String name,
            /** 计算所用价格类型，如 close */
            @JsonProperty("series_type") String seriesType,
            /** 计算周期数 */
            @JsonProperty("time_period") Integer timePeriod) {
    }

    /**
     * 单个数据点
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(
            /** K 线开盘时刻（股票为交易所本地时间，外汇/加密货币为 UTC） */
            String datetime,
            /** SMA 值 */
            String sma,
            /** 开盘价（include_ohlc=true 时返回） */
            String open,
            /** 最高价（include_ohlc=true 时返回） */
            String high,
            /** 最低价（include_ohlc=true 时返回） */
            String low,
            /** 收盘价（include_ohlc=true 时返回） */
            String close,
            /** 成交量（include_ohlc=true 时返回） */
            String volume) {
    }
}