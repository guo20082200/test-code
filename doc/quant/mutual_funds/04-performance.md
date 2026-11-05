# Performance — 共同基金绩效接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/performance` 接口提供全球共同基金的综合绩效数据，返回追踪收益率、年度收益率、季度收益率和销售费调整后收益等指标。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/performance` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/performance` |

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
GET https://api.twelvedata.com/mutual_funds/world/performance?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "performance": {
            "trailing_returns": [
                {
                    "period": "ytd",
                    "share_class_return": -0.02986,
                    "category_return": 0.2019,
                    "rank_in_category": 76
                }
            ],
            "annual_total_returns": [
                {
                    "year": 2024,
                    "share_class_return": 0.08546,
                    "category_return": 0.1119
                }
            ],
            "quarterly_total_returns": [
                {
                    "year": 2024,
                    "q1": 0.02358,
                    "q2": -0.03071,
                    "q3": 0.10099,
                    "q4": -0.00629
                }
            ],
            "load_adjusted_return": [
                {
                    "period": "1_year",
                    "return": 0.06139
                }
            ]
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### mutual_fund.performance

| 字段 | 类型 | 说明 |
|---|---|---|
| `trailing_returns[].period` | string | 追踪期（如 `ytd`、`1_month`、`3_month`、`1_year`、`3_year`、`5_year`、`10_year`） |
| `trailing_returns[].share_class_return` | double | 该追踪期内份额类别收益率 |
| `trailing_returns[].category_return` | double | 同期同类别基金平均收益率 |
| `trailing_returns[].rank_in_category` | integer | 同类排名（百分位） |
| `annual_total_returns[].year` | integer | 年份 |
| `annual_total_returns[].share_class_return` | double | 该年度份额类别总收益率 |
| `annual_total_returns[].category_return` | double | 该年度同类别基金平均收益率 |
| `quarterly_total_returns[].year` | integer | 年份 |
| `quarterly_total_returns[].q1`–`q4` | double | 该年度各季度收益率 |
| `load_adjusted_return[].period` | string | 计算周期（如 `1_year`） |
| `load_adjusted_return[].return` | double | 扣除销售费（load）后的收益率 |
| `status` | string | 请求状态（`ok`） |

> 与 ETF Performance 的差异：本接口多出 `rank_in_category`（同类排名）、`quarterly_total_returns`（季度收益）、`load_adjusted_return`（销售费调整后收益）。
