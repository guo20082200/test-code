# Exchange Rate — 汇率接口

> 官方文档：https://twelvedata.com/docs/currencies/currency-conversion

## 概述

`/exchange_rate` 接口返回指定货币对的实时汇率，涵盖外汇和加密货币。返回两种货币之间的当前汇率值，适用于快速获取金融交易或市场分析所需的最新汇率。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /exchange_rate` |
| API credits | `1` / symbol |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/exchange_rate` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | ✱ 是 | 货币对，外汇或加密货币，斜杠（`/`）分隔，如 `EUR/USD` 或 `BTC/ETH` | `EUR/USD` |
| `date` | string | 否 | 非空时返回指定日期或时间的汇率。格式 `2006-01-02` 或 `2006-01-02T15:04:05`。按交易所本地时区解析，可用 `timezone` 参数指定时区 | `2024-06-01T15:04:05` |
| `format` | string | 否 | 响应格式。支持：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | 下载 CSV 文件时使用的分隔符 | `;`（默认） |
| `dp` | integer | 否 | 数据的小数位数 | `5`（默认） |
| `timezone` | string | 否 | 输出 datetime 的时区。支持：`Exchange`（交易所本地时间）、`UTC`、IANA 时区名称（如 `America/New_York`、`Asia/Singapore`，**区分大小写**） | `UTC` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/exchange_rate?symbol=EUR/USD&apikey=your_api_key
```

## 响应示例

```json
{
    "symbol": "USD/JPY",
    "rate": 105.12,
    "timestamp": 1602714051
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 货币对 |
| `rate` | double | 汇率值（1 单位基础货币兑报价货币的数量） |
| `timestamp` | integer | 汇率时间戳（Unix 秒），时区由 `timezone` 参数决定 |
