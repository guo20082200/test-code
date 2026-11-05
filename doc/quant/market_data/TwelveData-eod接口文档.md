# Twelve Data — EOD（收盘价）接口文档

## 1. 接口概述

`eod`（End of Day，日终收盘）端点用于获取指定金融标的在**交易日收盘时**的收盘价及相关元数据。适合获取股票、ETF 等证券的日线级历史数据，用于追踪长期表现、比较每日市场走势。

与 `price`（最新实时价）不同，`eod` 返回的是**已收盘交易日**的价格——收盘后不再变化，适合报表、回测等对稳定性要求高的场景。

- **接口地址**：`https://api.twelvedata.com/eod`
- **API 积分消耗**：每个 symbol 消耗 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/eod?symbol=AAPL&apikey=demo"
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

### 3.2 行情限定参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 |
| --- | --- | --- | --- | --- |
| `exchange` | string | 可选 | 标的上市交易所 | `NASDAQ` |
| `mic_code` | string | 可选 | ISO 10383 标准的市场识别码（MIC） | `XNAS` |
| `country` | string | 可选 | 标的交易所所在国家，如 `United States` 或 `US` | `United States` |
| `type` | string | 可选 | 标的所属资产类别 | 与其他接口相同的 20 种取值（`Common Stock`、`ETF`、`Digital Currency` 等） |

### 3.3 输出控制参数

| 参数 | 类型 | 必填 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `date` | string | 可选 | 指定获取**某一具体日期**的收盘数据，格式 `yyyy-MM-dd`。不传则返回最近一个交易日的收盘数据 | `2006-01-02` | — |
| `prepost` | boolean | 可选 | 是否包含盘前/盘后行情。仅 Pro（个人）/ Venture（企业）及以上套餐可用；仅支持美股 `1min`、`5min`、`15min`、`30min` 周期；OHLC 值不含成交量 | `true` | `false` |
| `dp` | integer | 可选 | 浮点数小数位数，范围 [0, 11] | `5` | `5` |

## 4. 响应字段

响应为**扁平 JSON 对象**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 传入的标的代码 |
| `exchange` | string | 标的所在交易所 |
| `mic_code` | string | 交易所市场识别码（MIC） |
| `currency` | string | 计价币种 |
| `datetime` | string | 收盘数据所属的 K 线开盘时刻（按指定时区显示） |
| `close` | string | 最近的日终收盘价 |

## 5. 响应示例

```json
{
    "symbol": "AAPL",
    "exchange": "NASDAQ",
    "mic_code": "XNAS",
    "currency": "USD",
    "datetime": "2021-09-16",
    "close": "148.79"
}
```

## 6. 五个接口对比

| 对比项 | price | eod | quote | time_series | time_series/cross |
| --- | --- | --- | --- | --- | --- |
| 用途 | 最新价（单个数据点） | 某日收盘价（单个数据点） | 实时行情快照 | 历史行情序列 | 交叉行情序列 |
| 积分消耗 | 每 symbol `1` 点 | 每 symbol `1` 点 | 每 symbol `1` 点 | 每 symbol `1` 点 | 每次请求 `5` 点 |
| 响应结构 | 扁平，仅 `price` | 扁平（`close` + 标的信息） | 扁平（完整快照） | `meta` + `values` 数组 | `meta` + `values` 数组 |
| 价格性质 | 实时（或最近可用） | 收盘后固定不变 | 实时快照（含涨跌幅、52 周） | 按区间批量返回 | 实时计算 |
| 典型场景 | 轮询价格、轻量展示 | 报表、回测基准、日终对账 | 看板详情、涨跌幅、52 周指标 | K 线图、相关性分析 | 异种标的换算（如 JPY/BTC） |
