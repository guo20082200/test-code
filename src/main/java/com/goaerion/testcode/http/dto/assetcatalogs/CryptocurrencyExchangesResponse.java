package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Twelve Data 加密货币交易所（cryptocurrency_exchanges）响应体
 * <p>接口文档见 doc/asset_catalogs/13-加密货币交易所-cryptocurrency_exchanges.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CryptocurrencyExchangesResponse(
        /** 加密货币交易所列表 */
        List<CryptoExchange> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 交易所信息（仅名称一个字段）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CryptoExchange(
            /** 加密货币交易所名称，如 Binance */
            String name) {
    }
}
