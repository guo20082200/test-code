# Earnings Estimate — 盈利预估接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/earnings_estimate` 接口提供分析师对特定公司每股收益（EPS）的预估数据，覆盖即将到来的季度与年度周期。适用于跟踪和对比不同时间段的预期财务表现，评估公司未来盈利能力。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /earnings_estimate` |
| API credits | `20` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/earnings_estimate` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的交易所 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383） | `XNAS` |
| `country` | string | 可选 | 标的交易国家，如 `United States` 或 `US` | `United States` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/earnings_estimate?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "name": "Apple Inc",
        "currency": "USD",
        "exchange_timezone": "America/New_York",
        "exchange": "NASDAQ",
        "mic_code": "XNGS",
        "type": "Common Stock"
    },
    "earnings_estimate": [
        {
            "date": "2022-09-30",
            "period": "current_quarter",
            "number_of_analysts": 27,
            "avg_estimate": 1.26,
            "low_estimate": 1.13,
            "high_estimate": 1.35,
            "year_ago_eps": 1.24
        }
    ],
    "status": "ok"
}
```

## 响应字段说明

### meta（标的元信息）

| 字段 | 说明 |
|---|---|
| `symbol` / `name` | 标的代码 / 公司名称 |
| `currency` | 计价币种 |
| `exchange_timezone` | 交易所时区（IANA） |
| `exchange` / `mic_code` | 交易所 / 市场标识码 |
| `type` | 标的类型（如 Common Stock） |

### earnings_estimate[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `date` | string | 财报期截止日期（`YYYY-MM-DD`） |
| `period` | string | 财报周期（如 `current_quarter` 本季、`next_quarter` 下季、`current_year` 本年、`next_year` 明年） |
| `number_of_analysts` | integer | 参与预估的分析师数量 |
| `avg_estimate` | double | 平均 EPS 预估 |
| `low_estimate` | double | 最低 EPS 预估 |
| `high_estimate` | double | 最高 EPS 预估 |
| `year_ago_eps` | double | 去年同期实际 EPS |
| `status` | string | 请求状态（`ok`） |
