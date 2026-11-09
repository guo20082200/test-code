package com.goaerion.testcode.http.quant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * K 线：日/周/月/分钟线共用一张表，(symbol, interval, bar_time) 幂等
 * <p>对应表 td_bar，DDL 见 doc/quant/market_data/schema.sql；
 * interval 为 PostgreSQL 保留字，列名需带双引号
 */
@Data
@TableName("td_bar")
public class TdBar {

    /** 代理主键：兼容 MyBatis-Plus BaseMapper 单一 @TableId，应用侧生成（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标的代码，与 td_symbol.symbol 对应（逻辑外键，不建 FK 约束） */
    private String symbol;

    /** K 线周期：1min/5min/15min/30min/45min/1h/2h/4h/8h/1day/1week/1month */
    @TableField("\"interval\"")
    private String interval;

    /** K 线开始时间（UTC） */
    private OffsetDateTime barTime;

    /** 原始开盘价（未复权） */
    private BigDecimal open;

    /** 原始最高价（未复权） */
    private BigDecimal high;

    /** 原始最低价（未复权） */
    private BigDecimal low;

    /** 原始收盘价（未复权） */
    private BigDecimal close;

    /** 复权收盘价：回测收益/收益序列用它；技术指标用 close */
    private BigDecimal adjClose;

    /** 成交量：加密货币为小数，故用 NUMERIC */
    private BigDecimal volume;

    /** 成交额；接口未提供时留空 */
    private BigDecimal amount;

    /** 计价货币快照（USD/CNY/USDT...） */
    private String currency;

    /** 首次入库时间（UTC） */
    private OffsetDateTime createdAt;

    /** 最近覆盖时间（UTC），复权/回补后可见变化 */
    private OffsetDateTime updatedAt;
}