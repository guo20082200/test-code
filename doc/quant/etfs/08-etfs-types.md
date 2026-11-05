# ETFs Types — ETF 类型接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/type` 接口按市场（如新加坡、美国）返回 ETF 类别的简明列表，包括 "Equity Precious Metals"、"Large Blend" 等类型。支持定向投资研究和组合多元化分析。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/type` |
| API credits | `1` / request |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/etfs/type` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `country` | string | 否 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `fund_type` | string | 否 | 按基金类型过滤 | `Large Blend` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/etfs/type?apikey=your_api_key
```

## 响应示例

```json
{
    "result": {
        "Singapore": [
            "Property - Indirect Asia",
            "Sector Equity Water"
        ],
        "United States": [
            "Asia-Pacific ex-Japan Equity",
            "EUR Flexible Allocation - Global"
        ]
    },
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `result` | object | 以市场/国家名称为 key 的 ETF 类型分组，值为该市场下的类型名称数组 |
| `status` | string | 请求状态（`ok`） |

## 使用场景

- 配合 `/etfs/list` 的 `fund_type` 过滤参数，先枚举类型再按类型检索 ETF 列表
- 按资产类别（股票/债券/商品/行业主题等）进行组合多元化分析
