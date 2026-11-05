# ETF Full Data — ETF 全量数据接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/world` 接口提供全球交易所交易基金的详细信息，返回综合数据，包括概况（Summary）、绩效指标（Performance）、风险评估（Risk）和持仓构成（Composition）。适用于对全球 ETF 进行深度分析，访问关键财务指标和组合拆分。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/world` |
| API credits | `800` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/etfs/world` |

> 等价关系：本接口 = `/etfs/world/summary` + `/etfs/world/performance` + `/etfs/world/risk` + `/etfs/world/composition`（各 200 credits）的打包，总成本相同，仅减少请求次数。

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
GET https://api.twelvedata.com/etfs/world?symbol=IVV&apikey=your_api_key
```

## 响应示例

```json
{
    "etf": {
        "summary": {
            "symbol": "IVV",
            "name": "iShares Core S&P 500 ETF",
            "fund_family": "iShares",
            "fund_type": "Large Blend",
            "currency": "USD",
            "share_class_inception_date": "2000-11-13",
            "ytd_return": -0.0537,
            "expense_ratio_net": -0.004,
            "yield": 0.0133,
            "nav": 413.24,
            "last_price": 413.24,
            "turnover_rate": 0.04,
            "net_assets": 753409982464,
            "overview": "The investment seeks to track the performance of the Standard & Poor's 500..."
        },
        "performance": {
            "trailing_returns": [
                {
                    "period": "ytd",
                    "share_class_return": -0.0751,
                    "category_return": 0.1484
                }
            ],
            "annual_total_returns": [
                {
                    "year": 2021,
                    "share_class_return": 0.2866,
                    "category_return": 0
                }
            ]
        },
        "risk": {
            "volatility_measures": [
                {
                    "period": "3_year",
                    "alpha": -0.03,
                    "alpha_category": -0.02,
                    "beta": 1,
                    "beta_category": 0.01,
                    "mean_annual_return": 1.58,
                    "mean_annual_return_category": 0.01,
                    "r_squared": 100,
                    "r_squared_category": 0.95,
                    "std": 18.52,
                    "std_category": 0.19,
                    "sharpe_ratio": 0.95,
                    "sharpe_ratio_category": 0.01,
                    "treynor_ratio": 17.41,
                    "treynor_ratio_category": 0.16
                }
            ],
            "valuation_metrics": {
                "price_to_earnings": 26.46,
                "price_to_book": 4.42,
                "price_to_sales": 2.96,
                "price_to_cashflow": 17.57
            }
        },
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

## 响应结构说明

响应包含 4 个子分组，各分组的详细字段说明见对应单维度接口文档：

| 分组 | 说明 | 详细字段文档 |
|---|---|---|
| `etf.summary` | 基金概况快照 | [03-summary.md](03-summary.md) |
| `etf.performance` | 绩效指标（追踪收益 + 年度总收益） | [04-performance.md](04-performance.md) |
| `etf.risk` | 风险指标（波动率度量 + 估值指标） | [05-risk.md](05-risk.md) |
| `etf.composition` | 持仓构成（板块/国家/资产配置 + 前大持仓 + 债券明细） | [06-composition.md](06-composition.md) |
| `status` | 请求状态（`ok`） | — |
