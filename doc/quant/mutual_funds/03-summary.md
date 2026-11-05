# Summary — 共同基金概况接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/summary` 接口返回全球共同基金的简明概览，包括基金名称、代码、资产类别和地区等关键信息。适用于快速获取全球各类共同基金的核心信息，辅助投资组合的基金比较与筛选。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/summary` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/summary` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 基金代码 | `0P0001LCQ3` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG00HMMLCH1` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `LU1206782309` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `120678230` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/mutual_funds/world/summary?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "summary": {
            "symbol": "0P0001LCQ3",
            "name": "JNL Small Cap Index Fund (I)",
            "fund_family": "Jackson National",
            "fund_type": "Small Blend",
            "currency": "USD",
            "share_class_inception_date": "2021-04-26",
            "ytd_return": -0.02986,
            "expense_ratio_net": 0.001,
            "yield": 0,
            "nav": 10.09,
            "min_investment": 0,
            "turnover_rate": 0.32,
            "net_assets": 2400762112,
            "overview": "The fund invests, normally, at least 80% of its assets in the stocks...",
            "people": [
                {
                    "name": "John Doe",
                    "tenure_since": "2018-01-01"
                }
            ]
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### mutual_fund.summary

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 基金代码 |
| `name` | string | 基金名称 |
| `fund_family` | string | 基金家族（管理公司） |
| `fund_type` | string | 基金类型 |
| `currency` | string | 计价币种 |
| `share_class_inception_date` | string | 份额类别成立日期（`YYYY-MM-DD`） |
| `ytd_return` | double | 年初至今收益率 |
| `expense_ratio_net` | double | 净费率（费用比率） |
| `yield` | double | 收益率（股息/分红收益） |
| `nav` | double | 单位净值（NAV） |
| `min_investment` | double | 最低投资额 |
| `turnover_rate` | double | 换手率 |
| `net_assets` | integer | 净资产规模 |
| `overview` | string | 基金简介 |
| `people[].name` | string | 基金经理姓名 |
| `people[].tenure_since` | string | 任职起始日期（`YYYY-MM-DD`） |
| `status` | string | 请求状态（`ok`） |

> 与 ETF Summary 的差异：本接口多出 `people`（基金经理列表）、`min_investment` 字段，且无 `last_price`。
