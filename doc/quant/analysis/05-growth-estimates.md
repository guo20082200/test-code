# Growth Estimates — 增长预估接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/growth_estimates` 接口提供分析师对公司多个时间跨度增长率的共识预测，聚合多位分析师对 EPS 与营收等关键财务指标的预估均值。适用于从专家分析角度全面了解公司预期业绩。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /growth_estimates` |
| API credits | `20` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/growth_estimates` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `country` | string | 可选 | 标的交易国家，如 `United States` 或 `US` | `United States` |
| `exchange` | string | 可选 | 标的交易所 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383） | `XNAS` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/growth_estimates?symbol=AAPL&apikey=your_api_key
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
    "growth_estimates": {
        "current_quarter": 0.016,
        "next_quarter": 0.01,
        "current_year": 0.087,
        "next_year": 0.055999998,
        "next_5_years_pa": 0.094799995,
        "past_5_years_pa": 0.23867
    },
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

### growth_estimates

| 字段 | 类型 | 说明 |
|---|---|---|
| `current_quarter` | double | 本季度预期增长率 |
| `next_quarter` | double | 下季度预期增长率 |
| `current_year` | double | 本年度预期增长率 |
| `next_year` | double | 下年度预期增长率 |
| `next_5_years_pa` | double | 未来 5 年年均（per annum）预期增长率 |
| `past_5_years_pa` | double | 过去 5 年年均实际增长率 |
| `status` | string | 请求状态（`ok`） |
