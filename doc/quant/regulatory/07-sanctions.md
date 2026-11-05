# Sanctioned Entities — 制裁实体接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/sanctions/{source}` 接口提供指定监管机构发布的受制裁实体完整清单，覆盖 OFAC、UK、EU、AU 等来源。可获取受制裁个人、组织及其他实体的详细信息，支撑合规与风险管理流程。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /sanctions/{source}` |
| API credits | `50` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/sanctions/ofac` |

> 与本组其他接口不同：本接口**按 request 计费、不含标的标识参数**，返回的是清单数据而非特定标的详情，响应中也没有 `meta` 分组。

## 路径参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `source` | string（路径） | ✱ 是 | 制裁来源。可选值：`ofac`（美国财政部海外资产控制办公室）、`uk`（英国）、`eu`（欧盟）、`au`（澳大利亚） |

## 请求参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `apikey` | string | ✱ 是 | API 密钥 |

## 请求示例

```
GET https://api.twelvedata.com/sanctions/ofac?apikey=your_api_key
```

## 响应示例

```json
{
    "sanctions": [
        {
            "symbol": "LOKESHMACH",
            "name": "Lokesh Machines Ltd.",
            "mic_code": "NSE",
            "country": "India",
            "sanction": {
                "source": "ofac",
                "program": "RUSSIA-EO14024",
                "notes": "Block",
                "lists": [
                    {
                        "name": "SDN List",
                        "published_at": "2024-10-30"
                    }
                ]
            }
        }
    ],
    "count": 143,
    "status": "ok"
}
```

## 响应字段说明

### sanctions[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 实体关联标的代码 |
| `name` | string | 实体名称 |
| `mic_code` | string | 上市市场标识码 |
| `country` | string | 所属国家 |
| `sanction.source` | string | 制裁来源（ofac/uk/eu/au） |
| `sanction.program` | string | 制裁计划名称（如 `RUSSIA-EO14024`） |
| `sanction.notes` | string | 制裁措施说明（如 `Block` 冻结） |
| `sanction.lists[].name` | string | 所属制裁名单（如 `SDN List`） |
| `sanction.lists[].published_at` | string | 名单发布日期（`YYYY-MM-DD`） |

### 其他

| 字段 | 类型 | 说明 |
|---|---|---|
| `count` | integer | 返回记录总数 |
| `status` | string | 请求状态（`ok`） |

## 使用场景

- 交易前合规筛查：检查标的或交易对手是否在制裁名单上
- 风控系统集成：定期拉取 OFAC/EU/UK/AU 清单更新本地黑名单库
