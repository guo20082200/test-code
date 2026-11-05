# Income Statement Consolidated — 合并利润表接口

> 官方文档：https://twelvedata.com/docs#income-statement-consolidated

## 概述

`/income_statement/consolidated` 接口返回公司**原始格式**的合并利润表，详细列出指定期间（年报或季报）的营收、费用和净利润。相比标准版利润表，该接口提供更细粒度的原始会计科目拆分。适用于按时间维度评估公司财务表现。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /income_statement/consolidated` |
| API credits | `100` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/income_statement/consolidated` |

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
GET https://api.twelvedata.com/income_statement/consolidated?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "income_statement": [
        {
            "fiscal_date": "2023-09-30",
            "year": 2022,
            "revenue": {
                "total_revenue": 383285000000,
                "operating_revenue": 383285000000
            },
            "gross_profit": {
                "gross_profit_value": 169148000000,
                "cost_of_revenue": {
                    "cost_of_revenue_value": 214137000000,
                    "excise_taxes": 214137000000,
                    "reconciled_cost_of_revenue": 214137000000
                }
            },
            "operating_income": {
                "operating_income_value": 114301000000,
                "total_operating_income_as_reported": 114301000000,
                "operating_expense": 54847000000,
                "other_operating_expenses": 114301000000,
                "total_expenses": 268984000000
            },
            "net_income": {
                "net_income_value": 96995000000,
                "net_income_common_stockholders": 96995000000,
                "net_income_continuous_operations": 96995000000,
                "normalized_income": 96995000000
            },
            "earnings_per_share": {
                "diluted_eps": 6.13,
                "basic_eps": 6.16,
                "normalized_diluted_eps": 6.16,
                "normalized_basic_eps": 6.16,
                "diluted_continuous_operations": 6.16,
                "basic_continuous_operations": 6.16
            },
            "expenses": {
                "total_expenses": 268984000000,
                "selling_general_and_administration_expense": 24932000000,
                "research_and_development_expense": 29915000000,
                "depreciation_amortization_depletion_income_statement": 29915000000,
                "salaries_and_wages_expense": 29915000000
            },
            "interest_income_and_expense": {
                "interest_income": 3750000000,
                "interest_expense": 3933000000,
                "net_interest_income": -183000000,
                "interest_expense_non_operating": 3933000000,
                "interest_income_non_operating": 3750000000
            },
            "other_income_and_expenses": {
                "other_income_expense": -382000000,
                "special_income_charges": 382000000,
                "gain_on_sale_of_ppe": 382000000,
                "gain_on_sale_of_business": 382000000,
                "write_off": 382000000,
                "impairment_of_capital_assets": 382000000,
                "restructuring_and_merger_acquisition": 382000000,
                "earnings_from_equity_interest": 382000000,
                "total_other_finance_cost": 382000000
            },
            "taxes": {
                "tax_provision": 16741000000,
                "tax_rate_for_calculations": 0.147
            },
            "depreciation_and_amortization": {
                "depreciation_amortization_depletion": 129188000000,
                "amortization_of_intangibles": 129188000000,
                "depreciation": 129188000000,
                "amortization": 129188000000,
                "depletion": 129188000000
            },
            "ebitda": {
                "ebitda_value": 129188000000,
                "normalized_ebitda_value": 129188000000,
                "ebit_value": 117669000000
            },
            "dividends_and_shares": {
                "dividend_per_share": 15812547000,
                "diluted_average_shares": 15812547000,
                "basic_average_shares": 15744231000,
                "preferred_stock_dividends": 15744231000
            },
            "unusual_items": {
                "total_unusual_items": 11519000000,
                "total_unusual_items_excluding_goodwill": 11519000000
            },
            "depreciation": {
                "reconciled_depreciation": 11519000000
            },
            "pretax_income": {
                "pretax_income_value": 113736000000
            },
            "special_income_charges": {
                "special_income_charges_value": 113736000000
            }
        }
    ],
    "status": "ok"
}
```

> 响应示例有删节，实际响应包含更多明细科目（如各项应收/税费/非经常性项目等）。

## 响应字段说明（主要分组）

| 分组 | 说明 |
|---|---|
| `fiscal_date` / `year` | 财务期间截止日期 / 财务年度 |
| `revenue` | 营收：`total_revenue` 总营收、`operating_revenue` 经营营收 |
| `gross_profit` | 毛利：`gross_profit_value`、`cost_of_revenue`（营业成本，含 `excise_taxes` 消费税、`reconciled_cost_of_revenue`） |
| `operating_income` | 营业利润：`operating_income_value`、`operating_expense`、`total_expenses` 等 |
| `net_income` | 净利润：`net_income_value`、归属普通股东、含非控制性权益、持续/终止经营、正常化利润等 |
| `earnings_per_share` | EPS：`basic_eps` / `diluted_eps` 及各种口径的基本与摊薄 EPS |
| `expenses` | 费用明细：销管费、营销费、研发费、折旧摊销、薪资、租金、坏账准备等 |
| `interest_income_and_expense` | 利息收入/支出：`interest_income`、`interest_expense`、`net_interest_income` 等 |
| `other_income_and_expenses` | 其他收支：特殊收支、资产处置收益、减值、重组并购、权益法投资收益等 |
| `taxes` | 税费：`tax_provision` 税款准备、`tax_rate_for_calculations` 税率 |
| `depreciation_and_amortization` | 折旧摊销：折旧、摊销、折耗等 |
| `ebitda` | `ebitda_value`、`normalized_ebitda_value`、`ebit_value` |
| `dividends_and_shares` | 每股股息、基本/摊薄平均股数、优先股股息 |
| `unusual_items` | 非经常性项目合计 |
| `pretax_income` | 税前利润 |
| `special_income_charges` | 特殊收支 |
| `status` | 请求状态（`ok`） |
