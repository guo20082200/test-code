# Currency Conversion — 货币转换接口

> 官方文档：https://twelvedata.com/docs/currencies/currency-conversion

## 概述

`/currency_conversion` 接口返回指定货币对（外汇和加密货币）的实时汇率，并直接计算指定金额转换后的数量。适用于获取两种货币间的最新换算值，可用于财务报表、电商交易和旅行预算等场景。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /currency_conversion` |
| API credits | `1` / symbol |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/currency_conversion` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | ✱ 是 | 货币对，外汇或加密货币，斜杠（`/`）分隔，如 `EUR/USD` 或 `BTC/ETH` | `EUR/USD` |
| `amount` | double | ✱ 是 | 要转换成报价货币的基础货币金额，范围 ≥ `0` | `100` |
| `date` | string | 否 | 非空时返回指定日期或时间的汇率。格式 `2006-01-02` 或 `2006-01-02T15:04:05`。按交易所本地时区解析，可用 `timezone` 参数指定时区 | `2024-06-01T15:04:05` |
| `format` | string | 否 | 响应格式。支持：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | 下载 CSV 文件时使用的分隔符 | `;`（默认） |
| `dp` | integer | 否 | 数据的小数位数 | `5`（默认） |
| `timezone` | string | 否 | 输出 datetime 的时区。支持：`Exchange`（交易所本地时间）、`UTC`、IANA 时区名称（如 `America/New_York`、`Asia/Singapore`，**区分大小写**） | `UTC` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/currency_conversion?symbol=EUR/USD&amount=100&apikey=your_api_key
```

## 响应示例

```json
{
    "symbol": "USD/JPY",
    "rate": 105.12,
    "amount": 12824.64,
    "timestamp": 1602714051
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 货币对 |
| `rate` | double | 汇率值 |
| `amount` | double | 按 `rate` 换算后的目标货币金额 |
| `timestamp` | integer | 汇率时间戳（Unix 秒），时区由 `timezone` 参数决定 |

## 与 Exchange Rate 的区别

| 对比项 | `/exchange_rate` | `/currency_conversion` |
|---|---|---|
| 用途 | 仅查询汇率 | 查询汇率并直接换算金额 |
| 额外必填参数 | 无 | `amount` |
| 额外响应字段 | 无 | `amount`（换算结果） |
