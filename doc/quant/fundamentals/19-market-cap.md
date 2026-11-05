# Market Capitalization — 市值历史接口

> 官方文档：https://twelvedata.com/docs#market-capitalization

## 概述

`/market_cap` 接口提供公司在指定时间段内的历史市值数据。返回市值时间序列，便于追踪公司市场价值的变化。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /market_cap` |
| API credits | `5` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/market_cap` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 按交易所名称过滤 | `NASDAQ` |
| `mic_code` | string | 可选 | 按市场标识码（MIC，ISO 10383）过滤 | `XNAS` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |

### 时间与分页参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `start_date` | string | 否 | 市值数据起始日期，从此日期起返回数据。格式 `2006-01-02` | `2023-01-01` |
| `end_date` | string | 否 | 市值数据结束日期，返回至该日期（含）。格式 `2006-01-02` | `2023-12-31` |
| `page` | integer | 否 | 页码 | `1`（默认） |
| `outputsize` | integer | 否 | 响应中的记录数 | `10`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/market_cap?symbol=AAPL&apikey=your_api_key
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
    "market_cap": [
        {
            "date": "2025-07-14",
            "value": 3115906555944
        },
        {
            "date": "2025-07-11",
            "value": 3153843487457
        },
        {
            "date": "2025-07-10",
            "value": 3172513237217
        }
    ]
}
```

## 响应字段说明

### meta — 公司元信息

| 字段 | 说明 |
|---|---|
| `symbol` | 公司代码 |
| `name` | 公司名称 |
| `currency` | 交易币种（ISO 4217） |
| `exchange` / `mic_code` | 上市交易所名称 / 市场标识码 |
| `exchange_timezone` | 交易所时区 |

### market_cap — 市值序列

| 字段 | 类型 | 说明 |
|---|---|---|
| `date` | string | 市值日期（`YYYY-MM-DD`） |
| `value` | integer | 市值（美元） |
