# Twelve Data — Stocks（股票目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`02-外汇交易对`、`03-加密货币交易对`、`04-ETF目录`、`05-基金目录`、`06-大宗商品目录`、`07-债券目录`

## 1. 接口概述

`stocks` 端点返回**每日更新**的全部可用股票代码列表，数组中每条记录包含代码、名称、币种、交易所等信息，可用于后续行情接口（time_series / quote / price 等）的标的查询。

- **接口地址**：`https://api.twelvedata.com/stocks`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/stocks?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 / 可选值 | 默认值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 按标的代码过滤 | `AAPL` | — |
| `figi` | string | 按金融工具全球标识符（FIGI）过滤。仅 Ultra / Enterprise 及以上套餐可用 | `BBG000B9Y5X2` | — |
| `isin` | string | 按国际证券识别码（ISIN）过滤；响应中返回 ISIN 需要 ISIN Data 附加包 | `US0378331005` | — |
| `cusip` | string | 按 CUSIP 编码过滤；响应中返回 CUSIP 需要 CUSIP Data 附加包 | `594918104` | — |
| `cik` | string | 按 SEC 的 CIK 编码过滤 | `95953` | — |
| `exchange` | string | 按交易所名称过滤 | `NASDAQ` | — |
| `mic_code` | string | 按 ISO 10383 市场识别码（MIC）过滤 | `XNGS` | — |
| `country` | string | 按国家名称或缩写过滤 | `United States` / `US` | — |
| `type` | string | 按资产类别过滤 | 20 种取值（`Common Stock`、`ETF`、`REIT` 等） | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `show_plan` | boolean | 是否在响应中附加该标的可用的套餐信息（`access` 字段） | `true` | `false` |
| `include_delisted` | boolean | 是否包含已退市的标的 | `true` | `false` |
| `page` | integer | 结果分页页码 | `2` | `1` |
| `outputsize` | integer | 返回的记录数量。**不传则返回全部可用记录** | `50` | — |

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 返回的记录数量 |
| `data` | object 数组 | 股票标的列表，见下表 |
| `status` | string | 响应状态 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 标的代码（Ticker） |
| `name` | string | 标的全称 |
| `currency` | string | 计价币种（ISO 4217） |
| `exchange` | string | 上市交易所 |
| `mic_code` | string | 市场识别码（MIC） |
| `country` | string | 交易所所在国家 |
| `type` | string | 证券类型 |
| `figi_code` | string | 金融工具全球标识符（FIGI） |
| `cfi_code` | string | 金融工具分类码（CFI） |
| `isin` | string | 国际证券识别码（需向客服单独申请开通） |
| `cusip` | string | 美国证券统一识别码（9 位字母数字） |
| `access` | object | 套餐可用信息（仅 `show_plan=true` 时返回），含 `global`（访问级别）/ `plan`（个人套餐名）/ `plan_business`（企业套餐名） |

## 5. 响应示例

```json
{
    "count": 100,
    "data": [
        {
            "symbol": "AAPL",
            "name": "Apple Inc",
            "currency": "USD",
            "exchange": "NASDAQ",
            "mic_code": "XNGS",
            "country": "United States",
            "type": "Common Stock",
            "figi_code": "BBG000B9Y5X2",
            "cfi_code": "ESVUFR",
            "isin": "US0378331005",
            "cusip": "037833100",
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
