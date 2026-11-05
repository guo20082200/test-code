# Balance Sheet — 资产负债表接口

> 官方文档：https://twelvedata.com/docs#balance-sheet

## 概述

`/balance_sheet` 接口返回公司资产负债表的详细财务数据，包括流动/非流动资产、总负债和股东权益等结构化数据。适用于评估公司的财务健康状况和稳定性。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /balance_sheet` |
| API credits | `100` / symbol |
| 最低套餐 | Pro（个人）/ Venture（商业）及以上；**完整历史数据**需 Ultra（个人）/ Enterprise（商业） |
| 完整 URL | `https://api.twelvedata.com/balance_sheet` |

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
| `end_date` | string | 否 | 结束日期，仅返回财务日期在此日期及之前的报表。格式 `2006-01-02` | `2024-05-01` |
| `outputsize` | integer | 否 | 响应中的记录数 | `6`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/balance_sheet?symbol=AAPL&period=quarterly&apikey=your_api_key
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
    "balance_sheet": [
        {
            "fiscal_date": "2021-09-30",
            "year": 2022,
            "assets": {
                "current_assets": {
                    "cash": 17305000000,
                    "cash_equivalents": 17635000000,
                    "cash_and_cash_equivalents": 34940000000,
                    "other_short_term_investments": 27699000000,
                    "accounts_receivable": 26278000000,
                    "other_receivables": 25228000000,
                    "inventory": 6580000000,
                    "prepaid_assets": 0,
                    "restricted_cash": 0,
                    "assets_held_for_sale": 0,
                    "hedging_assets": 0,
                    "other_current_assets": 14111000000,
                    "total_current_assets": 134836000000
                },
                "non_current_assets": {
                    "properties": 0,
                    "land_and_improvements": 20041000000,
                    "machinery_furniture_equipment": 78659000000,
                    "construction_in_progress": 0,
                    "leases": 11023000000,
                    "accumulated_depreciation": -70283000000,
                    "goodwill": 0,
                    "investment_properties": 0,
                    "financial_assets": 0,
                    "intangible_assets": 0,
                    "investments_and_advances": 127877000000,
                    "other_non_current_assets": 48849000000,
                    "total_non_current_assets": 216166000000
                },
                "total_assets": 351002000000
            },
            "liabilities": {
                "current_liabilities": {
                    "accounts_payable": 54763000000,
                    "accrued_expenses": 0,
                    "short_term_debt": 15613000000,
                    "deferred_revenue": 7612000000,
                    "tax_payable": 0,
                    "pensions": 0,
                    "other_current_liabilities": 47493000000,
                    "total_current_liabilities": 125481000000
                },
                "non_current_liabilities": {
                    "long_term_provisions": 0,
                    "long_term_debt": 109106000000,
                    "provision_for_risks_and_charges": 24689000000,
                    "deferred_liabilities": 0,
                    "derivative_product_liabilities": 0,
                    "other_non_current_liabilities": 28636000000,
                    "total_non_current_liabilities": 162431000000
                },
                "total_liabilities": 287912000000
            },
            "shareholders_equity": {
                "common_stock": 57365000000,
                "retained_earnings": 5562000000,
                "other_shareholders_equity": 163000000,
                "total_shareholders_equity": 63090000000,
                "additional_paid_in_capital": 0,
                "treasury_stock": 0,
                "minority_interest": 0
            }
        }
    ]
}
```

## 响应字段说明

### assets — 资产

| 字段 | 说明 |
|---|---|
| `current_assets.cash` / `cash_equivalents` / `cash_and_cash_equivalents` | 现金 / 现金等价物 / 两者合计 |
| `current_assets.other_short_term_investments` | 其他短期投资 |
| `current_assets.accounts_receivable` / `other_receivables` | 应收账款 / 其他应收款 |
| `current_assets.inventory` | 存货 |
| `current_assets.prepaid_assets` / `restricted_cash` / `assets_held_for_sale` / `hedging_assets` | 预付资产 / 受限现金 / 待售资产 / 对冲资产 |
| `current_assets.other_current_assets` | 其他流动资产 |
| `current_assets.total_current_assets` | 流动资产合计 |
| `non_current_assets.properties` / `land_and_improvements` / `machinery_furniture_equipment` | 物业 / 土地及改良 / 机器家具设备 |
| `non_current_assets.construction_in_progress` / `leases` | 在建工程 / 租赁 |
| `non_current_assets.accumulated_depreciation` | 累计折旧（负值） |
| `non_current_assets.goodwill` / `intangible_assets` / `financial_assets` | 商誉 / 无形资产 / 金融资产 |
| `non_current_assets.investments_and_advances` | 投资与预付款 |
| `non_current_assets.other_non_current_assets` | 其他非流动资产 |
| `non_current_assets.total_non_current_assets` | 非流动资产合计 |
| `total_assets` | 总资产 |

### liabilities — 负债

| 字段 | 说明 |
|---|---|
| `current_liabilities.accounts_payable` | 应付账款 |
| `current_liabilities.accrued_expenses` | 应计费用 |
| `current_liabilities.short_term_debt` | 短期债务 |
| `current_liabilities.deferred_revenue` | 递延收入 |
| `current_liabilities.tax_payable` / `pensions` | 应交税费 / 养老金 |
| `current_liabilities.other_current_liabilities` | 其他流动负债 |
| `current_liabilities.total_current_liabilities` | 流动负债合计 |
| `non_current_liabilities.long_term_provisions` / `long_term_debt` | 长期准备金 / 长期债务 |
| `non_current_liabilities.provision_for_risks_and_charges` | 风险与费用准备 |
| `non_current_liabilities.deferred_liabilities` / `derivative_product_liabilities` | 递延负债 / 衍生品负债 |
| `non_current_liabilities.other_non_current_liabilities` | 其他非流动负债 |
| `non_current_liabilities.total_non_current_liabilities` | 非流动负债合计 |
| `total_liabilities` | 总负债 |

### shareholders_equity — 股东权益

| 字段 | 说明 |
|---|---|
| `common_stock` | 普通股 |
| `retained_earnings` | 留存收益 |
| `other_shareholders_equity` | 其他股东权益 |
| `total_shareholders_equity` | 股东权益合计 |
| `additional_paid_in_capital` | 资本公积（超额缴入资本） |
| `treasury_stock` | 库存股 |
| `minority_interest` | 少数股东权益 |
