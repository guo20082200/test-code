# Twelve Data — Cryptocurrencies（加密货币交易对目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录`、`02-外汇交易对`、`04-ETF目录`、`05-基金目录`、`06-大宗商品目录`、`07-债券目录`

## 1. 接口概述

`cryptocurrencies` 端点返回**每日更新**的全部可用加密货币交易对列表，每条记录包含交易对代码、可用交易所列表及基础/报价货币名称。适合需要展示可交易加密资产目录或维护最新市场列表的应用。

- **接口地址**：`https://api.twelvedata.com/cryptocurrencies`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/cryptocurrencies?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 按交易对代码过滤 | `BTC/USD` | — |
| `exchange` | string | 按交易所名称过滤，如 `Binance`、`Coinbase` | `Binance` | — |
| `currency_base` | string | 按基础货币过滤 | `BTC` | — |
| `currency_quote` | string | 按报价货币过滤 | `USD` | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `page` | integer | 结果分页页码 | `2` | `1` |
| `outputsize` | integer | 返回的记录数量。**不传则返回全部可用记录** | `50` | — |

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 返回的记录数量 |
| `data` | object 数组 | 加密货币交易对列表，见下表 |
| `status` | string | 响应状态 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 交易对代码（斜杠分隔），如 `BTC/USD` |
| `available_exchanges` | string 数组 | 该加密货币可交易的交易所列表 |
| `currency_base` | string | 基础货币名称（如 `Bitcoin`） |
| `currency_quote` | string | 报价货币名称（如 `US Dollar`） |

## 5. 响应示例

```json
{
    "count": 100,
    "data": [
        {
            "symbol": "BTC/USD",
            "available_exchanges": [
                "ABCC",
                "Allcoin",
                "BTC-Alpha",
                "BTCTurk",
                "Bibox",
                "n.exchange",
                "p2pb2b",
                "xBTCe"
            ],
            "currency_base": "Bitcoin",
            "currency_quote": "US Dollar"
        }
    ],
    "status": "ok"
}
```
