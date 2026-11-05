package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Twelve Data 工具类型目录（instrument_type）响应体
 * <p>注意：result 字段直接是字符串数组（与 funds/bonds 的 result.count + result.list 包装结构不同）
 * <p>接口文档见 doc/asset_catalogs/16-工具类型目录-instrument_type.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InstrumentTypeResponse(
        /** 工具类型名称列表，如 Common Stock、ETF、REIT */
        List<String> result,
        /** 响应状态，如 ok */
        String status) {
}
