package com.goaerion.testcode.http.quant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 同步游标：每个 symbol+interval 已同步到的时间点，支持断点续传
 * <p>对应表 td_sync_cursor，DDL 见 doc/quant/market_data/schema.sql；
 * 复合主键 (symbol, interval)，MyBatis-Plus 不支持复合 @TableId，
 * 按主键的读写走 TdSyncCursorMapper XML 中的手写 SQL
 */
@Data
@TableName("td_sync_cursor")
public class TdSyncCursor {

    /** 标的代码，与 td_bar.symbol 同口径 */
    private String symbol;

    /** K 线周期 */
    @TableField("\"interval\"")
    private String interval;

    /** 已同步的最后一条 K 线时间（UTC）；NULL 表示尚未开始 */
    private OffsetDateTime lastBarTime;

    /** 最近一次同步动作的时间 */
    private OffsetDateTime lastSyncAt;
}