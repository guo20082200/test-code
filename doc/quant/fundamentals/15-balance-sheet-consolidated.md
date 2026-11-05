# Balance Sheet Consolidated — 合并资产负债表接口

> 官方文档：https://twelvedata.com/docs#balance-sheet-consolidated

## 概述

`/balance_sheet/consolidated` 接口返回公司**原始格式**的合并资产负债表，全面汇总其资产、负债和股东权益。相比标准版资产负债表，该接口提供更细粒度的原始会计科目拆分。适用于获取反映公司整体财务状况和资本结构的关键信息。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /balance_sheet/consolidated` |
| API credits | `100` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/balance_sheet/consolidated` |

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
| `start_date` | string | 否 | 起始日期，仅返回财务日期在此日期及之后的报表。格式 `2006-01-02` | `2024-01-01` |
| `end_date` | string | 否 | 结束日期，仅返回财务日期在此日期及之前的报表。格式 `2006-01-02` | `2024-12-31` |
| `outputsize` | integer | 否 | 响应中的记录数 | `6`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/balance_sheet/consolidated?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "balance_sheet": [
        {
            "fiscal_date": "2023-09-30",
            "assets": {
                "total_assets": 352583000000,
                "current_assets": {
                    "total_current_assets": 143566000000,
                    "cash_cash_equivalents_and_short_term_investments": 61555000000,
                    "cash_and_cash_equivalents": 29965000000,
                    "other_short_term_investments": 31590000000,
                    "restricted_cash": 31590000000,
                    "receivables": {
                        "total_receivables": 60985000000,
                        "accounts_receivable": 29508000000,
                        "other_receivables": 31477000000
                    },
                    "inventory": {
                        "total_inventory": 6331000000,
                        "finished_goods": 6331000000,
                        "work_in_process": 6331000000,
                        "raw_materials": 6331000000
                    },
                    "prepaid_assets": 14695000000,
                    "current_deferred_assets": 14695000000,
                    "other_current_assets": 14695000000
                },
                "non_current_assets": {
                    "total_non_current_assets": 209017000000,
                    "investments_and_advances": 100544000000,
                    "goodwill_and_other_intangible_assets": {
                        "goodwill": 100544000000,
                        "other_intangible_assets": 100544000000,
                        "total_goodwill_and_intangible_assets": 100544000000
                    },
                    "net_ppe": 54376000000,
                    "gross_ppe": 125260000000,
                    "accumulated_depreciation": -70884000000,
                    "leases": 12839000000,
                    "construction_in_progress": 12839000000,
                    "machinery_furniture_equipment": 78314000000,
                    "land_and_improvements": 23446000000,
                    "other_non_current_assets": 36245000000
                },
                "liabilities": {
                    "total_liabilities_net_minority_interest": 290437000000,
                    "current_liabilities": {
                        "total_current_liabilities": 145308000000,
                        "current_debt_and_capital_lease_obligation": 17382000000,
                        "current_debt": 15807000000,
                        "current_capital_lease_obligation": 1575000000,
                        "payables_and_accrued_expenses": {
                            "total_payables_and_accrued_expenses": 71430000000,
                            "accounts_payable": 62611000000,
                            "total_tax_payable": 8819000000
                        },
                        "current_deferred_revenue": 8061000000,
                        "other_current_liabilities": 48435000000
                    },
                    "non_current_liabilities": {
                        "total_non_current_liabilities_net_minority_interest": 145129000000,
                        "long_term_debt_and_capital_lease_obligation": {
                            "total_long_term_debt_and_capital_lease_obligation": 106548000000,
                            "long_term_debt": 95281000000,
                            "long_term_capital_lease_obligation": 11267000000
                        },
                        "long_term_provisions": 15457000000,
                        "other_non_current_liabilities": 23124000000
                    },
                    "equity": {
                        "total_equity_gross_minority_interest": 62146000000,
                        "stockholders_equity": 62146000000,
                        "total_capitalization": 157427000000,
                        "net_tangible_assets": 62146000000,
                        "invested_capital": 173234000000,
                        "working_capital": -1742000000,
                        "capital_stock": {
                            "common_stock": 73812000000,
                            "additional_paid_in_capital": 73812000000,
                            "retained_earnings": -214000000,
                            "treasury_stock": 73812000000,
                            "ordinary_shares_number": 15550061000,
                            "share_issued": 15550061000
                        },
                        "equity_adjustments": {
                            "foreign_currency_translation_adjustments": 11452000000,
                            "unrealized_gain_loss": 11452000000
                        },
                        "net_debt": 81123000000,
                        "total_debt": 123930000000
                    }
                }
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
| `fiscal_date` | 财务期间截止日期 |
| `assets.total_assets` | 总资产 |
| `assets.current_assets` | 流动资产：现金及等价物与短期投资、应收款项（含毛应收、坏账准备、关联方应收、税费/利息/票据/贷款应收等）、存货（成品/在产品/原材料）、预付资产、递延资产、待售/对冲资产等 |
| `assets.non_current_assets` | 非流动资产：金融资产、投资与预付款（多种口径）、商誉与无形资产、净 PPE / 毛 PPE / 累计折旧、租赁、在建工程、土地与设备等 |
| `assets.liabilities.current_liabilities` | 流动负债：流动债务与资本租赁义务、应付与应计项目（应付账款、应付利息、应付股利、应交所得税等）、养老金与退休福利、递延收入/税费等 |
| `assets.liabilities.non_current_liabilities` | 非流动负债：长期债务与资本租赁义务、长期准备金、递延负债/收入/税费、衍生品负债等 |
| `assets.liabilities.equity` | 权益：股东权益合计、总资本化、有形净资产、投入资本、营运资本、股本（普通股/优先股、资本公积、留存收益、库存股、股数）、权益调整（外币折算、未实现损益等）、净债务、总债务 |
| `status` | 请求状态（`ok`） |
