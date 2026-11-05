package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 资产目录通用响应体（stocks / etfs 共用）
 * <p>响应结构：count + data + status
 * <p>接口文档见 doc/asset_catalogs/01-股票目录-stocks.md、04-ETF目录-etfs.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetCatalogResponse(
        /** 返回的记录数量 */
        Integer count,
        /** 标的列表 */
        List<Instrument> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 通用标的信息（stocks 含 type 字段，etfs 无此字段时为 null）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Instrument(
            /** 标的代码（Ticker） */
            String symbol,
            /** 标的全称 */
            String name,
            /** 计价币种（ISO 4217） */
            String currency,
            /** 上市交易所 */
            String exchange,
            /** 市场识别码（MIC） */
            @JsonProperty("mic_code") String micCode,
            /** 所在国家 */
            String country,
            /** 证券类型（stocks 有，etfs 无） */
            String type,
            /** 金融工具全球标识符（FIGI） */
            @JsonProperty("figi_code") String figiCode,
            /** 金融工具分类码（CFI） */
            @JsonProperty("cfi_code") String cfiCode,
            /** 国际证券识别码（ISIN，需向客服单独申请开通） */
            String isin,
            /** 美国证券统一识别码（9 位字母数字） */
            String cusip,
            /** 套餐可用信息（仅 show_plan=true 时返回） */
            AccessPlan access) {
    }

    /**
     * 套餐可用信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccessPlan(
            /** 该标的的访问级别 */
            String global,
            /** 个人套餐名称 */
            String plan,
            /** 企业套餐名称 */
            @JsonProperty("plan_business") String planBusiness) {
    }
}
