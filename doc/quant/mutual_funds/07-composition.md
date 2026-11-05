# Composition — 共同基金持仓构成接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/composition` 接口返回指定共同基金的组合持仓构成详情，包括板块配置、个别持仓及其权重敞口。适用于了解共同基金的投资分布与风险画像。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/composition` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/composition` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 基金代码 | `0P0001LCQ3` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG00HMMLCH1` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `LU1206782309` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `120678230` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/mutual_funds/world/composition?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "composition": {
            "major_market_sectors": [
                {
                    "sector": "Industrials",
                    "weight": 0.1742
                }
            ],
            "asset_allocation": {
                "cash": 0.0043,
                "stocks": 0.9956,
                "preferred_stocks": 0,
                "convertables": 0,
                "bonds": 0,
                "others": 0
            },
            "top_holdings": [
                {
                    "symbol": "BBWI",
                    "name": "Bath & Body Works Inc",
                    "exchange": "NASDAQ",
                    "mic_code": "XNAS",
                    "weight": 0.00624
                }
            ],
            "bond_breakdown": {
                "average_maturity": {
                    "fund": 0,
                    "category": 1.97
                },
                "average_duration": {
                    "fund": 0,
                    "category": 1.64
                },
                "credit_quality": [
                    {
                        "grade": "U.S. Government",
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

### mutual_fund.composition

| 字段 | 类型 | 说明 |
|---|---|---|
| `major_market_sectors[].sector` | string | 板块名称（如 Industrials） |
| `major_market_sectors[].weight` | double | 板块权重 |
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
| `bond_breakdown.credit_quality[].grade` | string | 信用评级（如 AAA、U.S. Government） |
| `bond_breakdown.credit_quality[].weight` | double | 该评级债券权重 |
| `status` | string | 请求状态（`ok`） |

> 与 ETF Composition 的差异：本接口无 `country_allocation`（国家配置）分组。
