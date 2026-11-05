# Twelve Data — Commodities（大宗商品目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录`、`02-外汇交易对`、`03-加密货币交易对`、`04-ETF目录`、`05-基金目录`、`07-债券目录`

## 1. 接口概述

`commodities` 端点返回**每日更新**的全部可用大宗商品交易对列表，覆盖贵金属、牲畜、软性商品、谷物等类别。每条记录包含交易对代码、名称、类别及简短描述。

- **接口地址**：`https://api.twelvedata.com/commodities`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/commodities?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 按标的代码过滤 | `XAU/USD` | — |
| `category` | string | 按商品类别过滤（如 `Precious Metal`、`Energy Resource`、`Industrial Metal`） | `Precious Metal` | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `page` | integer | 结果分页页码 | `2` | `1` |
| `outputsize` | integer | 返回的记录数量。**不传则返回全部可用记录** | `50` | — |

注意：commodities **不支持** `show_plan` / `include_delisted` 参数，响应中也无标识符字段（figi / isin / cusip）。

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 返回的记录数量 |
| `data` | object 数组 | 大宗商品列表，见下表 |
| `status` | string | 响应状态 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 商品代码（斜杠分隔或纯代码），如 `XAU/USD`、`WTI/USD`、`HG1` |
| `name` | string | 商品全称，如 `Gold Spot`（黄金现货） |
| `category` | string | 商品类别，如 `Precious Metal`（贵金属） |
| `description` | string | 商品简短描述 |

## 5. 响应示例

```json
{
    "count": 100,
    "data": [
        {
            "category": "Energy Resource",
            "description": "Spot price per barrel of West Texas Intermediate crude oil.",
            "name": "Crude Oil WTI Spot",
            "symbol": "WTI/USD"
        },
        {
            "category": "Industrial Metal",
            "description": "Spot price per pound of copper.",
            "name": "Copper Spot",
            "symbol": "HG1"
        },
        {
            "category": "Precious Metal",
            "description": "Spot price per troy ounce of gold.",
            "name": "Gold Spot",
            "symbol": "XAU/USD"
        }
    ],
    "status": "ok"
}
```
