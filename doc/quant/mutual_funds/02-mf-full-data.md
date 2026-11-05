# MF Full Data — 共同基金全量数据接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world` 接口提供全球共同基金的详细信息，返回综合数据集：基金概况、绩效指标、风险评估、评级、资产构成、购买详情及可持续性（ESG）因素。适用于对全球共同基金进行深度评估，涵盖投资绩效、风险水平和环境影响等多个方面。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world` |
| API credits | `1000` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world` |

> 等价关系：本接口 = `/mutual_funds/world/summary` + `/performance` + `/risk` + `/ratings` + `/composition` + `/purchase_info` + `/sustainability`（各 200 credits）的打包，总成本相同，仅减少请求次数。

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
GET https://api.twelvedata.com/mutual_funds/world?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "summary": {
            "symbol": "0P0001LCQ3",
            "name": "JNL Small Cap Index Fund (I)",
            "fund_family": "Jackson National",
            "fund_type": "Small Blend",
            "currency": "USD",
            "share_class_inception_date": "2021-04-26",
            "ytd_return": -0.02986,
            "expense_ratio_net": 0.001,
            "yield": 0,
            "nav": 10.09,
            "min_investment": 0,
            "turnover_rate": 0.32,
            "net_assets": 2400762112,
            "overview": "The fund invests, normally, at least 80% of its assets in the stocks...",
            "people": [
                {
                    "name": "John Doe",
                    "tenure_since": "2018-01-01"
                }
            ]
        },
        "performance": {
            "trailing_returns": [
                {
                    "period": "ytd",
                    "share_class_return": -0.02986,
                    "category_return": 0.2019,
                    "rank_in_category": 76
                }
            ],
            "annual_total_returns": [
                {
                    "year": 2024,
                    "share_class_return": 0.08546,
                    "category_return": 0.1119
                }
            ],
            "quarterly_total_returns": [
                {
                    "year": 2024,
                    "q1": 0.02358,
                    "q2": -0.03071,
                    "q3": 0.10099,
                    "q4": -0.00629
                }
            ],
            "load_adjusted_return": [
                {
                    "period": "1_year",
                    "return": 0.06139
                }
            ]
        },
        "risk": {
            "volatility_measures": [
                {
                    "period": "3_year",
                    "alpha": -9.12,
                    "alpha_category": -0.0939,
                    "beta": 1,
                    "beta_category": 0.0126,
                    "mean_annual_return": 0.45,
                    "mean_annual_return_category": 0.0117,
                    "r_squared": 69,
                    "r_squared_category": 0.8309,
                    "std": 23.15,
                    "std_category": 0.2554,
                    "sharpe_ratio": 0.04,
                    "sharpe_ratio_category": 0.005,
                    "treynor_ratio": -1.41,
                    "treynor_ratio_category": 0.0806
                }
            ],
            "valuation_metrics": {
                "price_to_earnings": 0.05695,
                "price_to_earnings_category": 20.63,
                "price_to_book": 0.55626,
                "price_to_book_category": 2.87,
                "price_to_sales": 0.97803,
                "price_to_sales_category": 1.34,
                "price_to_cashflow": 0.10564,
                "price_to_cashflow_category": 11.81,
                "median_market_capitalization": 2965,
                "median_market_capitalization_category": 4925,
                "3_year_earnings_growth": 16.32,
                "3_year_earnings_growths_category": 10.55
            }
        },
        "ratings": {
            "performance_rating": 2,
            "risk_rating": 4,
            "return_rating": 0
        },
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
                "credit_quality": [
                    {
                        "grade": "U.S. Government",
                        "weight": 0
                    }
                ],
                "average_duration": {
                    "fund": 0,
                    "category": 1.64
                }
            }
        },
        "purchase_info": {
            "expenses": {
                "expense_ratio_gross": 0.0022,
                "expense_ratio_net": 0.001
            },
            "minimums": {
                "initial_investment": 0,
                "additional_investment": 0,
                "initial_ira_investment": 0,
                "additional_ira_investment": 0
            },
            "pricing": {
                "nav": 10.09,
                "12_month_low": 9.630000114441,
                "12_month_high": 12.10000038147,
                "last_month": 11.050000190735
            },
            "brokerages": []
        },
        "sustainability": {
            "score": 22,
            "corporate_esg_pillars": {
                "environmental": 3.73,
                "social": 10.44,
                "governance": 7.86
            },
            "sustainable_investment": false,
            "corporate_aum": 0.99486
        }
    },
    "status": "ok"
}
```

## 响应结构说明

响应包含 7 个子分组，各分组的详细字段说明见对应单维度接口文档：

| 分组 | 说明 | 详细字段文档 |
|---|---|---|
| `mutual_fund.summary` | 基金概况快照（含基金经理） | [03-summary.md](03-summary.md) |
| `mutual_fund.performance` | 绩效指标（追踪/年度/季度/费后收益） | [04-performance.md](04-performance.md) |
| `mutual_fund.risk` | 风险指标（波动率度量 + 估值指标） | [05-risk.md](05-risk.md) |
| `mutual_fund.ratings` | 评级（0–5 星） | [06-ratings.md](06-ratings.md) |
| `mutual_fund.composition` | 持仓构成 | [07-composition.md](07-composition.md) |
| `mutual_fund.purchase_info` | 购买信息（费率/门槛/券商） | [08-purchase-info.md](08-purchase-info.md) |
| `mutual_fund.sustainability` | ESG 可持续性评分 | [09-sustainability.md](09-sustainability.md) |
| `status` | 请求状态（`ok`） | — |
