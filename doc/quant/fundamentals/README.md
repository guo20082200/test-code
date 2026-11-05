# Twelve Data — Fundamentals（公司基本面）接口文档

> 来源：https://twelvedata.com/docs/fundamentals
>
> 提供公司与基金的深度财务数据——利润表、资产负债表、现金流量表、公司概况、公司事件（股息/拆股/财报/IPO）以及关键比率，适用于估值、筛选和基本面研究。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数 |
| 请求方式 | `GET` |
| 响应格式 | 默认 `JSON`（部分接口支持 `CSV`） |
| 计费单位 | API credits（按 symbol 或按 request 计） |

## 通用说明

### 标的标识参数

大多数接口支持以下标的标识方式，**通常要求其中至少一个**（文档中标注 "One of these parameters is required"）：

| 参数 | 说明 |
|---|---|
| `symbol` | 标的代码，优先股用点号分隔（如 `BRK.A`、`BRK.B`） |
| `figi` | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 |
| `isin` | 国际证券识别码（ISIN）；响应中返回 ISIN 需 ISIN Data 附加包 |
| `cusip` | 美国 CUSIP 码；响应中返回 CUSIP 需 CUSIP Data 附加包 |
| `exchange` | 交易所名称，如 `NASDAQ` |
| `mic_code` | ISO 10383 市场标识码（MIC），如 `XNAS` |
| `country` | 交易所在国家，如 `United States` 或 `US` |

### 套餐层级（个人版）

`Free` < `Basic` < `Grow` < `Pro` < `Ultra`（商业版对应 `Venture` / `Enterprise`）

## 接口目录

| # | 文档 | 接口 | Credits | 最低套餐 | 说明 |
|---|---|---|---|---|---|
| 01 | [Logo](01-logo.md) | `/logo` | 1 / symbol | 无 | 公司/加密货币/外汇 Logo 图片 |
| 02 | [Profile](02-profile.md) | `/profile` | 10 / symbol | Grow | 公司概况（行业、板块、CEO、地址） |
| 03 | [Dividends](03-dividends.md) | `/dividends` | 20 / symbol | Grow | 股息历史 |
| 04 | [Dividends calendar](04-dividends-calendar.md) | `/dividends_calendar` | 40 / symbol | Grow | 股息日历 |
| 05 | [Splits](05-splits.md) | `/splits` | 20 / symbol | Grow | 拆股历史 |
| 06 | [Splits calendar](06-splits-calendar.md) | `/splits_calendar` | 40 / symbol | Grow | 拆股日历 |
| 07 | [Earnings](07-earnings.md) | `/earnings` | 20 / symbol | Grow | 财报 EPS（预估/实际） |
| 08 | [Earnings calendar](08-earnings-calendar.md) | `/earnings_calendar` | 40 / request | Grow | 财报发布日历 |
| 09 | [IPO calendar](09-ipo-calendar.md) | `/ipo_calendar` | 40 / request | Grow | IPO 日历 |
| 10 | [Statistics](10-statistics.md) | `/statistics` | 50 / symbol | Pro | 估值/财务/股性统计快照 |
| 11 | [Press releases](11-press-releases.md) | `/press_releases` | 1 / request | Basic | 官方新闻稿 |
| 12 | [Income statement](12-income-statement.md) | `/income_statement` | 100 / symbol | Pro | 利润表（标准格式） |
| 13 | [Income statement consolidated](13-income-statement-consolidated.md) | `/income_statement/consolidated` | 100 / symbol | Ultra | 利润表（原始合并格式） |
| 14 | [Balance sheet](14-balance-sheet.md) | `/balance_sheet` | 100 / symbol | Pro | 资产负债表（标准格式） |
| 15 | [Balance sheet consolidated](15-balance-sheet-consolidated.md) | `/balance_sheet/consolidated` | 100 / symbol | Ultra | 资产负债表（原始合并格式） |
| 16 | [Cash flow](16-cash-flow.md) | `/cash_flow` | 100 / symbol | Pro | 现金流量表（标准格式） |
| 17 | [Cash flow consolidated](17-cash-flow-consolidated.md) | `/cash_flow/consolidated` | 100 / symbol | Ultra | 现金流量表（原始合并格式） |
| 18 | [Key executives](18-key-executives.md) | `/key_executives` | 1000 / symbol | Ultra | 公司高管信息 |
| 19 | [Market capitalization](19-market-cap.md) | `/market_cap` | 5 / request | Ultra | 市值历史时间序列 |
| 20 | [Last changes](20-last-change.md) | `/last_change/{endpoint}` | 1 / request | 无 | 基本面数据最后更新时间戳 |

## 请求示例

```
GET https://api.twelvedata.com/profile?symbol=AAPL&apikey=your_api_key
```

`apikey=demo` 可用于部分接口的演示查询（有限制）。

## 错误响应

```json
{
    "code": 404,
    "message": "****not found",
    "status": "error"
}
```

常见错误：无效 `apikey`（401）、套餐权限不足（403）、超出配额（429）、无效参数（400）。
