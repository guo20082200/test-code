# Twelve Data — Time Series（时间序列）接口文档

## 1. 接口概述

`time_series` 端点用于获取指定金融标的的详细历史行情数据，返回两部分主要内容：

- **meta（元数据）**：标的基本信息（代码、交易所、币种等）；
- **values（时间序列数据集）**：按时间先后排列的数据点，包含开、高、低、收（OHLC）四类价格，适用标的还包含成交量。

该接口适合获取完整的历史价格数据，用于分析或可视化场景。

- **接口地址**：`https://api.twelvedata.com/time_series`
- **API 积分消耗**：每个 symbol 消耗 `1` 点
- **需求等级**：高需求（High demand）接口

## 2. 本地请求示例

IDEA HTTP Client 示例（见 `aaa.http`）：

```http
###  7eccf8efb5cf4a2397282c118df0306b
GET https://api.twelvedata.com/time_series?symbol=AAPL&interval=1min&apikey=demo
Content-Type: application/json

{}
```

cURL 示例：

```bash
curl "https://api.twelvedata.com/time_series?symbol=AAPL&interval=1min&apikey=demo"
```

## 3. 请求参数

> **注意**：`symbol`、`isin`、`figi`、`cusip` 四个参数中**至少必填一个**。

### 3.1 标的识别参数

| 参数 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 四选一 | 标的代码（Ticker），如 `AAPL`、`EUR/USD`、`ETH/BTC` | `AAPL` |
| `figi` | string | 四选一 | 按 FIGI 编码查询。仅 Ultra（个人）/ Enterprise（企业）及以上套餐可用 | `BBG000B9Y5X2` |
| `isin` | string | 四选一 | 按国际证券识别码（ISIN）查询。所有套餐可用；响应中返回 ISIN 需要 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 四选一 | 按 CUSIP 编码查询。所有套餐可用；响应中返回 CUSIP 需要 CUSIP Data 附加包 | `594918104` |

### 3.2 行情配置参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `interval` | string | **必填** | 相邻两个时间点之间的间隔 | `1min`、`5min`、`15min`、`30min`、`45min`、`1h`、`2h`、`4h`、`8h`、`1day`、`1week`、`1month` | — |
| `exchange` | string | 可选 | 标的上市交易所 | `NASDAQ` | — |
| `mic_code` | string | 可选 | ISO 10383 标准的市场识别码（MIC） | `XNAS` | — |
| `country` | string | 可选 | 标的交易所在国家，如 `United States` 或 `US` | `United States` | — |
| `type` | string | 可选 | 标的所属资产类别 | 见下方资产类别列表 | — |

`type` 可选值：`American Depositary Receipt`（美国存托凭证）、`Bond`（债券）、`Bond Fund`（债券基金）、`Closed-end Fund`（封闭式基金）、`Common Stock`（普通股）、`Depositary Receipt`（存托凭证）、`Digital Currency`（数字货币）、`ETF`、`Exchange-Traded Note`（交易所交易票据）、`Global Depositary Receipt`（全球存托凭证）、`Limited Partnership`（有限合伙）、`Mutual Fund`（共同基金）、`Physical Currency`（实物货币）、`Preferred Stock`（优先股）、`REIT`（房地产信托）、`Right`（权利）、`Structured Product`（结构化产品）、`Trust`（信托）、`Unit`（单位）、`Warrant`（权证）

### 3.3 输出控制参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `outputsize` | integer | 可选 | 返回的数据点数量，范围 `1` ~ `5000`。未设置日期参数时默认 `30`，设置了日期参数则取最大值 | `100` | `30` |
| `format` | string | 可选 | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | 可选 | CSV 响应数据的分隔符 | `,` | `;` |
| `dp` | integer | 可选 | 浮点数小数位数，范围 [0, 11]。默认根据数值自动确定（`-1` 表示自动） | `4` | `-1` |
| `order` | string | 可选 | 输出排序方式 | `asc`（升序）、`desc`（降序） | `desc` |
| `timezone` | string | 可选 | 输出时间所用的时区，支持三种写法：1. `Exchange`（交易所本地时间）；2. `UTC`（标准 UTC 时间）；3. IANA 时区名（如 `America/New_York`、`Asia/Singapore`，区分大小写）。**限制**：仅对日内周期（小于 1 天）有效；`1day`/`1week`/`1month` 周期下该参数被忽略，数据严格按交易所本地时间返回 | `Asia/Shanghai` | `Exchange` |
| `prepost` | boolean | 可选 | 是否包含盘前/盘后行情。仅 Pro（个人）/ Venture（企业）及以上套餐可用；仅支持美股 `1min`、`5min`、`15min`、`30min` 周期；OHLC 值不含成交量 | `true` | `false` |
| `previous_close` | boolean | 可选 | 是否在数据中包含前一收盘价。为 `true` 时在当前对象中追加前一根 K 线的收盘价 | `true` | `false` |
| `adjust` | string | 可选 | 价格复权模式 | `all`（全复权）、`splits`（拆股复权）、`dividends`（分红复权）、`none`（不复权） | `splits` |

### 3.4 日期范围参数

| 参数 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `date` | string | 可选 | 指定获取某一确切日期的数据。可以是具体日期，也可以是自然语言 `today` / `yesterday` | `2021-10-27` |
| `start_date` | string | 可选 | 数据起始时间，格式 `2006-01-02` 或 `2006-01-02T15:04:05`，可与 `end_date` 组合使用 | `2024-08-22T15:04:05` |
| `end_date` | string | 可选 | 数据截止时间，详见 `start_date` 说明 | `2024-08-22T16:04:05` |

**`start_date` / `end_date` 的时区规则：**

- 外汇与加密货币：默认 UTC；
- 股票：默认交易所所在地时区（如 AAPL 为 `America/New_York`）；
- 若同时提供了 `timezone` 参数，则 `start_date` / `end_date` 按该时区解释。

**示例：**

1. `&symbol=AAPL&start_date=2019-08-09T15:50:00&…`
   返回从纽约时间 2019-08-09 15:50:00 起至今的所有记录；
2. `&symbol=EUR/USD&timezone=Asia/Singapore&start_date=2019-08-09T15:50:00&…`
   返回从新加坡时间 2019-08-09 15:50:00 起至今的所有记录；
3. `&symbol=ETH/BTC&timezone=Europe/Zurich&start_date=2019-08-09T15:50:00&end_date=2019-08-09T15:55:00&...`
   返回苏黎世时间 2019-08-09 15:50:00 至 15:55:00 之间的所有记录。

## 4. 响应字段

### 4.1 `meta`（object）— 请求元信息

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 所请求标的的代码 |
| `interval` | string | 相邻数据点之间的时间间隔 |
| `currency` | string | 交易标的的币种 |
| `exchange_timezone` | string | 标的所在交易所的时区 |
| `exchange` | string | 标的所在交易所名称 |
| `mic_code` | string | 交易所的市场识别码（MIC） |
| `type` | string | 标的所属资产类别 |

### 4.2 `values`（object 数组）— 时间序列数据点

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `datetime` | string | 该 K 线（bar）**开盘**时刻的时间戳（交易所本地时间） |
| `open` | string | 当前 K 线开盘价 |
| `high` | string | 当前 K 线期间的最高价 |
| `low` | string | 当前 K 线期间的最低价 |
| `close` | string | 当前 K 线收盘价 |
| `volume` | string | 当前 K 线期间的成交量 |

### 4.3 `status`（string）— 响应状态

## 5. 请求与响应示例

**请求：**

```bash
curl "https://api.twelvedata.com/time_series?symbol=AAPL&interval=1min&apikey=demo"
```

**响应：**

```json
{
    "meta": {
        "symbol": "AAPL",
        "interval": "1min",
        "currency": "USD",
        "exchange_timezone": "America/New_York",
        "exchange": "NASDAQ",
        "mic_code": "XNAS",
        "type": "Common Stock"
    },
    "values": [
        {
            "datetime": "2021-09-16 15:59:00",
            "open": "148.73500",
            "high": "148.86000",
            "low": "148.73000",
            "close": "148.85001",
            "volume": "624277"
        }
    ],
    "status": "ok"
}
```
