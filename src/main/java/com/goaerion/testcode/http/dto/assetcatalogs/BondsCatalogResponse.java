package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 债券目录（bonds）响应体
 * <p>注意：数据包裹在 result 对象中（result.count + result.list），与 funds 相同
 * <p>接口文档见 doc/asset_catalogs/07-债券目录-bonds.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BondsCatalogResponse(
        /** 结果包装对象 */
        Result result,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 结果包装对象
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            /** 匹配的标的总数 */
            Integer count,
            /** 债券列表 */
            List<Bond> list) {
    }

    /**
     * 债券（无 figi/isin/cusip 标识符字段）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bond(
            /** 债券代码 */
            String symbol,
            /** 债券全称 */
            String name,
            /** 所在国家 */
            String country,
            /** 计价币种（ISO 4217） */
            String currency,
            /** 交易场所 */
            String exchange,
            /** 市场识别码（MIC） */
            @JsonProperty("mic_code") String micCode,
            /** 债券类型 */
            String type,
            /** 套餐可用信息（仅 show_plan=true 时返回） */
            AssetCatalogResponse.AccessPlan access) {
    }
}
