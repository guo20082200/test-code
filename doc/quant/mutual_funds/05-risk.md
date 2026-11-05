# Risk — 共同基金风险接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/risk` 接口返回全球共同基金的详细风险指标，包括标准差、beta、夏普比率等，用于评估跨市场共同基金的波动性与风险画像。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/risk` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/risk` |

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
GET https://api.twelvedata.com/mutual_funds/world/risk?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
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
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### mutual_fund.risk.volatility_measures — 波动率度量（按周期分组）

每个周期（如 `3_year`、`5_year`、`10_year`）包含以下指标，后缀 `_category` 为同类别基金平均值：

| 字段 | 说明 |
|---|---|
| `period` | 计算周期 |
| `alpha` / `alpha_category` | Alpha（超额收益） |
| `beta` / `beta_category` | Beta（相对市场敏感度） |
| `mean_annual_return` / `_category` | 年均收益率 |
| `r_squared` / `_category` | R²（与基准指数的拟合度，0–100） |
| `std` / `_category` | 标准差（波动率） |
| `sharpe_ratio` / `_category` | 夏普比率（风险调整后收益） |
| `treynor_ratio` / `_category` | 特雷诺比率（系统性风险调整后收益） |

### mutual_fund.risk.valuation_metrics — 估值指标

每个指标均提供基金值与 `_category` 同类均值：

| 字段 | 说明 |
|---|---|
| `price_to_earnings` | 市盈率（P/E） |
| `price_to_book` | 市净率（P/B） |
| `price_to_sales` | 市销率（P/S） |
| `price_to_cashflow` | 市现率（P/CF） |
| `median_market_capitalization` | 持仓中位市值 |
| `3_year_earnings_growth` | 3 年盈利增长率 |
| `status` | 请求状态（`ok`） |

> 与 ETF Risk 的差异：估值指标额外提供 `_category` 同类对比，并多出 `median_market_capitalization`、`3_year_earnings_growth`。
