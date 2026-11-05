# Twelve Data — Cross Listings（交叉上市）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `07-债券目录`、`08-代码搜索`、`10-最早时间戳`

## 1. 接口概述

`cross_listings` 端点返回**每日更新**的指定标的**交叉上市列表**——同一证券在多个交易所上市的代码记录。适合找出某只证券的所有交易市场，获取跨市场完整交易信息。

- **接口地址**：`https://api.twelvedata.com/cross_listings`
- **API 积分消耗**：每次请求 `40` 点（⚠️ 本目录中消耗最高的接口）
- **套餐限制**：需 Grow（个人）或 Venture（企业）及以上套餐，免费套餐会返回错误

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/cross_listings?symbol=AAPL&apikey=demo"
```

## 3. 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `symbol` | string | 是 | 请求的标的代码 | `AAPL` | — |
| `exchange` | string | 否 | 标的交易场所（用于消歧多市场同代码标的） | `NASDAQ` | — |
| `mic_code` | string | 否 | 市场识别码（MIC，ISO 10383 标准） | `XNGS` | — |
| `country` | string | 否 | 交易所所属国家 | `United States` | — |

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `result` | object | 结果包装对象，见下表（与 funds / bonds 相同的包装结构） |

**`result` 对象：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 找到的交叉上市数量 |
| `list` | object 数组 | 交叉上市列表，见下表 |

**`result.list` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 各市场的标的代码（同一证券在不同市场可能不同，如 NVDA / NVDACL） |
| `name` | string | 标的名称 |
| `exchange` | string | 交易场所 |
| `mic_code` | string | 市场识别码（MIC） |

## 5. 响应示例

```json
{
    "result": {
        "count": 4,
        "list": [
            {
                "exchange": "NASDAQ",
                "mic_code": "XNGS",
                "name": "NVIDIA Corporation",
                "symbol": "NVDA"
            },
            {
                "exchange": "VSE",
                "mic_code": "XWBO",
                "name": "NVIDIA Corporation",
                "symbol": "NVDA"
            },
            {
                "exchange": "BVS",
                "mic_code": "XSGO",
                "name": "NVIDIA Corporation",
                "symbol": "NVDACL"
            },
            {
                "exchange": "BVS",
                "mic_code": "XSGO",
                "name": "NVIDIA Corporation",
                "symbol": "NVDA"
            }
        ]
    }
}
```
