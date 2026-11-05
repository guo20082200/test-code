# Performance — ETF 绩效接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/world/performance` 接口提供全球交易所交易基金的全面绩效数据，返回追踪收益率和年度收益率等详细指标。适用于跨时间段对比 ETF 表现、评估投资潜力。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/world/performance` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/etfs/world/performance` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | ETF 代码 | `IVV` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000BVZ697` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US4642872000` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `464287200` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/etfs/world/performance?symbol=IVV&apikey=your_api_key
```

## 响应示例

```json
{
    "etf": {
        "performance": {
            "trailing_returns": [
                {
                    "period": "ytd",
                    "share_class_return": -0.0751,
                    "category_return": 0.1484
                }
            ],
            "annual_total_returns": [
                {
                    "year": 2021,
                    "share_class_return": 0.2866,
                    "category_return": 0
                }
            ]
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### etf.performance

| 字段 | 类型 | 说明 |
|---|---|---|
| `trailing_returns[].period` | string | 追踪期（如 `ytd`、`1_month`、`3_month`、`1_year`、`3_year`、`5_year`、`10_year`） |
| `trailing_returns[].share_class_return` | double | 该追踪期内份额类别收益率 |
| `trailing_returns[].category_return` | double | 同期同类别基金平均收益率 |
| `annual_total_returns[].year` | integer | 年份 |
| `annual_total_returns[].share_class_return` | double | 该年度份额类别总收益率 |
| `annual_total_returns[].category_return` | double | 该年度同类别基金平均收益率 |
| `status` | string | 请求状态（`ok`） |

> `share_class_return` 与 `category_return` 对比可快速判断该 ETF 相对同类基金的超额/落后表现。
