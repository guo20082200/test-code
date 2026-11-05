# MFs Types — 共同基金类型接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/type` 接口返回各类共同基金类型的详细信息（如股票型、债券型、平衡型基金），帮助用户了解可用的投资选项。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/type` |
| API credits | `1` / request |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/type` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `country` | string | 否 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `fund_type` | string | 否 | 按基金类型过滤 | `Large Blend` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/mutual_funds/type?apikey=your_api_key
```

## 响应示例

```json
{
    "result": {
        "Singapore": [
            "Property - Indirect Asia",
            "Sector Equity Water",
            "SGD Bond",
            "Singapore Equity",
            "Taiwan Large-Cap Equity"
        ],
        "United States": [
            "Asia-Pacific ex-Japan Equity",
            "EUR Flexible Allocation - Global",
            "Euro Short Bond PP",
            "Large Blend",
            "Other Allocation"
        ]
    },
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `result` | object | 以市场/国家名称为 key 的基金类型分组，值为该市场下的类型名称数组 |
| `status` | string | 请求状态（`ok`） |

## 使用场景

- 配合 `/mutual_funds/list` 的 `fund_type` 过滤参数，先枚举类型再按类型检索基金列表
- 按资产类别（股票/债券/平衡/行业主题等）进行组合多元化分析
