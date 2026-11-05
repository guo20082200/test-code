# Direct Holders — 直接持股人接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/direct_holders` 接口提供个人或实体在公司官方股东名册中记录的直接持股明细。适用于了解公司内部股权分布、识别主要股东与评估股东集中度。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /direct_holders` |
| API credits | `1500` / symbol（与 Institutional/Fund holders 同为最贵档位） |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上；**Tadawul（沙特交易所）支持目前为 beta** |
| 完整 URL | `https://api.twelvedata.com/direct_holders` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码；优先股用点号分隔，如 `BRK.A`、`BRK.B` | `7203` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的交易所 | `NASDAQ` |
| `mic_code` | string | 可选 | 市场标识码（ISO 10383） | `XNAS` |
| `country` | string | 可选 | 交易国家，如 `United States` 或 `US` | `United States` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/direct_holders?symbol=7203&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "7203",
        "name": "Elm Co.",
        "currency": "SAR",
        "exchange": "Tadawul",
        "mic_code": "XSAU",
        "exchange_timezone": "Asia/Riyadh"
    },
    "direct_holders": [
        {
            "entity_name": "Public Investment Fund (Investment Company)",
            "date_reported": "2025-03-13",
            "shares": 53600000,
            "value": 43148000000,
            "percent_held": 0
        }
    ]
}
```

## 响应字段说明

### meta（标的元信息）

| 字段 | 说明 |
|---|---|
| `symbol` / `name` | 标的代码 / 公司名称 |
| `currency` | 计价币种 |
| `exchange` / `mic_code` | 交易所 / 市场标识码 |
| `exchange_timezone` | 交易所时区（IANA） |

### direct_holders[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `entity_name` | string | 持股实体名称（个人或机构） |
| `date_reported` | string | 申报日期（`YYYY-MM-DD`） |
| `shares` | integer | 持股数量 |
| `value` | integer | 持股市值 |
| `percent_held` | double | 持股占比（部分市场可能为 0，表示未披露） |

> 与 Institutional/Fund holders 的响应结构一致；本接口数据来自公司官方股东名册，覆盖范围更广（含非美市场，如沙特 Tadawul）。
