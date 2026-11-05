# Tax Information — 税务信息接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/tax_info` 接口提供指定金融工具的税务相关数据，包括适用的税率与相关税务代码。适用于了解交易或投资特定工具时涉及的税务影响。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /tax_info` |
| API credits | `50` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/tax_info` |

## 请求参数

✱ 以下标识参数**至少提供一个**（**注意：本接口不支持 `country` 参数**）：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，如 `SKYQ`、`AIRE`、`ALM:BME`、`HSI:HKEX` | `SKYQ` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG019XJT9D6` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US5949181045` |
| `exchange` | string | 可选 | 标的交易所名称，如 `Nasdaq`、`Euronext` | `Nasdaq` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383），如 `XNAS`、`XLON` | `XNAS` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/tax_info?symbol=SKYQ&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "SKYQ",
        "name": "Sky Quarry Inc.",
        "exchange": "NASDAQ",
        "mic_code": "XNCM",
        "country": "United States"
    },
    "data": {
        "tax_indicator": "us_1446f"
    },
    "status": "ok"
}
```

## 响应字段说明

### meta（标的元信息）

| 字段 | 说明 |
|---|---|
| `symbol` / `name` | 标的代码 / 公司名称 |
| `exchange` / `mic_code` | 交易所 / 市场标识码 |
| `country` | 所属国家 |

### data

| 字段 | 类型 | 说明 |
|---|---|---|
| `tax_indicator` | string | 税务标识代码（如 `us_1446f`，对应美国 IRC 1446(f) 预扣税规则） |
| `status` | string | 请求状态（`ok`） |
