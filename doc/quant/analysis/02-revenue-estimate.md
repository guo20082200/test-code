# Revenue Estimate — 营收预估接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/revenue_estimate` 接口基于分析师预估，提供公司季度与年度营收预测数据。适用于洞察公司预期业绩表现，可将预测销售额与历史数据或其他公司的预估进行对比。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /revenue_estimate` |
| API credits | `20` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/revenue_estimate` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `exchange` | string | 可选 | 按交易所名称过滤 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383） | `XNAS` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/revenue_estimate?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "name": "Apple Inc",
        "currency": "USD",
        "exchange_timezone": "America/New_York",
        "exchange": "NASDAQ",
        "mic_code": "XNGS",
        "type": "Common Stock"
    },
    "revenue_estimate": [
        {
            "date": "2022-09-30",
            "period": "current_quarter",
            "number_of_analysts": 24,
            "avg_estimate": 88631500000,
            "low_estimate": 85144300000,
            "high_estimate": 92794900000,
            "year_ago_sales": 83360000000,
            "sales_growth": 0.06
        }
    ],
    "status": "ok"
}
```

## 响应字段说明

### meta（标的元信息）

| 字段 | 说明 |
|---|---|
| `symbol` / `name` | 标的代码 / 公司名称 |
| `currency` | 计价币种 |
| `exchange_timezone` | 交易所时区（IANA） |
| `exchange` / `mic_code` | 交易所 / 市场标识码 |
| `type` | 标的类型（如 Common Stock） |

### revenue_estimate[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `date` | string | 财报期截止日期（`YYYY-MM-DD`） |
| `period` | string | 财报周期（如 `current_quarter` 本季） |
| `number_of_analysts` | integer | 参与预估的分析师数量 |
| `avg_estimate` | double | 平均营收预估 |
| `low_estimate` | double | 最低营收预估 |
| `high_estimate` | double | 最高营收预估 |
| `year_ago_sales` | double | 去年同期实际营收 |
| `sales_growth` | double | 预期销售增长率 |
| `status` | string | 请求状态（`ok`） |
