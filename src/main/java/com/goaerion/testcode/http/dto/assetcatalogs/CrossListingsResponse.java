package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 交叉上市（cross_listings）响应体
 * <p>注意：数据包裹在 result 对象中（result.count + result.list），与 funds / bonds 相同
 * <p>需 Grow（个人）/ Venture（企业）及以上套餐，每次消耗 40 积分
 * <p>接口文档见 doc/asset_catalogs/09-交叉上市-cross_listings.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CrossListingsResponse(
        /** 结果包装对象 */
        Result result) {

    /**
     * 结果包装对象
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            /** 找到的交叉上市数量 */
            Integer count,
            /** 交叉上市列表 */
            List<CrossListing> list) {
    }

    /**
     * 交叉上市条目（同一证券在不同市场的代码可能不同）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CrossListing(
            /** 各市场的标的代码 */
            String symbol,
            /** 标的名称 */
            String name,
            /** 交易场所 */
            String exchange,
            /** 市场识别码（MIC） */
            @JsonProperty("mic_code") String micCode) {
    }
}
