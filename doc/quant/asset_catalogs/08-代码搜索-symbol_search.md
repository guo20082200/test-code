# Twelve Data — Symbol Search（代码搜索）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `07-债券目录`、`09-交叉上市`、`10-最早时间戳`

## 1. 接口概述

`symbol_search` 端点（高需求接口）用于**按名称或代码模糊搜索金融工具**，返回按相关度排序的匹配列表（最相关的排在最前）。适合在只有部分信息时快速定位股票、ETF 等标的。

- **接口地址**：`https://api.twelvedata.com/symbol_search`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/symbol_search?symbol=AAPL&apikey=demo"
```

## 3. 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- | --- |
| `symbol` | string | 是 | 搜索关键字，支持：Ticker 代码 / ISIN（所有套餐可用，返回 ISIN 需 ISIN Data 附加包）/ FIGI、Composite FIGI、Share Class FIGI（均需 Ultra 个人版或 Enterprise 企业版及以上） | `AAPL` | — |
| `outputsize` | integer | 否 | 返回的匹配数量，最大 `120` | `50` | `30` |
| `show_plan` | boolean | 否 | 是否在响应中附加该标的可用的套餐信息（`access` 字段） | `true` | `false` |

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | object 数组 | 匹配搜索条件的标的列表，见下表 |
| `status` | string | 响应状态 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 标的代码（Ticker） |
| `instrument_name` | string | 标的名称（文档原文为 "Name of exchange"，实际返回标的全称，如 `Alcoa Corp`） |
| `exchange` | string | 交易场所 |
| `mic_code` | string | 市场识别码（MIC，ISO 10383 标准） |
| `exchange_timezone` | string | 交易所所在时区 |
| `instrument_type` | string | 工具类型，如 `Common Stock` |
| `country` | string | 交易所所属国家 |
| `currency` | string | 交易计价币种 |
| `access` | object | 套餐可用信息（仅 `show_plan=true` 时返回），含 `global`（访问级别）/ `plan`（个人套餐名）/ `plan_business`（企业套餐名） |

## 5. 响应示例

```json
{
    "data": [
        {
            "symbol": "AA",
            "instrument_name": "Alcoa Corp",
            "exchange": "NYSE",
            "mic_code": "XNYS",
            "exchange_timezone": "America/New_York",
            "instrument_type": "Common Stock",
            "country": "United States",
            "currency": "USD",
            "access": {
                "global": "Basic",
                "plan": "Basic",
                "plan_business": "Basic"
            }
        }
    ],
    "status": "ok"
}
```
