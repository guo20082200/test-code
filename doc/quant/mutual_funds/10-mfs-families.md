# MFs Families — 共同基金家族接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/family` 接口返回共同基金家族（由同一投资公司管理的基金组）的全面列表。适用于探索或对比不同基金家族、了解各公司提供的投资选择范围，以及挖掘特定基金家族内的投资机会。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/family` |
| API credits | `1` / request |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/family` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `country` | string | 否 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `fund_family` | string | 否 | 按管理该基金的投资公司过滤 | `Jackson National` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/mutual_funds/family?apikey=your_api_key
```

## 响应示例

```json
{
    "result": {
        "India": [
            "Aberdeen Standard Fund Managers Limited",
            "Aditya Birla Sun Life AMC Ltd"
        ],
        "United States": [
            "Aegon Asset Management UK PLC",
            "Ampega Investment GmbH",
            "Aviva SpA"
        ]
    },
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `result` | object | 以国家名称为 key 的基金家族分组，值为该国家下的基金家族（管理公司）名称数组 |
| `status` | string | 请求状态（`ok`） |

## 使用场景

- 配合 `/mutual_funds/list` 的 `fund_family` 过滤参数，先枚举家族再按家族检索基金列表
- 构建市场/国家维度的基金管理公司目录
