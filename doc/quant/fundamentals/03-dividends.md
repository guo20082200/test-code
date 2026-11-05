# Dividends — 股息历史接口

> 官方文档：https://twelvedata.com/docs#dividends

## 概述

`/dividends` 接口返回指定股票的历史股息数据，通常覆盖超过十年。返回内容包括每次股息派发的除息日、金额和频率等信息。适用于追踪股息历史或评估股票收益潜力。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /dividends` |
| API credits | `20` / symbol |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/dividends` |

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
| `country` | string | 可选 | 交易所在国家，如 `United States` 或 `US` | `US` |

### 时间范围参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `range` | string | 否 | 获取股息数据的时间范围。支持：`last`（最近一次）、`next`（下一次）、`1m`、`3m`、`6m`、`ytd`、`1y`、`2y`、`5y`、`full`（全部）。与 `start_date`/`end_date` 同时提供时，`range` 优先 | `1y`（默认 `last`） |
| `start_date` | string | 否 | 起始日期，仅返回此日期及之后的股息。格式 `2006-01-02`。与 `range` 同时提供时 `range` 优先 | `2024-01-01` |
| `end_date` | string | 否 | 结束日期，仅返回此日期及之前的股息。格式 `2006-01-02`。与 `range` 同时提供时 `range` 优先 | `2024-12-31` |
| `adjust` | boolean | 否 | 是否对数据进行调整 | `true`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/dividends?symbol=AAPL&range=1y&apikey=your_api_key
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
        "exchange_timezone": "America/New_York"
    },
    "dividends": [
        {
            "ex_date": "2021-08-06",
            "amount": 0.22
        }
    ]
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `meta.symbol` | string | 标的代码 |
| `meta.name` | string | 公司名称 |
| `meta.currency` | string | 币种 |
| `meta.exchange` / `meta.mic_code` | string | 交易所名称 / 市场标识码 |
| `meta.exchange_timezone` | string | 交易所时区 |
| `dividends[].ex_date` | string | 除息日（`YYYY-MM-DD`） |
| `dividends[].amount` | double | 每股股息金额 |
