package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Twelve Data 交易所交易时间表（exchange_schedule）响应体
 * <p>需 Ultra（个人）/ Enterprise（企业）及以上套餐，每次消耗 100 积分
 * <p>接口文档见 doc/asset_catalogs/12-交易所交易时间表-exchange_schedule.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeScheduleResponse(
        /** 交易所时间表列表 */
        List<Schedule> data) {

    /**
     * 交易所时间表
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Schedule(
            /** 交易所官方名称 */
            String title,
            /** 交易所名称（简称） */
            String name,
            /** 市场识别码（MIC） */
            String code,
            /** 交易所所属国家 */
            String country,
            /** 交易所所在时区 */
            @JsonProperty("time_zone") String timeZone,
            /** 交易时段列表 */
            List<Session> sessions) {
    }

    /**
     * 交易时段
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Session(
            /** 时段开盘时间（HH:MM:SS） */
            @JsonProperty("open_time") String openTime,
            /** 时段收盘时间（HH:MM:SS） */
            @JsonProperty("close_time") String closeTime,
            /** 时段名称，如 Pre market（盘前） */
            @JsonProperty("session_name") String sessionName,
            /** 时段类型，如 pre（盘前）/ regular（常规） */
            @JsonProperty("session_type") String sessionType) {
    }
}
