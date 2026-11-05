# Fund Holders — 共同基金持仓接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/fund_holders` 接口提供共同基金持有公司股票的比例明细，包括持股数量、占总股本比例及基金名称。适用于了解共同基金对特定公司的投资布局。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /fund_holders` |
| API credits | `1500` / symbol（与 Institutional/Direct holders 同为最贵档位，调用前务必评估必要性） |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/fund_holders` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码；优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的交易所 | `NASDAQ` |
| `mic_code` | string | 可选 | 市场标识码（ISO 10383） | `XNAS` |
| `country` | string | 可选 | 交易国家，如 `United States` 或 `US` | `United States` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/fund_holders?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "name": "Apple Inc",
        "currency": "USD",
        "exchange": "NASDAQ",
        "mic_code": "XNAS",
        "exchange_timezone": "America/New_York"
    },
    "fund_holders": [
        {
            "entity_name": "VANGUARD INDEX FUNDS-Vanguard Total Stock Market Index Fund",
            "date_reported": "2025-09-30",
            "shares": 467135722,
            "value": 129695568698,
            "percent_held": 0.031600002
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

### fund_holders[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `entity_name` | string | 基金全称（含基金家族前缀，如 "VANGUARD INDEX FUNDS-Vanguard Total Stock Market Index Fund"） |
| `date_reported` | string | 申报日期（`YYYY-MM-DD`） |
| `shares` | integer | 持股数量 |
| `value` | integer | 持股市值 |
| `percent_held` | double | 持股占比 |

> 与 Institutional holders 的响应结构完全相同，差异仅在持仓实体类型（共同基金 vs 机构投资者）。
