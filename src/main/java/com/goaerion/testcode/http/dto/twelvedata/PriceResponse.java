package com.goaerion.testcode.http.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Twelve Data price（最新价）接口响应体（扁平结构，仅一个字段）
 * <p>接口文档见 doc/TwelveData-price接口文档.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceResponse(
        /** 实时（或最近一次可用的）成交价格，字符串形式，使用时需转数值 */
        String price) {
}
