# Twelve Data — Price（最新价）接口文档

## 1. 接口概述

`price` 端点用于获取指定金融标的的**最新市场价格**，只返回一个数据点：当前（或最近一次可用的）成交价格。

这是 Twelve Data 中最轻量的行情接口——只关心"现在多少钱"时用它，不需要开高低收、成交量等信息。需要完整行情快照时改用 `quote` 接口。

- **接口地址**：`https://api.twelvedata.com/price`
- **API 积分消耗**：每个 symbol 消耗 `1` 点
- **需求等级**：高需求（High demand）接口

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/price?symbol=AAPL&apikey=demo"
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
| `type` | string | 可选 | 标的所属资产类别 | 与 `time_series` / `quote` 相同的 20 种取值（`Common Stock`、`ETF`、`Digital Currency` 等） |

### 3.3 输出控制参数

| 参数 | 类型 | 必填 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `format` | string | 可选 | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | 可选 | 下载 CSV 文件时使用的分隔符 | `;` | `;` |
| `prepost` | boolean | 可选 | 是否包含盘前/盘后行情。仅 Pro（个人）/ Venture（企业）及以上套餐可用；仅支持美股 `1min`、`5min`、`15min`、`30min` 周期；OHLC 值不含成交量 | `true` | `false` |
| `dp` | integer | 可选 | 浮点数小数位数，范围 [0, 11] | `5` | `5` |

## 4. 响应字段

响应为**扁平 JSON 对象**，只有一个字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `price` | string | 实时（或最近一次可用的）成交价格 |

注意：

- 价格以**字符串**形式返回，使用时需自行转换为数值类型；
- 市场休市时返回的是**最近一次可用的成交价**，并非实时价格。

## 5. 响应示例

```json
{
    "price": "200.99001"
}
```

## 6. 四个接口对比

| 对比项 | price | quote | time_series | time_series/cross |
| --- | --- | --- | --- | --- |
| 用途 | 最新价（单个数据点） | 实时行情快照 | 历史行情序列 | 交叉行情序列 |
| 积分消耗 | 每 symbol `1` 点 | 每 symbol `1` 点 | 每 symbol `1` 点 | 每次请求 `5` 点 |
| 响应结构 | 扁平对象，仅 `price` 一个字段 | 扁平对象（完整快照） | `meta` + `values` 数组 | `meta` + `values` 数组 |
| 典型场景 | 轮询价格、轻量展示 | 看板详情、涨跌幅、52 周指标 | K 线图、回测、相关性分析 | 异种标的换算（如 JPY/BTC） |
