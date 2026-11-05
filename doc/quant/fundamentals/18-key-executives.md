# Key Executives — 公司高管接口

> 官方文档：https://twelvedata.com/docs#key-executives

## 概述

`/key_executives` 接口返回指定股票代码对应的公司关键高管详细信息，包括姓名、职位和角色等数据。适用于了解公司的领导层结构。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /key_executives` |
| API credits | `1000` / symbol（消耗较大，注意调用频率） |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/key_executives` |

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
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/key_executives?symbol=AAPL&apikey=your_api_key
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
    "key_executives": [
        {
            "name": "Mr. Timothy D. Cook",
            "title": "CEO & Director",
            "age": 59,
            "year_born": 1961,
            "pay": 14769259
        }
    ]
}
```

## 响应字段说明

### meta — 标的元信息

| 字段 | 说明 |
|---|---|
| `symbol` | 标的代码 |
| `name` | 公司名称 |
| `currency` | 标的计价货币代码 |
| `exchange` / `mic_code` | 交易所名称 / 市场标识码 |
| `exchange_timezone` | 交易所时区 |

### key_executives — 高管列表

| 字段 | 类型 | 说明 |
|---|---|---|
| `name` | string | 高管全名（含姓、名、中间名及后缀） |
| `title` | string | 职位名称 |
| `age` | integer | 当前年龄（如有） |
| `year_born` | integer | 出生年份（如有） |
| `pay` | integer | 总薪酬（如有） |
