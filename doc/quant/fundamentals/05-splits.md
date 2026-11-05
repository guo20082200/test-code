# Splits — 拆股历史接口

> 官方文档：https://twelvedata.com/docs#splits

## 概述

`/splits` 接口返回指定公司的历史拆股事件数据，包括每次拆股的日期及对应拆股比例。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /splits` |
| API credits | `20` / symbol |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/splits` |

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

### 时间范围参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `range` | string | 否 | 返回数据的时间范围。支持：`last`、`1m`、`3m`、`6m`、`ytd`、`1y`、`2y`、`5y`、`full` | `full`（默认 `last`） |
| `start_date` | string | 否 | 数据筛选起始日期。格式 `2006-01-02` | `2020-01-01` |
| `end_date` | string | 否 | 数据筛选结束日期。格式 `2006-01-02` | `2020-12-31` |

## 请求示例

```
GET https://api.twelvedata.com/splits?symbol=AAPL&range=full&apikey=your_api_key
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
    "splits": [
        {
            "date": "2020-08-31",
            "description": "4-for-1 split",
            "ratio": 0.25,
            "from_factor": 4,
            "to_factor": 1
        }
    ]
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `meta.*` | object | 标的元信息（symbol、name、currency、exchange、mic_code、exchange_timezone） |
| `splits[].date` | string | 拆股生效日期（`YYYY-MM-DD`） |
| `splits[].description` | string | 拆股描述，如 `4-for-1 split` |
| `splits[].ratio` | double | 拆股比率 |
| `splits[].from_factor` | integer | 拆股前系数 |
| `splits[].to_factor` | integer | 拆股后系数 |
