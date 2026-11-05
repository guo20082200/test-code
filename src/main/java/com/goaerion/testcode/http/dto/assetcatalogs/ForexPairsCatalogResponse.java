package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 外汇交易对目录（forex_pairs）响应体
 * <p>接口文档见 doc/asset_catalogs/02-外汇交易对-forex_pairs.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ForexPairsCatalogResponse(
        /** 返回的记录数量 */
        Integer count,
        /** 外汇货币对列表 */
        List<ForexPair> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 外汇货币对
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForexPair(
            /** 货币对代码（ISO 4217，斜杠分隔），如 EUR/USD */
            String symbol,
            /** 货币对组别：Major（主要）/ Minor（次要）/ Exotic（奇异）/ Exotic-Cross（奇异交叉） */
            @JsonProperty("currency_group") String currencyGroup,
            /** 基础货币名称（ISO 4217） */
            @JsonProperty("currency_base") String currencyBase,
            /** 报价货币名称（ISO 4217） */
            @JsonProperty("currency_quote") String currencyQuote) {
    }
}
