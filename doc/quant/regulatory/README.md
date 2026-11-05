# Twelve Data — Regulatory（合规与监管）接口文档

> 来源：https://twelvedata.com/docs/regulatory
>
> 合规与申报数据：内幕交易、SEC 报告、治理文件等。适用于审计追踪、尽职调查工作流与风险管理集成。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数 |
| 请求方式 | `GET` |
| 响应格式 | `JSON` |
| 接口总数 | 7 个 |

## 接口目录

| # | 文档 | 接口 | Credits | 最低套餐 | 说明 |
|---|---|---|---|---|---|
| 01 | [EDGAR filings](01-edgar-filings.md) | `/edgar_filings/archive` | 50 / request | Ultra / Enterprise | SEC EDGAR 申报文件归档（支持表单类型与日期过滤） |
| 02 | [Insider transaction](02-insider-transactions.md) | `/insider_transactions` | 200 / symbol | Pro / Venture | 内部人交易明细 |
| 03 | [Institutional holders](03-institutional-holders.md) | `/institutional_holders` | 1500 / symbol | Ultra / Enterprise | 机构投资者持仓 |
| 04 | [Fund holders](04-fund-holders.md) | `/fund_holders` | 1500 / symbol | Ultra / Enterprise | 共同基金持仓 |
| 05 | [Direct holders](05-direct-holders.md) | `/direct_holders` | 1500 / symbol | Ultra / Enterprise | 官方股东名册直接持股人（Tadawul 为 beta） |
| 06 | [Tax information](06-tax-info.md) | `/tax_info` | 50 / symbol | Ultra / Enterprise | 税务标识信息 |
| 07 | [Sanctioned entities](07-sanctions.md) | `/sanctions/{source}` | 50 / request | Ultra / Enterprise | 制裁实体清单（OFAC/UK/EU/AU） |

## 通用说明

### 标的标识参数

除 Sanctions 外，其余接口要求以下参数**至少提供一个**（"✱ One of these parameters is required"）：

| 参数 | 说明 |
|---|---|
| `symbol` | 标的代码，如 `AAPL`；**优先股需用点号分隔**，如 `BRK.A`、`BRK.B` |
| `figi` | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 |
| `isin` | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 |
| `cusip` | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 |
| `exchange` | 交易所名称，如 `Nasdaq`、`NSE` |
| `mic_code` | 市场标识码（ISO 10383），如 `XNAS`、`XLON` |
| `country` | 交易国家名称或缩写，如 `United States` 或 `US`（注意：`/tax_info` 不支持 `country` 参数） |

### 响应结构

除 Sanctions 外，所有接口响应均含 `meta`（标的元信息）+ 业务数据数组。Sanctions 接口响应为清单 + `count`。

### 成本提示

本组接口成本梯度极大，调用前务必评估：

| 分级 | Credits | 接口 |
|---|---|---|
| 低成本 | 50 | EDGAR filings、Sanctions（均按 request 计）、Tax info（按 symbol 计） |
| 中成本 | 200 | Insider transactions |
| **高成本** | **1500** | Institutional holders、Fund holders、Direct holders（本组为 Twelve Data 全部接口中最贵的一档，仅应在必要场景调用） |
