# IPO Calendar — IPO 日历接口

> 官方文档：https://twelvedata.com/docs#ipo-calendar

## 概述

`/ipo_calendar` 接口返回首次公开发行（IPO）的详细信息，包括已发生、当天发生或计划中的 IPO。可获取公司名称、IPO 日期、发行细节等数据，便于高效跟踪和监控 IPO 活动。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /ipo_calendar` |
| API credits | `40` / request |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/ipo_calendar` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `exchange` | string | 否 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 否 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 否 | 交易所在国家，如 `United States` 或 `US` | `United States` |
| `start_date` | string | 否 | 结果中包含的最早 IPO 日期。格式 `2006-01-02` | `2021-01-01` |
| `end_date` | string | 否 | 结果中包含的最晚 IPO 日期。格式 `2006-01-02` | `2021-12-31` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/ipo_calendar?start_date=2021-01-01&end_date=2021-12-31&apikey=your_api_key
```

## 响应示例

```json
{
    "2025-07-16": [
        {
            "symbol": "DWACU",
            "name": "Digital World Acquisition Corp.",
            "exchange": "NASDAQ",
            "mic_code": "XNAS",
            "price_range_low": 10,
            "price_range_high": 10,
            "offer_price": 0,
            "currency": "USD",
            "shares": 0
        }
    ]
}
```

## 响应字段说明

响应为以 IPO 日期（`YYYY-MM-DD`）为 key 的对象，每个日期对应一个 IPO 数组：

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 标的代码 |
| `name` | string | 公司名称 |
| `exchange` | string | 上市交易所名称 |
| `mic_code` | string | 市场标识码 |
| `price_range_low` | double | 发行价区间下限 |
| `price_range_high` | double | 发行价区间上限 |
| `offer_price` | double | 实际发行价 |
| `currency` | string | 币种 |
| `shares` | integer | 发行股数 |
