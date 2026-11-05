# Twelve Data — Quote（实时报价）接口文档

## 1. 接口概述

`quote` 端点用于获取指定金融标的的**实时行情快照**，返回最新价、开高低收、成交量、涨跌幅、52 周高低等关键信息。适合需要追踪个股、ETF 或其他证券最新价格变动的场景。

与 `time_series` 返回一段历史序列不同，`quote` 只返回**当前时刻**的一条汇总数据，且**响应为扁平对象**（没有 `meta` / `values` 包装层）。

- **接口地址**：`https://api.twelvedata.com/quote`
- **API 积分消耗**：每个 symbol 消耗 `1` 点
- **需求等级**：高需求（High demand）接口

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/quote?symbol=AAPL&apikey=demo"
```

## 3. 请求参数

> **注意**：`symbol`、`figi`、`isin`、`cusip` 四个参数中**至少必填一个**。

### 3.1 标的识别参数

| 参数 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 四选一 | 标的代码（Ticker） | `AAPL` |
| `figi` | string | 四选一 | 按金融工具全球标识符（FIGI）过滤。仅 Ultra（个人）/ Enterprise（企业）及以上套餐可用 | `BBG000BHTMY7` |
| `isin` | string | 四选一 | 按国际证券识别码（ISIN）查询。所有套餐可用；响应中返回 ISIN 需要 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 四选一 | 按 CUSIP 编码查询。所有套餐可用；响应中返回 CUSIP 需要 CUSIP Data 附加包 | `594918104` |

### 3.2 行情配置参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `interval` | string | 可选 | 报价对应的 K 线周期 | `1min`、`5min`、`15min`、`30min`、`45min`、`1h`、`2h`、`4h`、`8h`、`1day`、`1week`、`1month` | `1day` |
| `exchange` | string | 可选 | 标的上市交易所 | `NASDAQ` | — |
| `mic_code` | string | 可选 | ISO 10383 标准的市场识别码（MIC） | `XNAS` | — |
| `country` | string | 可选 | 标的交易所所在国家，如 `United States` 或 `US` | `United States` | — |
| `type` | string | 可选 | 标的所属资产类别 | 与 `time_series` 相同的 20 种取值（`Common Stock`、`ETF`、`Digital Currency` 等） | — |

### 3.3 输出控制参数

| 参数 | 类型 | 必填 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `volume_time_period` | integer | 可选 | 平均成交量（`average_volume`）的计算周期数 | `9` | `9` |
| `format` | string | 可选 | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | 可选 | 下载 CSV 文件时使用的分隔符 | `;` | `;` |
| `prepost` | boolean | 可选 | 是否包含盘前/盘后行情。仅 Pro（个人）/ Venture（企业）及以上套餐可用；仅支持美股 `1min`、`5min`、`15min`、`30min` 周期；OHLC 值不含成交量。开启后响应中追加 `extended_*` 系列字段 | `true` | `false` |
| `eod` | boolean | 可选 | 为 `true` 时返回**已收盘交易日**的数据 | `true` | `false` |
| `rolling_period` | integer | 可选 | 滚动涨跌幅（`rolling_change`）的计算小时数，范围 [1, 168] | `24` | `24` |
| `dp` | integer | 可选 | 浮点数小数位数，范围 [0, 11] | `5` | `5` |
| `timezone` | string | 可选 | 输出时间所用时区，支持 `Exchange` / `UTC` / IANA 时区名（**区分大小写**）。**限制**：仅对日内周期（小于 1 天）有效；`1day`/`1week`/`1month` 周期下被忽略，数据严格按交易所本地时间返回 | `Asia/Shanghai` | `Exchange` |

## 4. 响应字段

> 响应为**扁平 JSON 对象**，无 `meta` / `values` 包装。

### 4.1 标的与时间信息

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 传入的标的代码 |
| `name` | string | 标的名称 |
| `exchange` | string | 标的所在交易所 |
| `mic_code` | string | 交易所市场识别码（MIC），适用于股票、ETF、共同基金、债券 |
| `currency` | string | 计价币种，适用于股票、ETF、共同基金、债券 |
| `datetime` | string | 当前 K 线开盘时刻（按指定时区显示） |
| `timestamp` | integer | 当前 K 线开盘时刻的 Unix 时间戳 |
| `last_quote_at` | integer | 最后一分钟 K 线的 Unix 时间戳 |
| `is_market_open` | boolean | 市场是否开盘中（`true` 开市 / `false` 休市） |

### 4.2 价格与成交量

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `open` | string | 当前 K 线开盘价 |
| `high` | string | 当前 K 线期间的最高价 |
| `low` | string | 当前 K 线期间的最低价 |
| `close` | string | 当前 K 线收盘价（即最新价） |
| `volume` | string | 当前 K 线期间成交量（并非所有标的有此字段） |
| `previous_close` | string | 前一根 K 线的收盘价 |
| `average_volume` | string | 指定周期（`volume_time_period`）的平均成交量（并非所有标的有此字段） |

### 4.3 涨跌幅

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `change` | string | `close - previous_close` |
| `percent_change` | string | `(close - previous_close) / previous_close × 100` |
| `rolling_1d_change` | string | 1 天周期的滚动涨跌幅（仅加密货币） |
| `rolling_7d_change` | string | 7 天周期的滚动涨跌幅（仅加密货币） |
| `rolling_change` | string | 按 `rolling_period` 参数指定周期的滚动涨跌幅（仅加密货币） |

### 4.4 `fifty_two_week`（object）— 52 周指标

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `low` | string | 52 周最低价 |
| `high` | string | 52 周最高价 |
| `low_change` | string | 当前价 − 52 周最低价 |
| `high_change` | string | 当前价 − 52 周最高价 |
| `low_change_percent` | string | 相对 52 周最低价的涨跌百分比 |
| `high_change_percent` | string | 相对 52 周最高价的涨跌百分比 |
| `range` | string | 52 周最低价到最高价的区间字符串 |

### 4.5 盘前/盘后字段（仅 `prepost=true` 时返回）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `extended_change` | string | 常规收盘价与最新盘外价之差 |
| `extended_percent_change` | string | 常规收盘价与最新盘外价的涨跌百分比 |
| `extended_price` | string | 最新盘外价 |
| `extended_timestamp` | integer | 最新盘外价的 Unix 时间戳 |

## 5. 响应示例

```json
{
    "symbol": "AAPL",
    "name": "Apple Inc",
    "exchange": "NASDAQ",
    "mic_code": "XNAS",
    "currency": "USD",
    "datetime": "2021-09-16",
    "timestamp": 1631772000,
    "last_quote_at": 1631772000,
    "open": "148.44000",
    "high": "148.96840",
    "low": "147.22099",
    "close": "148.85001",
    "volume": "67903927",
    "previous_close": "149.09000",
    "change": "-0.23999",
    "percent_change": "-0.16097",
    "average_volume": "83571571",
    "rolling_1d_change": "123.123",
    "rolling_7d_change": "123.123",
    "rolling_change": "123.123",
    "is_market_open": false,
    "fifty_two_week": {
        "low": "103.10000",
        "high": "157.25999",
        "low_change": "45.75001",
        "high_change": "-8.40999",
        "low_change_percent": "44.37440",
        "high_change_percent": "-5.34782",
        "range": "103.099998 - 157.259995"
    },
    "extended_change": "0.09",
    "extended_percent_change": "0.05",
    "extended_price": "125.22",
    "extended_timestamp": 1649845281
}
```

## 6. 三个接口对比

| 对比项 | quote | time_series | time_series/cross |
| --- | --- | --- | --- |
| 用途 | 单一标的的实时行情快照 | 单一标的的历史行情序列 | 两种标的的交叉行情序列 |
| 积分消耗 | 每 symbol `1` 点 | 每 symbol `1` 点 | 每次请求 `5` 点 |
| 标的指定方式 | `symbol` / `figi` / `isin` / `cusip` 四选一 | 同左 | `base` + `quote` 组合 |
| 响应结构 | 扁平对象（一条） | `meta` + `values` 数组 | `meta` + `values` 数组 |
| 独有信息 | 涨跌幅、52 周指标、开收盘状态、盘外价 | 历史序列 | 交叉汇率 |
