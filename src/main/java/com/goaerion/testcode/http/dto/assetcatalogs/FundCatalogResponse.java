package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 基金目录（funds）响应体
 * <p>注意：数据包裹在 result 对象中（result.count + result.list），与其他目录接口不同
 * <p>接口文档见 doc/asset_catalogs/05-基金目录-funds.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FundCatalogResponse(
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
            /** 基金列表（结构与 stocks/etfs 的标的一致，复用 Instrument） */
            List<AssetCatalogResponse.Instrument> list) {
    }
}
