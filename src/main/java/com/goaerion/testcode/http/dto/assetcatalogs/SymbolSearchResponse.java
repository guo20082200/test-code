package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 代码搜索（symbol_search）响应体
 * <p>接口文档见 doc/asset_catalogs/08-代码搜索-symbol_search.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SymbolSearchResponse(
        /** 匹配搜索条件的标的列表（按相关度排序，最相关在前） */
        List<SearchItem> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 搜索结果条目
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchItem(
            /** 标的代码（Ticker） */
            String symbol,
            /** 标的名称 */
            @JsonProperty("instrument_name") String instrumentName,
            /** 交易场所 */
            String exchange,
            /** 市场识别码（MIC，ISO 10383 标准） */
            @JsonProperty("mic_code") String micCode,
            /** 交易所所在时区 */
            @JsonProperty("exchange_timezone") String exchangeTimezone,
            /** 工具类型，如 Common Stock */
            @JsonProperty("instrument_type") String instrumentType,
            /** 交易所所属国家 */
            String country,
            /** 交易计价币种 */
            String currency,
            /** 套餐可用信息（仅 show_plan=true 时返回） */
            AssetCatalogResponse.AccessPlan access) {
    }
}
