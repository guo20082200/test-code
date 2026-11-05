package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 大宗商品目录（commodities）响应体
 * <p>接口文档见 doc/asset_catalogs/06-大宗商品目录-commodities.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CommoditiesCatalogResponse(
        /** 返回的记录数量 */
        Integer count,
        /** 大宗商品列表 */
        List<Commodity> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 大宗商品
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Commodity(
            /** 商品代码，如 XAU/USD、WTI/USD、HG1 */
            String symbol,
            /** 商品全称，如 Gold Spot（黄金现货） */
            String name,
            /** 商品类别，如 Precious Metal（贵金属） */
            String category,
            /** 商品简短描述 */
            String description) {
    }
}
