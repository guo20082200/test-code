package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Twelve Data 交易所目录（exchanges）响应体
 * <p>接口文档见 doc/asset_catalogs/11-交易所目录-exchanges.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangesResponse(
        /** 交易所列表 */
        List<ExchangeInfo> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 交易所信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExchangeInfo(
            /** 交易所全称 */
            String title,
            /** 交易所名称（简称） */
            String name,
            /** 市场识别码（MIC，ISO 10383 标准） */
            String code,
            /** 交易所所属国家 */
            String country,
            /** 交易所所在时区 */
            String timezone,
            /** 套餐可用信息（仅 show_plan=true 时返回） */
            AssetCatalogResponse.AccessPlan access) {
    }
}
