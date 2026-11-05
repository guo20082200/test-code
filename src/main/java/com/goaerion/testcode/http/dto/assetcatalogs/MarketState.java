package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Twelve Data 市场状态（market_state）条目
 * <p>注意：该接口响应为【顶层数组】（无 data/status 包装层），反序列化时使用
 * {@code MarketState[].class}，不要包装成对象
 * <p>接口文档见 doc/asset_catalogs/14-市场状态-market_state.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketState(
        /** 交易所全称 */
        String name,
        /** 市场识别码（MIC，ISO 10383 标准） */
        String code,
        /** 交易所所在国家 */
        String country,
        /** 交易所当前是否开市 */
        @JsonProperty("is_market_open") Boolean isMarketOpen,
        /** 开市后已过时间（HH:MM:SS）；当前休市时为 00:00:00 */
        @JsonProperty("time_after_open") String timeAfterOpen,
        /** 距下次开市时间（HH:MM:SS）；当前开市时为 00:00:00 */
        @JsonProperty("time_to_open") String timeToOpen,
        /** 距本次收盘时间（HH:MM:SS）；当前休市时为 00:00:00 */
        @JsonProperty("time_to_close") String timeToClose) {
}
