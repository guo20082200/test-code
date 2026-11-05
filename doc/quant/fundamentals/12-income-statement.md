# Income Statement — 利润表接口

> 官方文档：https://twelvedata.com/docs#income-statement

## 概述

`/income_statement` 接口返回公司利润表的详细财务数据，包括指定期间（年报或季报）的营收、费用和净利润。适用于获取公司全面的财务绩效指标，包括历史和当前财务业绩。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /income_statement` |
| API credits | `100` / symbol |
| 最低套餐 | Pro（个人）/ Venture（商业）及以上；**完整历史数据**需 Ultra（个人）/ Enterprise（商业） |
| 完整 URL | `https://api.twelvedata.com/income_statement` |

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
| `period` | string | 否 | 报表报告期。支持：`annual`（年度）、`quarterly`（季度） | `annual` |
| `start_date` | string | 否 | 起始日期，仅返回财务日期在此日期及之后的利润表。格式 `2006-01-02` | `2024-01-01` |
| `end_date` | string | 否 | 结束日期，仅返回财务日期在此日期及之前的利润表。格式 `2006-01-02` | `2024-12-31` |
| `outputsize` | integer | 否 | 响应中的记录数 | `6`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/income_statement?symbol=AAPL&period=quarterly&apikey=your_api_key
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
    "income_statement": [
        {
            "fiscal_date": "2021-12-31",
            "quarter": 1,
            "year": 2022,
            "sales": 123945000000,
            "cost_of_goods": 69702000000,
            "gross_profit": 54243000000,
            "operating_expense": {
                "research_and_development": 6306000000,
                "selling_general_and_administrative": 6449000000,
                "other_operating_expenses": 0
            },
            "operating_income": 41488000000,
            "non_operating_interest": {
                "income": 650000000,
                "expense": 694000000
            },
            "other_income_expense": -203000000,
            "pretax_income": 41241000000,
            "income_tax": 6611000000,
            "net_income": 34630000000,
            "eps_basic": 2.11,
            "eps_diluted": 2.1,
            "basic_shares_outstanding": 16391724000,
            "diluted_shares_outstanding": 16391724000,
            "ebit": 41488000000,
            "ebitda": 44632000000,
            "net_income_continuous_operations": 0,
            "minority_interests": 0,
            "preferred_stock_dividends": 0
        }
    ]
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `meta.*` | object | 标的元信息（含 `period`：Quarterly / Annual） |
| `fiscal_date` | string | 财务期间截止日期（`YYYY-MM-DD`） |
| `quarter` | integer | 财务季度 |
| `year` | integer | 财务年度 |
| `sales` | double | 营业收入 |
| `cost_of_goods` | double | 营业成本（COGS） |
| `gross_profit` | double | 毛利润 |
| `operating_expense.research_and_development` | double | 研发费用 |
| `operating_expense.selling_general_and_administrative` | double | 销售及管理费用 |
| `operating_expense.other_operating_expenses` | double | 其他经营费用 |
| `operating_income` | double | 营业利润 |
| `non_operating_interest.income` / `expense` | double | 非经营利息收入 / 支出 |
| `other_income_expense` | double | 其他收入与支出 |
| `pretax_income` | double | 税前利润 |
| `income_tax` | double | 所得税 |
| `net_income` | double | 净利润 |
| `eps_basic` / `eps_diluted` | double | 基本 / 摊薄每股收益 |
| `basic_shares_outstanding` / `diluted_shares_outstanding` | integer | 基本 / 摊薄流通股数 |
| `ebit` / `ebitda` | double | EBIT / EBITDA |
| `net_income_continuous_operations` | double | 持续经营净利润 |
| `minority_interests` | double | 少数股东权益 |
| `preferred_stock_dividends` | double | 优先股股息 |
