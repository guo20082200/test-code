# Twelve Data — Forex Pairs（外汇交易对目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录`、`03-加密货币交易对`、`04-ETF目录`、`05-基金目录`、`06-大宗商品目录`、`07-债券目录`

## 1. 接口概述

`forex_pairs` 端点返回**每日更新**的全部可用外汇货币对列表，每条记录包含货币对代码、所属组别（主要/次要/奇异货币对）及基础/报价货币。

- **接口地址**：`https://api.twelvedata.com/forex_pairs`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/forex_pairs?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 按货币对代码过滤 | `EUR/USD` | — |
| `currency_base` | string | 按基础货币（Base）过滤 | `EUR` | — |
| `currency_quote` | string | 按报价货币（Quote）过滤 | `USD` | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `page` | integer | 结果分页页码 | `2` | `1` |
| `outputsize` | integer | 返回的记录数量。**不传则返回全部可用记录** | `50` | — |

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 返回的记录数量 |
| `data` | object 数组 | 外汇货币对列表，见下表 |
| `status` | string | 响应状态 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 货币对代码（ISO 4217，斜杠分隔），如 `EUR/USD` |
| `currency_group` | string | 货币对所属组别：`Major`（主要）、`Minor`（次要）、`Exotic`（奇异）、`Exotic-Cross`（奇异交叉） |
| `currency_base` | string | 基础货币名称（ISO 4217） |
| `currency_quote` | string | 报价货币名称（ISO 4217） |

## 5. 响应示例

```json
{
    "count": 100,
    "data": [
        {
            "symbol": "EUR/USD",
            "currency_group": "Major",
            "currency_base": "EUR",
            "currency_quote": "USD"
        }
    ],
    "status": "ok"
}
```
