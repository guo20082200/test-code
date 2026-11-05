# Cash Flow — 现金流量表接口

> 官方文档：https://twelvedata.com/docs#cash-flow

## 概述

`/cash_flow` 接口返回公司现金流量活动的详细信息，包括流入流出企业的净现金及现金等价物。数据涵盖经营、投资和筹资活动现金流，全面反映公司的流动性和财务健康状况。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /cash_flow` |
| API credits | `100` / symbol |
| 最低套餐 | Pro（个人）/ Venture（商业）及以上；**完整历史数据**需 Ultra（个人）/ Enterprise（商业） |
| 完整 URL | `https://api.twelvedata.com/cash_flow` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 可选 | 交易所在国家，如 `United States` 或 `US` | `United States` |

### 报表期间与筛选参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `period` | string | 否 | 报表报告期。支持：`annual`（年度）、`quarterly`（季度） | `annual`（默认） |
| `start_date` | string | 否 | 起始日期，仅返回财务日期在此日期及之后的现金流量表。格式 `2006-01-02` | `2024-01-01` |
| `end_date` | string | 否 | 结束日期，仅返回财务日期在此日期及之前的现金流量表。格式 `2006-01-02` | `2024-12-31` |
| `outputsize` | integer | 否 | 响应中的记录数 | `6`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/cash_flow?symbol=AAPL&period=quarterly&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "name": "Apple Inc",
        "currency": "USD",
        "exchange": "NASDAQ",
        "mic_code": "XNAS",
        "exchange_timezone": "America/New_York",
        "period": "Quarterly"
    },
    "cash_flow": [
        {
            "fiscal_date": "2021-12-31",
            "quarter": "1",
            "year": 2022,
            "operating_activities": {
                "net_income": 34630000000,
                "depreciation": 2697000000,
                "deferred_taxes": 682000000,
                "stock_based_compensation": 2265000000,
                "other_non_cash_items": 167000000,
                "accounts_receivable": -13746000000,
                "accounts_payable": 19813000000,
                "other_assets_liabilities": 458000000,
                "operating_cash_flow": 46966000000
            },
            "investing_activities": {
                "capital_expenditures": -2803000000,
                "net_intangibles": 0,
                "net_acquisitions": 0,
                "purchase_of_investments": -34913000000,
                "sale_of_investments": 21984000000,
                "other_investing_activity": -374000000,
                "investing_cash_flow": -16106000000
            },
            "financing_activities": {
                "long_term_debt_issuance": 0,
                "long_term_debt_payments": 0,
                "short_term_debt_issuance": -1000000000,
                "common_stock_issuance": 0,
                "common_stock_repurchase": -20478000000,
                "common_dividends": -3732000000,
                "other_financing_charges": -2949000000,
                "financing_cash_flow": -28159000000
            },
            "end_cash_position": 38630000000,
            "income_tax_paid": 5235000000,
            "interest_paid": 531000000,
            "free_cash_flow": 49769000000
        }
    ]
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `meta.*` | object | 标的元信息（含 `period`：Quarterly / Annual） |
| `fiscal_date` | string | 财务期间截止日期 |
| `quarter` | string | 财务季度（`period=quarterly` 时可见） |
| `year` | integer | 财务年度 |

### operating_activities — 经营活动

| 字段 | 说明 |
|---|---|
| `net_income` | 净利润 |
| `depreciation` | 折旧 |
| `deferred_taxes` | 递延税款 |
| `stock_based_compensation` | 股权激励费用 |
| `other_non_cash_items` | 其他非现金项目 |
| `accounts_receivable` / `accounts_payable` | 应收账款变动 / 应付账款变动 |
| `other_assets_liabilities` | 其他资产与负债变动 |
| `operating_cash_flow` | 经营现金流合计 |

### investing_activities — 投资活动

| 字段 | 说明 |
|---|---|
| `capital_expenditures` | 资本支出 |
| `net_intangibles` / `net_acquisitions` | 无形资产净值 / 净收购 |
| `purchase_of_investments` / `sale_of_investments` | 投资购买 / 投资出售 |
| `other_investing_activity` | 其他投资活动 |
| `investing_cash_flow` | 投资现金流合计 |

### financing_activities — 筹资活动

| 字段 | 说明 |
|---|---|
| `long_term_debt_issuance` / `long_term_debt_payments` | 长期债务发行 / 偿还 |
| `short_term_debt_issuance` | 短期债务发行 |
| `common_stock_issuance` / `common_stock_repurchase` | 普通股发行 / 回购 |
| `common_dividends` | 普通股股息 |
| `other_financing_charges` | 其他筹资费用 |
| `financing_cash_flow` | 筹资现金流合计 |

### 其他

| 字段 | 说明 |
|---|---|
| `end_cash_position` | 期末现金头寸 |
| `income_tax_paid` | 已缴所得税 |
| `interest_paid` | 已付利息 |
| `free_cash_flow` | 自由现金流 |
