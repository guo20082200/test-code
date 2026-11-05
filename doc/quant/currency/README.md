# Twelve Data — Currencies（货币转换）接口文档

> 来源：https://twelvedata.com/docs/currencies/currency-conversion
>
> 提供外汇（Forex）与加密货币（Cryptocurrency）货币对的实时汇率及货币金额换算能力。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数 |
| 请求方式 | `GET` |
| 响应格式 | `JSON`（支持 `CSV`） |
| 计费单位 | API credits，`1` / symbol |

## 接口目录

| # | 文档 | 接口 | Credits | 说明 |
|---|---|---|---|---|
| 01 | [Exchange Rate（汇率）](01-exchange-rate.md) | `/exchange_rate` | 1 / symbol | 查询货币对实时/指定日期汇率 |
| 02 | [Currency Conversion（货币转换）](02-currency-conversion.md) | `/currency_conversion` | 1 / symbol | 汇率 + 指定金额的直接换算 |

## 通用说明

- **货币对格式**：斜杠（`/`）分隔，如 `EUR/USD`（外汇）、`BTC/ETH`（加密货币）
- **套餐限制**：两个接口均无套餐限制（所有套餐可用）
- **时区**：`date` 参数按交易所本地时区解析，可通过 `timezone` 参数指定输出时区（IANA 名称区分大小写）
