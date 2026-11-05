package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Twelve Data 最早时间戳（earliest_timestamp）响应体
 * <p>注意：响应为扁平对象（无包装层），仅两个字段
 * <p>接口文档见 doc/asset_catalogs/10-最早时间戳-earliest_timestamp.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EarliestTimestampResponse(
        /** 最早可用时间，格式取决于 interval（如 1day 为 1980-12-12） */
        String datetime,
        /** 该时间对应的 UNIX 时间戳 */
        Long unix_time) {
}
