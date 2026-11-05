# Insider Transactions — 内部人交易接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/insider_transactions` 接口提供公司内部人（高管、董事等）执行交易的详细数据，包括内部人姓名、职位、交易类型、股数、交易日期及每股价格。适用于跟踪内部人活动、了解其对公司股票的潜在态度。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /insider_transactions` |
| API credits | `200` / symbol |
| 最低套餐 | **Pro（个人）/ Venture（商业）** 及以上（本组中门槛最低） |
| 完整 URL | `https://api.twelvedata.com/insider_transactions` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，如 `AAPL`、`TSLA`；优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的交易所，如 `Nasdaq`、`NSE` | `NASDAQ` |
| `mic_code` | string | 可选 | 市场标识码（ISO 10383） | `XNAS` |
| `country` | string | 可选 | 交易国家，如 `United States` 或 `US` | `United States` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/insider_transactions?symbol=AAPL&apikey=your_api_key
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
    "insider_transactions": [
        {
            "full_name": "ADAMS KATHERINE L",
            "position": "General Counsel",
            "date_reported": "2021-05-03",
            "is_direct": true,
            "shares": 17000,
            "value": 2257631,
            "description": "Sale at price 132.57 - 133.93 per share."
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

### insider_transactions[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `full_name` | string | 内部人姓名 |
| `position` | string | 内部人职位（如 General Counsel 总法律顾问） |
| `date_reported` | string | 申报日期（`YYYY-MM-DD`） |
| `is_direct` | boolean | 是否为直接交易 |
| `shares` | integer | 交易股数 |
| `value` | integer | 交易金额 |
| `description` | string | 交易描述（含成交价区间，如 "Sale at price 132.57 - 133.93 per share."） |

## 使用场景

- 内部人情绪监测：批量统计高管买入/卖出行为
- 合规审计：与 `/edgar_filings/archive` 的 `144` 表单交叉验证
