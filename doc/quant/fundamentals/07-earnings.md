# Earnings — 财报收益接口

> 官方文档：https://twelvedata.com/docs#earnings

## 概述

`/earnings` 接口返回指定公司的综合财报收益数据，包括预估 EPS（每股收益）和实际 EPS。提供历史收益信息，便于追踪公司的财务表现随时间的变化。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /earnings` |
| API credits | `20` / symbol |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/earnings` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 可选 | 交易所在国家，如 `United States` 或 `US` | `United States` |

### 筛选与输出参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `type` | string | 否 | 标的所属资产类别。支持：`American Depositary Receipt`、`Bond`、`Bond Fund`、`Closed-end Fund`、`Common Stock`、`Depositary Receipt`、`Digital Currency`、`ETF`、`Exchange-Traded Note`、`Global Depositary Receipt`、`Limited Partnership`、`Mutual Fund`、`Physical Currency`、`Preferred Stock`、`REIT`、`Right`、`Structured Product`、`Trust`、`Unit`、`Warrant` | `Common Stock` |
| `period` | string | 否 | 收益类型，仅返回 1 条记录。设置后 `start_date`/`end_date`/`outputsize` 参数将被忽略。支持：`latest`（最新）、`next`（下一次） | `latest` |
| `outputsize` | integer | 否 | 返回数据条数，范围 `1`–`1000`。未设置日期参数时默认 `10`，设置了日期参数则为最大值 | `10` |
| `start_date` | string | 否 | 数据请求起始日期。格式 `YYYY-MM-DD` | `2024-04-01` |
| `end_date` | string | 否 | 数据请求结束日期。格式 `YYYY-MM-DD` | `2024-04-30` |
| `format` | string | 否 | 响应数据格式。支持：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | CSV 响应数据的分隔符 | `;`（默认） |
| `dp` | integer | 否 | 响应数据小数位数，范围 `[0, 11]` | `2`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/earnings?symbol=AAPL&apikey=your_api_key
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
    "earnings": [
        {
            "date": "2020-04-30",
            "time": "After Hours",
            "eps_estimate": 2.09,
            "eps_actual": 2.55,
            "difference": 0.46,
            "surprise_prc": 22.01
        }
    ],
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `meta.*` | object | 标的元信息（symbol、name、currency、exchange、mic_code、exchange_timezone） |
| `earnings[].date` | string | 财报发布日期（`YYYY-MM-DD`） |
| `earnings[].time` | string | 发布时间（如 `After Hours` 盘后） |
| `earnings[].eps_estimate` | double | 预估 EPS |
| `earnings[].eps_actual` | double | 实际 EPS |
| `earnings[].difference` | double | 实际与预估差值 |
| `earnings[].surprise_prc` | double | 意外百分比（%），实际相对预估的偏离 |
| `status` | string | 请求状态（`ok`） |
