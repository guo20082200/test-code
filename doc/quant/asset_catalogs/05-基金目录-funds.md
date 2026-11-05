# Twelve Data — Funds（基金目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录`、`02-外汇交易对`、`03-加密货币交易对`、`04-ETF目录`、`06-大宗商品目录`、`07-债券目录`

## 1. 接口概述

`funds` 端点返回**每日更新**的全部可用投资基金列表，每条记录包含标识符、名称等属性。

⚠️ **与其他目录接口不同**：funds 的响应数据包裹在 `result` 对象中（`result.count` + `result.list`），而不是顶层的 `count` + `data`，解析时需注意。

- **接口地址**：`https://api.twelvedata.com/funds`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/funds?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 按标的代码过滤 | `FXAIX` | — |
| `figi` | string | 按金融工具全球标识符（FIGI）过滤。仅 Ultra / Enterprise 及以上套餐可用 | `BBG000BHTMY7` | — |
| `isin` | string | 按国际证券识别码（ISIN）过滤；响应中返回 ISIN 需要 ISIN Data 附加包 | `US0378331005` | — |
| `cusip` | string | 按 CUSIP 编码过滤；响应中返回 CUSIP 需要 CUSIP Data 附加包 | `594918104` | — |
| `cik` | string | 按 SEC 的 CIK 编码过滤 | `95953` | — |
| `exchange` | string | 按交易所名称过滤 | `Nasdaq` | — |
| `mic_code` | string | 按 ISO 10383 市场识别码（MIC）过滤 | `ARCX` | — |
| `country` | string | 按国家名称或缩写过滤 | `United States` / `US` | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `show_plan` | boolean | 是否在响应中附加该标的可用的套餐信息（`access` 字段） | `true` | `false` |
| `page` | integer | 结果分页页码 | `2` | `1` |
| `outputsize` | integer | 返回的记录数量。**不传则返回全部可用记录** | `50` | — |

注意：funds **不支持** `include_delisted` 参数（stocks / etfs 支持）。

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `result` | object | 结果包装对象，见下表 |
| `status` | string | 响应状态 |

**`result` 对象：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 匹配的标的总数 |
| `list` | object 数组 | 基金列表，见下表 |

**`result.list` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 标的代码（Ticker） |
| `name` | string | 基金全称 |
| `country` | string | 基金所在国家 |
| `currency` | string | 计价币种（ISO 4217） |
| `exchange` | string | 交易场所 |
| `mic_code` | string | 市场识别码（MIC） |
| `type` | string | 基金类型 |
| `figi_code` | string | 金融工具全球标识符（FIGI） |
| `cfi_code` | string | 金融工具分类码（CFI） |
| `isin` | string | 国际证券识别码 |
| `cusip` | string | 美国证券统一识别码 |
| `access` | object | 套餐可用信息（仅 `show_plan=true` 时返回），含 `global` / `plan` / `plan_business` |

## 5. 响应示例

```json
{
    "result": {
        "count": 84799,
        "list": [
            {
                "symbol": "DIVI",
                "name": "AdvisorShares Athena High Dividend ETF",
                "country": "United States",
                "currency": "USD",
                "exchange": "NYSE",
                "mic_code": "ARCX",
                "type": "ETF",
                "figi_code": "BBG00161BCW4",
                "cfi_code": "CECILU",
                "isin": "GB00B65TLW28",
                "cusip": "35473P108",
                "access": {
                    "global": "Basic",
                    "plan": "Basic",
                    "plan_business": "Basic"
                }
            }
        ]
    },
    "status": "ok"
}
```
