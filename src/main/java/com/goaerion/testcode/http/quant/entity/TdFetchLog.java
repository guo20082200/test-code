package com.goaerion.testcode.http.quant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 拉取日志：每次任务的结果与 credits 消耗
 * <p>对应表 td_fetch_log，DDL 见 doc/quant/market_data/schema.sql
 */
@Data
@TableName("td_fetch_log")
public class TdFetchLog {

    /** 代理主键：表无自增默认值，id 由应用侧生成（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 任务类型：time_series_backfill / time_series_increment / quote / api_usage */
    private String taskType;

    /** 标的代码，记录便于排查；批量任务可空 */
    private String symbol;

    /** K 线周期，批量任务可空 */
    @TableField("\"interval\"")
    private String interval;

    /** 本次拉取的 K 线条数 */
    private Integer barsFetched;

    /** 结果状态：success / partial / failed / skipped_no_quota */
    private String status;

    /** 失败原因 */
    private String errorMsg;

    /** 本次消耗的 credits */
    private Integer creditsCost;

    /** 任务开始时间（UTC） */
    private OffsetDateTime startedAt;

    /** 任务结束时间（UTC） */
    private OffsetDateTime finishedAt;
}