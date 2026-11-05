# Institutional Holders — 机构持仓接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/institutional_holders` 接口提供机构投资者（如养老基金、保险公司、投资公司）持有公司股票的占比与数量明细。适用于了解大型机构在公司股权结构中的影响力与参与度。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /institutional_holders` |
| API credits | `1500` / symbol（Twelve Data 全部接口中最贵的档位，调用前务必评估必要性） |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/institutional_holders` |

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
GET https://api.twelvedata.com/institutional_holders?symbol=AAPL&apikey=your_api_key
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
    "institutional_holders": [
        {
            "entity_name": "Vanguard Group Inc",
            "date_reported": "2025-09-30",
            "shares": 1399427162,
            "value": 388536977757,
            "percent_held": 0.0947
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

### institutional_holders[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `entity_name` | string | 机构实体名称 |
| `date_reported` | string | 申报日期（`YYYY-MM-DD`） |
| `shares` | integer | 持股数量 |
| `value` | integer | 持股市值 |
| `percent_held` | double | 持股占比（如 0.0947 = 9.47%） |
