# Twelve Data — Fixed Income（债券目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录`、`02-外汇交易对`、`03-加密货币交易对`、`04-ETF目录`、`05-基金目录`、`06-大宗商品目录`

## 1. 接口概述

`bonds`（Fixed Income，固定收益）端点返回**每日更新**的全部可用债券列表，每条记录包含标识符、名称等属性。

⚠️ **与其他目录接口不同**：bonds 的响应数据包裹在 `result` 对象中（`result.count` + `result.list`），而不是顶层的 `count` + `data`（与 funds 相同，与 stocks / etfs 等不同），解析时需注意。

- **接口地址**：`https://api.twelvedata.com/bonds`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/bonds?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `symbol` | string | 按债券代码过滤 | `US2Y`（美国 2 年期国债收益率） | — |
| `exchange` | string | 按交易所名称过滤 | `NYSE` | — |
| `mic_code` | string | 按 ISO 10383 市场识别码（MIC）过滤 | `XNYS` | — |
| `country` | string | 按国家名称或缩写过滤 | `United States` / `US` | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `show_plan` | boolean | 是否在响应中附加该标的可用的套餐信息（`access` 字段） | `true` | `false` |
| `page` | integer | 结果分页页码 | `2` | `1` |
| `outputsize` | integer | 返回的记录数量。**不传则返回全部可用记录** | `50` | — |

注意：bonds **不支持** `figi` / `isin` / `cusip` / `cik` 过滤参数（stocks / etfs / funds 支持），响应数据中也无这些标识符字段。

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `result` | object | 结果包装对象，见下表 |
| `status` | string | 响应状态 |

**`result` 对象：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | integer | 匹配的标的总数 |
| `list` | object 数组 | 债券列表，见下表 |

**`result.list` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 债券代码 |
| `name` | string | 债券全称 |
| `country` | string | 债券所在国家 |
| `currency` | string | 计价币种（ISO 4217） |
| `exchange` | string | 交易场所 |
| `mic_code` | string | 市场识别码（MIC） |
| `type` | string | 债券类型 |
| `access` | object | 套餐可用信息（仅 `show_plan=true` 时返回），含 `global` / `plan` / `plan_business` |

## 5. 响应示例

```json
{
    "result": {
        "count": 6,
        "list": [
            {
                "symbol": "US2Y",
                "name": "US Treasury Yield 2 Years",
                "country": "United States",
                "currency": "USD",
                "exchange": "NYSE",
                "mic_code": "XNYS",
                "type": "Bond",
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

## 6. 七个资产目录接口结构对比

| 端点 | 响应结构 | 特有过滤参数 | 数据中含标识符（figi/isin/cusip） |
| --- | --- | --- | --- |
| `/stocks` | `count` + `data` | `cik`、`include_delisted` | 是（含 isin/cusip 备注） |
| `/etfs` | `count` + `data` | `cik`、`include_delisted` | 是 |
| `/funds` | **`result.count` + `result.list`** | `cik` | 是 |
| `/bonds` | **`result.count` + `result.list`** | 无 | 否（仅基础信息 + access） |
| `/forex_pairs` | `count` + `data` | `currency_base`、`currency_quote` | 否 |
| `/cryptocurrencies` | `count` + `data` | `exchange`、`currency_base`、`currency_quote` | 否（含 available_exchanges） |
| `/commodities` | `count` + `data` | `category` | 否（含 description） |

共同点：所有接口均支持 `page` 分页与 `outputsize`（不传返回全部记录）；消耗均为每次请求 `1` 点积分；除 commodities 外均支持 `show_plan`。
