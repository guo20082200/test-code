# Twelve Data — Time Series Cross（交叉汇率时间序列）接口文档

## 1. 接口概述

`time_series/cross` 端点用于**实时计算**并返回异种类别的交叉汇率历史数据，支持外汇、加密货币或股票之间的组合（例如：用印度卢比计价的苹果公司股价、JPY/BTC 等）。返回内容包括：

- **meta（元数据）**：请求标的相关信息；
- **values（时间序列数据集）**：按时间**降序**排列的数据点，包含开、高、低、收（OHLC）四类价格。

- **接口地址**：`https://api.twelvedata.com/time_series/cross`
- **API 积分消耗**：每次请求消耗 `5` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/time_series/cross?base=JPY&quote=BTC&interval=1min&apikey=demo"
```

## 3. 请求参数

### 3.1 核心参数

| 参数 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `base` | string | **必填** | 基础货币（Base）代码 | `JPY` |
| `quote` | string | **必填** | 报价货币（Quote）代码 | `BTC` |
| `interval` | string | **必填** | 相邻两个时间点之间的间隔 | `1min` |

`interval` 可选值：`1min`、`5min`、`15min`、`30min`、`45min`、`1h`、`2h`、`4h`、`8h`、`1day`、`1week`、`1month`

### 3.2 基础货币（base）限定参数（均可选）

| 参数 | 说明 | 示例值 |
| --- | --- | --- |
| `base_type` | 基础标的的资产类别（参照 `/instrument_type` 端点） | `Physical Currency` |
| `base_exchange` | 基础标的所在交易所 | `Binance` |
| `base_mic_code` | 基础标的的市场识别码（MIC） | `XNGS` |

### 3.3 报价货币（quote）限定参数（均可选）

| 参数 | 说明 | 示例值 |
| --- | --- | --- |
| `quote_type` | 报价标的的资产类别（参照 `/instrument_type` 端点） | `Digital Currency` |
| `quote_exchange` | 报价标的所在交易所 | `Coinbase` |
| `quote_mic_code` | 报价标的的市场识别码（MIC） | `XNYS` |

### 3.4 输出控制参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `outputsize` | integer | 可选 | 返回的数据点数量，范围 `1` ~ `5000`。未设置日期参数时默认 `30`，设置了日期参数则取最大值 | `30` | `30` |
| `format` | string | 可选 | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | 可选 | CSV 文件的分隔符 | `;` | `;` |
| `dp` | integer | 可选 | 浮点数小数位数，范围 [0, 11] | `5` | `5` |
| `timezone` | string | 可选 | 输出时间所用的时区，支持三种写法：1. `Exchange`（交易所本地时间）；2. `UTC`（标准 UTC 时间）；3. IANA 时区名（如 `America/New_York`、`Asia/Singapore`，**区分大小写**） | `UTC` | — |
| `adjust` | boolean | 可选 | 是否进行复权调整 | `true` | `true` |
| `prepost` | boolean | 可选 | 是否包含盘前/盘后行情。仅 Pro（个人）/ Venture（企业）及以上套餐可用；仅支持美股 `1min`、`5min`、`15min`、`30min` 周期；OHLC 值不含成交量 | `false` | `false` |

### 3.5 日期范围参数

| 参数 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `start_date` | string | 可选 | 时间序列数据的起始日期 | `2025-01-01` |
| `end_date` | string | 可选 | 时间序列数据的截止日期 | `2025-01-31` |

## 4. 响应字段

### 4.1 `meta`（object）— 请求元信息

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `base_instrument` | string | 基础标的代码（如 `JPY/USD`） |
| `base_currency` | string | 基础货币 |
| `base_exchange` | string | 基础标的所在交易所（如 `PHYSICAL CURRENCY`） |
| `interval` | string | 相邻数据点之间的时间间隔 |
| `quote_instrument` | string | 报价标的代码（如 `BTC/USD`） |
| `quote_currency` | string | 报价货币 |
| `quote_exchange` | string | 报价标的所在交易所（如 `Coinbase Pro`） |

### 4.2 `values`（object 数组）— 时间序列数据点

> 数据按时间**降序**排列。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `datetime` | string | 该 K 线（bar）**开盘**时刻的时间戳（交易所本地时间） |
| `open` | string | 当前 K 线开盘价 |
| `high` | string | 当前 K 线期间的最高价 |
| `low` | string | 当前 K 线期间的最低价 |
| `close` | string | 当前 K 线收盘价 |

注意：与 `time_series` 不同，本接口的数据点**不含 `volume`（成交量）字段**。

## 5. 响应示例

```json
{
    "meta": {
        "base_instrument": "JPY/USD",
        "base_currency": "",
        "base_exchange": "PHYSICAL CURRENCY",
        "interval": "1min",
        "quote_instrument": "BTC/USD",
        "quote_currency": "",
        "quote_exchange": "Coinbase Pro"
    },
    "values": [
        {
            "datetime": "2025-02-28 14:30:00",
            "open": "0.0000081115665",
            "high": "0.0000081273069",
            "low": "0.0000081088287",
            "close": "0.0000081268066"
        }
    ]
}
```

## 6. 与 time_series 接口的主要区别

| 对比项 | time_series | time_series/cross |
| --- | --- | --- |
| 用途 | 查询单一标的的历史行情 | 实时计算两种标的的交叉行情 |
| 积分消耗 | 每 symbol `1` 点 | 每次请求 `5` 点 |
| 标的指定方式 | `symbol` / `isin` / `figi` / `cusip` 四选一 | `base` + `quote` 组合 |
| 成交量 | 含 `volume` 字段 | 不含 `volume` 字段 |
| 排序 | 默认 `desc`（可通过 `order` 切换） | 固定按时间降序 |
