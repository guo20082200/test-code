# Logo — 资产 Logo 图片接口

> 官方文档：https://twelvedata.com/docs#logo

## 概述

`/logo` 接口返回指定公司、加密货币或外汇对的官方 Logo 图片地址。适用于在金融应用、网站或报告中集成品牌视觉元素，帮助用户识别对应的金融资产。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /logo` |
| API credits | `1` / symbol |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/logo` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | ✱ 是 | 标的代码，如 `AAPL`、`BTC/USD`、`EUR/USD` | `BTC/USD` |
| `exchange` | string | 否 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 否 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 否 | 交易所在国家，如 `United States` 或 `US` | `United States` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/logo?symbol=BTC/USD&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "BTC/USD",
        "exchange": "Coinbase Pro"
    },
    "url": "https://api.twelvedata.com/logo/apple.com",
    "logo_base": "https://logo.twelvedata.com/crypto/btc.png",
    "logo_quote": "https://logo.twelvedata.com/crypto/usd.png"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `meta.symbol` | string | 标的代码 |
| `meta.exchange` | string | 交易所名称 |
| `url` | string | 标的 Logo 图片地址 |
| `logo_base` | string | 基础货币（base）Logo 地址（加密货币/外汇对时返回） |
| `logo_quote` | string | 报价货币（quote）Logo 地址（加密货币/外汇对时返回） |
