package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 国家目录（countries）响应体
 * <p>接口文档见 doc/asset_catalogs/15-国家目录-countries.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CountriesResponse(
        /** 国家列表 */
        List<Country> data) {

    /**
     * 国家信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Country(
            /** ISO 3166 两位国家代码，如 US */
            String iso2,
            /** ISO 3166 三位国家代码，如 USA */
            String iso3,
            /** ISO 3166 数字国家代码，如 840 */
            String numeric,
            /** 国家全称，如 United States */
            String name,
            /** 国家官方名称，如 United States of America */
            @JsonProperty("official_name") String officialName,
            /** 首都 */
            String capital,
            /** 国家货币 */
            String currency) {
    }
}
