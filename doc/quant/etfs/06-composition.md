# Composition — ETF 持仓构成接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/world/composition` 接口返回全球交易所交易基金的持仓构成详情，包括板块分布、具体持仓明细及各成分权重。适用于了解 ETF 组合的具体构成和板块分布。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/world/composition` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/etfs/world/composition` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | ETF 代码 | `IVV` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000BVZ697` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US4642872000` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `464287200` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/etfs/world/composition?symbol=IVV&apikey=your_api_key
```

## 响应示例

```json
{
    "etf": {
        "composition": {
            "major_market_sectors": [
                {
                    "sector": "Technology",
                    "weight": 0.2424
                }
            ],
            "country_allocation": [
                {
                    "country": "United Kingdom",
                    "allocation": 0.9855
                }
            ],
            "asset_allocation": {
                "cash": 0.0004,
                "stocks": 0.9996,
                "preferred_stocks": 0,
                "convertables": 0,
                "bonds": 0,
                "others": 0
            },
            "top_holdings": [
                {
                    "symbol": "AAPL",
                    "name": "Apple Inc",
                    "exchange": "NASDAQ",
                    "mic_code": "XNAS",
                    "weight": 0.0592
                }
            ],
            "bond_breakdown": {
                "average_maturity": {
                    "fund": 6.65,
                    "category": 7.81
                },
                "average_duration": {
                    "fund": 5.72,
                    "category": 5.64
                },
                "credit_quality": [
                    {
                        "grade": "AAA",
                        "weight": 0
                    }
                ]
            }
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### etf.composition

| 字段 | 类型 | 说明 |
|---|---|---|
| `major_market_sectors[].sector` | string | 板块名称（如 Technology） |
| `major_market_sectors[].weight` | double | 板块权重 |
| `country_allocation[].country` | string | 国家名称 |
| `country_allocation[].allocation` | double | 国家配置比例 |
| `asset_allocation.cash` | double | 现金占比 |
| `asset_allocation.stocks` | double | 股票占比 |
| `asset_allocation.preferred_stocks` | double | 优先股占比 |
| `asset_allocation.convertables` | double | 可转债占比 |
| `asset_allocation.bonds` | double | 债券占比 |
| `asset_allocation.others` | double | 其他资产占比 |
| `top_holdings[].symbol` / `name` | string | 前大持仓代码 / 名称 |
| `top_holdings[].exchange` / `mic_code` | string | 持仓所在交易所 / 市场标识码 |
| `top_holdings[].weight` | double | 持仓权重 |
| `bond_breakdown.average_maturity.fund` / `category` | double | 平均到期年限（基金 / 同类均值） |
| `bond_breakdown.average_duration.fund` / `category` | double | 平均久期（基金 / 同类均值） |
| `bond_breakdown.credit_quality[].grade` | string | 信用评级（如 AAA） |
| `bond_breakdown.credit_quality[].weight` | double | 该评级债券权重 |
| `status` | string | 请求状态（`ok`） |

> `bond_breakdown` 债券明细主要在债券型 ETF 中有实质数据；股票型 ETF 中多为 0 或空。
