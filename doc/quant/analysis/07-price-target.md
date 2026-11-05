# Price Target — 目标价接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/price_target` 接口提供金融分析师对证券未来价格的详细预测，包括最高、最低与平均目标价。适用于基于专家分析了解特定证券的潜在未来估值。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /price_target` |
| API credits | `75` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/price_target` |

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
GET https://api.twelvedata.com/price_target?symbol=AAPL&apikey=your_api_key
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
    "price_target": {
        "high": 220,
        "median": 185,
        "low": 136,
        "average": 184.01,
        "current": 169.5672,
        "currency": "USD"
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

### price_target

| 字段 | 类型 | 说明 |
|---|---|---|
| `high` | double | 分析师最高目标价 |
| `median` | double | 目标价中位数 |
| `low` | double | 分析师最低目标价 |
| `average` | double | 目标价平均值 |
| `current` | double | 当前市场价格 |
| `currency` | string | 目标价计价币种 |
| `status` | string | 请求状态（`ok`） |
