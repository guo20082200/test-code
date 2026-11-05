# Dividends Calendar — 股息日历接口

> 官方文档：https://twelvedata.com/docs#dividends-calendar

## 概述

`/dividends_calendar` 接口返回指定日期范围内即将发生和已经发生的股息事件日程。通过 `start_date` 和 `end_date` 参数可获取派息公司列表，包括除息日和股息金额。适用于追踪股息派发和围绕股息日程规划投资策略。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /dividends_calendar` |
| API credits | `40` / symbol |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/dividends_calendar` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 否 | 标的代码，优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 否 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 否 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 否 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 否 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 否 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 否 | 交易所在国家，如 `United States` 或 `US` | `US` |
| `start_date` | string | 否 | 起始日期，仅返回除息日在此日期及之后的数据。格式 `2006-01-02` | `2024-01-01` |
| `end_date` | string | 否 | 结束日期，仅返回除息日在此日期及之前的数据。格式 `2006-01-02` | `2024-12-31` |
| `outputsize` | integer | 否 | 返回数据条数，范围 `1`–`500`。未设置日期参数时默认 `100`，设置了日期参数则为最大值 | `100` |
| `page` | integer | 否 | 页码 | `1`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

> 说明：不传 `symbol` 等标的参数时，返回全市场（跨公司）的股息日历。

## 请求示例

```
GET https://api.twelvedata.com/dividends_calendar?start_date=2024-01-01&end_date=2024-12-31&apikey=your_api_key
```

## 响应示例

```json
[
    {
        "symbol": "MSFT",
        "mic_code": "XNGS",
        "exchange": "NASDAQ",
        "ex_date": "2024-02-14",
        "amount": 0.75
    }
]
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 标的代码 |
| `mic_code` | string | 市场标识码 |
| `exchange` | string | 交易所名称 |
| `ex_date` | string | 除息日（`YYYY-MM-DD`） |
| `amount` | double | 每股股息金额 |
