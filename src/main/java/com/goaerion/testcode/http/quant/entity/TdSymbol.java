package com.goaerion.testcode.http.quant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 标的池：量化系统跟踪的金融工具清单（含接口 meta 属性快照）
 * <p>对应表 td_symbol，DDL 见 doc/quant/market_data/schema.sql
 */
@Data
@TableName("td_symbol")
public class TdSymbol {

    /** 代理主键：表无自增默认值，id 由应用侧生成（雪花算法），符合主键外部指定约定 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标的代码：AAPL / EUR/USD / ETH/BTC；优先股用点号（BRK.A） */
    private String symbol;

    /** 交易所名称（如 NASDAQ），外汇/加密货币可空 */
    private String exchange;

    /** ISO 10383 市场标识码（如 XNAS） */
    private String micCode;

    /** 资产粗分类：stock/forex/crypto/etf/fund/commodity/bond */
    private String assetType;

    /** 工具细分类，取自 meta.type：Common Stock / Preferred Stock / ADR / ETF / Index 等 */
    private String instrumentType;

    /** 交易/计价货币，取自 meta.currency：USD / CNY / USDT */
    private String currency;

    /** 交易所时区，取自 meta.exchange_timezone：America/New_York */
    private String exchangeTimezone;

    /** 需同步的 interval 集合，逗号分隔（1min,5min,1day） */
    private String intervalSet;

    /** 是否参与同步，FALSE 时调度器跳过该标的 */
    private Boolean enabled;

    /** 创建时间（UTC） */
    private OffsetDateTime createdAt;

    /** 更新时间（UTC） */
    private OffsetDateTime updatedAt;
}