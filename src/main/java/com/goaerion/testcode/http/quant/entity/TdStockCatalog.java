package com.goaerion.testcode.http.quant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 股票目录快照：Twelve Data /stocks 全量清单（含 name/figi_code），与 td_symbol 跟踪池分离
 * <p>对应表 td_stock_catalog，DDL 见 doc/quant/market_data/stock_catalog_schema.sql
 */
@Data
@TableName("td_stock_catalog")
public class TdStockCatalog {

    /** 代理主键：表无自增默认值，id 由应用侧生成（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标的代码（Twelve Data symbol）：AAPL / AACIW（warrant 独立代码） */
    private String symbol;

    /** 标的全称：Apple Inc. */
    private String name;

    /** 计价货币：USD */
    private String currency;

    /** 交易所名称：NASDAQ */
    private String exchange;

    /** ISO 10383 市场标识码：XNGS / XNCM / XNMS / XNAS */
    private String micCode;

    /** 注册国家：United States */
    private String country;

    /** 工具类型：Common Stock / Preferred Stock / Warrant / American Depositary Receipt 等 */
    private String type;

    /** FIGI 金融工具全球标识（12 位）；部分标的为空 */
    private String figiCode;

    /** 快照抓取时间，同批数据取同一值 */
    private OffsetDateTime fetchedAt;

    /** 创建时间（UTC） */
    private OffsetDateTime createdAt;

    /** 更新时间（UTC） */
    private OffsetDateTime updatedAt;
}