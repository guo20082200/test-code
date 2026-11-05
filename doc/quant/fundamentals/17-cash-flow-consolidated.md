# Cash Flow Consolidated — 合并现金流量表接口

> 官方文档：https://twelvedata.com/docs#cash-flow-consolidated

## 概述

`/cash_flow/consolidated` 接口返回公司**原始格式**的合并现金流量表数据，包括流入流出企业的净现金及现金等价物。返回经营、投资和筹资活动信息，帮助用户追踪特定期间的流动性和财务健康状况。相比标准版现金流量表，该接口提供更细粒度的原始会计科目拆分。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /cash_flow/consolidated` |
| API credits | `100` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/cash_flow/consolidated` |

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
GET https://api.twelvedata.com/cash_flow/consolidated?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "cash_flow": [
        {
            "fiscal_date": "2023-09-30",
            "year": 2024,
            "cash_flow_from_operating_activities": {
                "net_income_from_continuing_operations": 96995000000,
                "operating_cash_flow": 110543000000,
                "cash_flow_from_continuing_operating_activities": 110543000000,
                "cash_from_discontinued_operating_activities": 108488000000,
                "free_cash_flow": 99584000000,
                "taxes_refund_paid": 108488000000,
                "interest_received": 108488000000,
                "interest_paid": 108488000000,
                "dividend_received": 108488000000,
                "dividend_paid": 108488000000,
                "change_in_working_capital": -6577000000,
                "change_in_receivables": -417000000,
                "changes_in_account_receivables": -1688000000,
                "change_in_payables_and_accrued_expense": -1889000000,
                "change_in_account_payable": -1889000000,
                "change_in_inventory": -1618000000,
                "other_non_cash_items": -2227000000,
                "stock_based_compensation": 10833000000,
                "deferred_tax": 108488000000,
                "depreciation_amortization_depletion": 11519000000,
                "depreciation_and_amortization": 11519000000,
                "unrealized_gain_loss_on_investment_securities": 108488000000,
                "gain_loss_on_sale_of_ppe": 108488000000,
                "net_foreign_currency_exchange_gain_loss": 108488000000
            },
            "cash_flow_from_investing_activities": {
                "investing_cash_flow": 3705000000,
                "cash_flow_from_continuing_investing_activities": 3705000000,
                "net_other_investing_changes": -1337000000,
                "net_investment_purchase_and_sale": 16001000000
            },
            "cash_flow_from_financing_activities": {
                "cash_from_discontinued_financing_activities": 108488000000,
                "net_other_financing_charges": 0,
                "interest_paid_cff": 0,
                "proceeds_from_stock_option_exercised": 0,
                "cash_dividends_paid": 0,
                "preferred_stock_dividend_paid": 0,
                "common_stock_dividend_paid": 0,
                "net_preferred_stock_issuance": 0,
                "net_common_stock_issuance": 0,
                "repurchase_of_capital_stock": 0,
                "net_issuance_payments_of_debt": 0
            }
        }
    ],
    "status": "ok"
}
```

> 响应示例有删节，实际响应包含更多明细科目。

## 响应字段说明（主要分组）

| 分组 | 说明 |
|---|---|
| `fiscal_date` / `year` | 财务期间截止日期 / 财务年度 |
| `cash_flow_from_operating_activities` | 经营活动现金流：持续经营净利润、经营现金流（直接/间接法）、终止经营现金流、自由现金流、税费返还/缴纳、利息与股息收付、营运资本变动（应收/应付/存货/预付等各项变动）、非现金项目（股权激励、递延税、折旧摊销、未实现损益、减值、外币折算等） |
| `cash_flow_from_investing_activities` | 投资活动现金流：投资现金流合计（持续/终止经营口径）、其他投资变动、利息/股息收付、净投资购买与出售、资本支出、收购等 |
| `cash_flow_from_financing_activities` | 筹资活动现金流：终止经营筹资现金流、其他筹资费用、已付利息/股息、股权激励行权、优先股/普通股发行与派息、资本回购、债务发行与偿还（净额/短期/长期）等 |
| `status` | 请求状态（`ok`） |

### 响应分组内的典型字段（部分）

| 字段 | 说明 |
|---|---|
| `operating_cash_flow` | 经营现金流 |
| `cash_flow_from_continuing_operating_activities` | 持续经营现金流 |
| `cash_from_discontinued_operating_activities` | 终止经营现金流 |
| `free_cash_flow` | 自由现金流 |
| `cash_flows_from_used_in_operating_activities_direct` | 直接法经营现金流 |
| `investing_cash_flow` | 投资现金流合计 |
| `net_investment_purchase_and_sale` | 投资购买与出售净额 |
| `net_issuance_payments_of_debt` | 债务发行与偿付净额 |
| `net_short_term_debt_issuance` / `short_term_debt_payments` | 短期债务净发行 / 偿付 |
| `repurchase_of_capital_stock` | 资本回购 |
| `cash_dividends_paid` | 已付现金股息 |
