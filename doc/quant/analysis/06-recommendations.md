# Recommendations — 分析师建议接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/recommendations` 接口提供特定股票的分析师观点汇总，给出分类为 Strong Buy（强力买入）、Buy（买入）、Hold（持有）或 Sell（卖出）的平均建议，并附带数值化建议评分，快速呈现基于专家分析的市场情绪。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /recommendations` |
| API credits | `100` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/recommendations` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `country` | string | 可选 | 标的交易国家，如 `United States` 或 `US` | `United States` |
| `exchange` | string | 可选 | 标的交易所名称，如 `Nasdaq`、`NSE` | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383） | `XNAS` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/recommendations?symbol=AAPL&apikey=your_api_key
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
    "trends": {
        "current_month": {
            "strong_buy": 13,
            "buy": 20,
            "hold": 8,
            "sell": 0,
            "strong_sell": 0
        },
        "previous_month": {
            "strong_buy": 13,
            "buy": 20,
            "hold": 8,
            "sell": 0,
            "strong_sell": 0
        },
        "2_months_ago": {
            "strong_buy": 13,
            "buy": 20,
            "hold": 8,
            "sell": 0,
            "strong_sell": 0
        },
        "3_months_ago": {
            "strong_buy": 13,
            "buy": 20,
            "hold": 8,
            "sell": 0,
            "strong_sell": 0
        }
    },
    "rating": 8.2,
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

### trends（近 4 个月评级人数分布）

每个月度分组（`current_month`、`previous_month`、`2_months_ago`、`3_months_ago`）包含：

| 字段 | 类型 | 说明 |
|---|---|---|
| `strong_buy` | integer | 强力买入评级人数 |
| `buy` | integer | 买入评级人数 |
| `hold` | integer | 持有评级人数 |
| `sell` | integer | 卖出评级人数 |
| `strong_sell` | integer | 强力卖出评级人数 |

### 其他

| 字段 | 类型 | 说明 |
|---|---|---|
| `rating` | double | 数值化综合建议评分（如 8.2） |
| `status` | string | 请求状态（`ok`） |
