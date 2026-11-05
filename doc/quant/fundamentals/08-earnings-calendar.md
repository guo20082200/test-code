# Earnings Calendar — 财报日历接口

> 官方文档：https://twelvedata.com/docs#earnings-calendar

## 概述

`/earnings_calendar` 接口返回指定日期范围内的公司财报发布日程，默认返回当天的财报数据。可通过 `start_date` 和 `end_date` 自定义日期范围。适用于跟踪即将发布的财报并围绕关键财务公告进行规划。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /earnings_calendar` |
| API credits | `40` / request |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/earnings_calendar` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `exchange` | string | 否 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 否 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 否 | 交易所在国家，如 `United States` 或 `US` | `United States` |
| `start_date` | string | 否 | 起始日期，可单独使用或与 `end_date` 组合。格式 `2006-01-02` 或 `2006-01-02T15:04:05` | `2024-04-01` |
| `end_date` | string | 否 | 结束日期，可单独使用或与 `start_date` 组合。格式 `2006-01-02` 或 `2006-01-02T15:04:05` | `2024-04-30` |
| `format` | string | 否 | 响应格式。支持：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | 下载 CSV 文件时使用的分隔符 | `;`（默认） |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `2`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

> **限制**：单次请求最多返回 `1200` 条记录。如超出限制，请缩小日期范围或按 `exchange`、`mic_code`、`country` 过滤。

## 请求示例

```
GET https://api.twelvedata.com/earnings_calendar?start_date=2024-04-01&end_date=2024-04-30&apikey=your_api_key
```

## 响应示例

```json
{
    "earnings": {
        "2020-04-30": [
            {
                "symbol": "BR",
                "name": "Broadridge Financial Solutions Inc",
                "currency": "USD",
                "exchange": "NYSE",
                "mic_code": "XNYS",
                "country": "United States",
                "time": "Time Not Supplied",
                "eps_estimate": 1.72,
                "eps_actual": 1.67,
                "difference": -0.05,
                "surprise_prc": -2.9
            }
        ]
    },
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `earnings` | object | 以日期（`YYYY-MM-DD`）为 key 的财报数据分组 |
| `earnings.{date}[].symbol` | string | 标的代码 |
| `earnings.{date}[].name` | string | 公司名称 |
| `earnings.{date}[].currency` | string | 币种 |
| `earnings.{date}[].exchange` / `mic_code` | string | 交易所名称 / 市场标识码 |
| `earnings.{date}[].country` | string | 国家 |
| `earnings.{date}[].time` | string | 发布时间（如 `After Hours`、`Before Hours`、`Time Not Supplied`） |
| `earnings.{date}[].eps_estimate` | double | 预估 EPS |
| `earnings.{date}[].eps_actual` | double | 实际 EPS |
| `earnings.{date}[].difference` | double | 实际与预估差值 |
| `earnings.{date}[].surprise_prc` | double | 意外百分比（%） |
| `status` | string | 请求状态（`ok`） |
