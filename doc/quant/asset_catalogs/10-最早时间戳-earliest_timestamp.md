# Twelve Data — Earliest Timestamp（最早时间戳）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `07-债券目录`、`08-代码搜索`、`09-交叉上市`

## 1. 接口概述

`earliest_timestamp` 端点返回指定标的在**指定时间间隔下最早可用的行情数据时间**。用于确定各类资产（股票、外汇等）历史数据的起始点，了解数据覆盖的时间范围——拉取长周期历史数据前可先用它探测数据起点。

- **接口地址**：`https://api.twelvedata.com/earliest_timestamp`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/earliest_timestamp?symbol=AAPL&interval=1day&apikey=demo"
```

## 3. 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `symbol` | string | 四选一 | 标的代码 | `AAPL` | — |
| `figi` | string | 四选一 | 金融工具全球标识符（FIGI），需 Ultra / Enterprise 及以上套餐 | `BBG000B9XRY4` | — |
| `isin` | string | 四选一 | 国际证券识别码（ISIN），返回 ISIN 需 ISIN Data 附加包 | `US0378331005` | — |
| `cusip` | string | 四选一 | CUSIP 编码，返回 CUSIP 需 CUSIP Data 附加包 | `594918104` | — |
| `interval` | string | 是 | 时间间隔，12 种取值 | `1min` / `5min` / `15min` / `30min` / `45min` / `1h` / `2h` / `4h` / `8h` / `1day` / `1week` / `1month` | — |
| `exchange` | string | 否 | 交易场所（用于消歧多市场同代码标的） | `Nasdaq` | — |
| `mic_code` | string | 否 | 市场识别码（MIC，ISO 10383 标准） | `XNAS` | — |
| `timezone` | string | 否 | 输出时间的时区，支持三种取值：`Exchange`（交易所本地时间）/ `UTC` / IANA 时区名（如 `America/New_York`、`Asia/Singapore`，**区分大小写**） | `Exchange` / `UTC` / IANA 时区名 | `Exchange` |

**⚠️ 标的识别四选一**：`symbol` / `figi` / `isin` / `cusip` 至少提供一个。

**⚠️ timezone 的间隔限制**：`timezone` 参数仅对日内间隔（小于 1day）生效；对 `1day` / `1week` / `1month`，该参数被忽略，数据严格按交易所本地时间返回。

## 4. 响应字段

响应为一个扁平对象（无包装层），仅两个字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `datetime` | string | 最早可用时间，格式取决于 interval（如 1day 为 `1980-12-12`，1min 含时分秒） |
| `unix_time` | integer | 该时间对应的 UNIX 时间戳 |

## 5. 响应示例

```json
{
    "datetime": "1980-12-12",
    "unix_time": 345479400
}
```
