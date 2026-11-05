# Profile — 公司概况接口

> 官方文档：https://twelvedata.com/docs#profile

## 概述

`/profile` 接口返回指定公司的详细信息，包括公司名称、行业、板块、CEO 和总部地址等。适用于获取公司业务与经营状况的全面概览。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /profile` |
| API credits | `10` / symbol |
| 最低套餐 | Grow（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/profile` |

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
GET https://api.twelvedata.com/profile?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "symbol": "AAPL",
    "name": "Apple Inc",
    "exchange": "NASDAQ",
    "mic_code": "XNAS",
    "sector": "Technology",
    "industry": "Consumer Electronics",
    "employees": 147000,
    "website": "http://www.apple.com",
    "description": "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and...",
    "type": "Common Stock",
    "CEO": "Mr. Timothy D. Cook",
    "address": "One Apple Park Way",
    "address2": "Cupertino, CA 95014",
    "city": "Cupertino",
    "zip": "95014",
    "state": "CA",
    "country": "US",
    "phone": "408-996-1010"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | 标的代码 |
| `name` | string | 公司名称 |
| `exchange` | string | 交易所名称 |
| `mic_code` | string | 市场标识码 |
| `sector` | string | 所属板块 |
| `industry` | string | 所属行业 |
| `employees` | integer | 员工数量 |
| `website` | string | 公司官网 |
| `description` | string | 公司简介 |
| `type` | string | 证券类型（如 Common Stock） |
| `CEO` | string | 首席执行官姓名 |
| `address` / `address2` | string | 公司地址 |
| `city` / `zip` / `state` / `country` | string | 城市 / 邮编 / 州 / 国家 |
| `phone` | string | 联系电话 |
