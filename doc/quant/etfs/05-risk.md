# Risk — ETF 风险接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/world/risk` 接口返回全球交易所交易基金的核心风险指标，包括波动率、beta 及其他风险相关度量。适用于评估全球范围内投资各类 ETF 的潜在风险。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/world/risk` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/etfs/world/risk` |

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
GET https://api.twelvedata.com/etfs/world/risk?symbol=IVV&apikey=your_api_key
```

## 响应示例

```json
{
    "etf": {
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
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### etf.risk.volatility_measures — 波动率度量（按周期分组）

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

### etf.risk.valuation_metrics — 估值指标

| 字段 | 说明 |
|---|---|
| `price_to_earnings` | 市盈率（P/E） |
| `price_to_book` | 市净率（P/B） |
| `price_to_sales` | 市销率（P/S） |
| `price_to_cashflow` | 市现率（P/CF） |
| `status` | 请求状态（`ok`） |
