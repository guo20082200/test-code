package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 加密货币交易对目录（cryptocurrencies）响应体
 * <p>接口文档见 doc/asset_catalogs/03-加密货币交易对-cryptocurrencies.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CryptocurrenciesCatalogResponse(
        /** 返回的记录数量 */
        Integer count,
        /** 加密货币交易对列表 */
        List<CryptoPair> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 加密货币交易对
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CryptoPair(
            /** 交易对代码（斜杠分隔），如 BTC/USD */
            String symbol,
            /** 可交易的交易所列表 */
            @JsonProperty("available_exchanges") List<String> availableExchanges,
            /** 基础货币名称，如 Bitcoin */
            @JsonProperty("currency_base") String currencyBase,
            /** 报价货币名称，如 US Dollar */
            @JsonProperty("currency_quote") String currencyQuote) {
    }
}
