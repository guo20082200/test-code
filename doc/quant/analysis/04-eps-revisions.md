# EPS Revisions — EPS 修正接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/eps_revisions` 接口提供分析师对公司季度与年度每股收益（EPS）预测的最新修正情况，反映过去一周和一个月内预估的上调/下调次数。适用于监控市场对公司财务表现预期的变化。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /eps_revisions` |
| API credits | `20` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/eps_revisions` |

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
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/eps_revisions?symbol=AAPL&apikey=your_api_key
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
    "eps_revision": [
        {
            "date": "2022-09-30",
            "period": "current_quarter",
            "up_last_week": 1,
            "up_last_month": 5,
            "down_last_week": 0,
            "down_last_month": 0
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

### eps_revision[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `date` | string | 财报期截止日期（`YYYY-MM-DD`） |
| `period` | string | 财报周期（如 `current_quarter` 本季） |
| `up_last_week` | integer | 过去一周 EPS 预估上调的分析师数量 |
| `up_last_month` | integer | 过去一个月 EPS 预估上调的分析师数量 |
| `down_last_week` | integer | 过去一周 EPS 预估下调的分析师数量 |
| `down_last_month` | integer | 过去一个月 EPS 预估下调的分析师数量 |
| `status` | string | 请求状态（`ok`） |
