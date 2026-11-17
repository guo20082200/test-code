package com.goaerion.testcode.http.dto.twelvedata;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Twelve Data 技术指标通用响应体
 * <p>102 个技术指标端点的响应结构一致：meta + values + status，
 * 区别仅在于 values 中每个数据点的字段名（如 sma/ema/rsi/macd 等）
 * 与 meta.indicator 回显的计算参数。因此用 Map 承载可变字段，一个 DTO 覆盖全部指标。
 * <p>各指标文档见 doc/quant/technical-indicators/（文件名与 API 路由一致）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IndicatorResponse(
        /** 请求元信息，各指标统一结构，indicator 子对象回显本次计算所用参数 */
        Map<String, Object> meta,
        /** 时间序列数据点列表，每个数据点的字段名随指标不同（如 sma、upper_band、macd_hist 等） */
        List<Map<String, Object>> values,
        /** 响应状态，如 ok / error */
        String status,
        /** 错误码（仅出错时返回，如 429 配额超限） */
        Integer code,
        /** 错误信息（仅出错时返回） */
        String message) {
}