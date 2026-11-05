# Statistics — 公司统计数据接口

> 官方文档：https://twelvedata.com/docs#statistics

## 概述

`/statistics` 接口返回公司关键财务统计数据的综合快照，包括估值指标、营收数据、利润率及其他重要财务数据。适用于深入了解公司的财务健康状况和绩效指标。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /statistics` |
| API credits | `50` / symbol |
| 最低套餐 | Pro（个人）/ Venture（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/statistics` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |
| `country` | string | 可选 | 交易所在国家，如 `United States` 或 `US` | `United States` |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/statistics?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "name": "Apple Inc",
        "currency": "USD",
        "exchange": "NASDAQ",
        "mic_code": "XNAS",
        "exchange_timezone": "America/New_York"
    },
    "statistics": {
        "valuations_metrics": {
            "market_capitalization": 2546807865344,
            "enterprise_value": 2620597731328,
            "trailing_pe": 30.162493,
            "forward_pe": 26.982489,
            "peg_ratio": 1.4,
            "price_to_sales_ttm": 7.336227,
            "price_to_book_mrq": 39.68831,
            "enterprise_to_revenue": 7.549,
            "enterprise_to_ebitda": 23.623
        },
        "financials": {
            "fiscal_year_ends": "2020-09-26",
            "most_recent_quarter": "2021-06-26",
            "gross_margin": 46.57807,
            "profit_margin": 0.25004,
            "operating_margin": 0.28788,
            "return_on_assets_ttm": 0.19302,
            "return_on_equity_ttm": 1.27125,
            "income_statement": {
                "revenue_ttm": 347155005440,
                "revenue_per_share_ttm": 20.61,
                "quarterly_revenue_growth": 0.364,
                "gross_profit_ttm": 104956000000,
                "ebitda": 110934999040,
                "net_income_to_common_ttm": 86801997824,
                "diluted_eps_ttm": 5.108,
                "quarterly_earnings_growth_yoy": 0.932
            },
            "balance_sheet": {
                "total_cash_mrq": 61696000000,
                "total_cash_per_share_mrq": 3.732,
                "total_debt_mrq": 135491002368,
                "total_debt_to_equity_mrq": 210.782,
                "current_ratio_mrq": 1.062,
                "book_value_per_share_mrq": 3.882
            },
            "cash_flow": {
                "operating_cash_flow_ttm": 104414003200,
                "levered_free_cash_flow_ttm": 80625876992
            }
        },
        "stock_statistics": {
            "shares_outstanding": 16530199552,
            "float_shares": 16513305231,
            "avg_10_volume": 72804757,
            "avg_90_volume": 77013078,
            "shares_short": 93105968,
            "short_ratio": 1.19,
            "short_percent_of_shares_outstanding": 0.0056,
            "percent_held_by_insiders": 0.00071000005,
            "percent_held_by_institutions": 0.58474
        },
        "stock_price_summary": {
            "fifty_two_week_low": 103.1,
            "fifty_two_week_high": 157.26,
            "fifty_two_week_change": 0.375625,
            "beta": 1.201965,
            "day_50_ma": 148.96686,
            "day_200_ma": 134.42506
        },
        "dividends_and_splits": {
            "forward_annual_dividend_rate": 0.88,
            "forward_annual_dividend_yield": 0.0057,
            "trailing_annual_dividend_rate": 0.835,
            "trailing_annual_dividend_yield": 0.0053832764,
            "5_year_average_dividend_yield": 1.27,
            "payout_ratio": 0.16309999,
            "dividend_frequency": "Quarterly",
            "dividend_date": "2021-08-12",
            "ex_dividend_date": "2021-08-06",
            "last_split_factor": "4-for-1 split",
            "last_split_date": "2020-08-31"
        }
    }
}
```

## 响应字段说明

### statistics.valuations_metrics — 估值指标

| 字段 | 说明 |
|---|---|
| `market_capitalization` | 市值（流通股的市场价值） |
| `enterprise_value` | 企业价值（EV），市值的更全面替代指标 |
| `trailing_pe` | 追踪市盈率（P/E，过去 12 个月） |
| `forward_pe` | 预期市盈率 |
| `peg_ratio` | PEG 比率 |
| `price_to_sales_ttm` | 市销率（TTM） |
| `price_to_book_mrq` | 市净率（最近一季 MRQ） |
| `enterprise_to_revenue` | 企业价值/营收 |
| `enterprise_to_ebitda` | 企业价值/EBITDA |

### statistics.financials — 财务数据

| 字段 | 说明 |
|---|---|
| `fiscal_year_ends` | 财年结束日期 |
| `most_recent_quarter` | 最近季度日期 |
| `gross_margin` | 毛利率 |
| `profit_margin` | 利润率 |
| `operating_margin` | 营业利润率 |
| `return_on_assets_ttm` | 总资产收益率（TTM） |
| `return_on_equity_ttm` | 净资产收益率（TTM） |
| `income_statement.*` | 利润表指标：营收 TTM、每股营收、季度营收增长、毛利 TTM、EBITDA、归属普通股东净利润 TTM、摊薄 EPS TTM、季度盈利同比增长 |
| `balance_sheet.*` | 资产负债表指标：总现金 MRQ、每股现金 MRQ、总债务 MRQ、债务/权益比 MRQ、流动比率 MRQ、每股账面价值 MRQ |
| `cash_flow.*` | 现金流指标：经营现金流 TTM、杠杆自由现金流 TTM |

### statistics.stock_statistics — 股本统计

| 字段 | 说明 |
|---|---|
| `shares_outstanding` | 流通股数量 |
| `float_shares` | 自由流通股数量 |
| `avg_10_volume` / `avg_90_volume` | 10 日 / 90 日平均成交量 |
| `shares_short` | 空头持仓股数 |
| `short_ratio` | 空头比率 |
| `short_percent_of_shares_outstanding` | 空头占流通股比例 |
| `percent_held_by_insiders` | 内部人持股比例 |
| `percent_held_by_institutions` | 机构持股比例 |

### statistics.stock_price_summary — 股价摘要

| 字段 | 说明 |
|---|---|
| `fifty_two_week_low` / `fifty_two_week_high` | 52 周最低价 / 最高价 |
| `fifty_two_week_change` | 52 周涨跌幅 |
| `beta` | Beta 系数 |
| `day_50_ma` / `day_200_ma` | 50 日 / 200 日移动平均线 |

### statistics.dividends_and_splits — 股息与拆股

| 字段 | 说明 |
|---|---|
| `forward_annual_dividend_rate` / `yield` | 预期年股息率 / 股息收益率 |
| `trailing_annual_dividend_rate` / `yield` | 追踪年股息率 / 股息收益率 |
| `5_year_average_dividend_yield` | 5 年平均股息收益率 |
| `payout_ratio` | 派息比率 |
| `dividend_frequency` | 派息频率（如 Quarterly） |
| `dividend_date` / `ex_dividend_date` | 派息日 / 除息日 |
| `last_split_factor` / `last_split_date` | 最近拆股比例 / 拆股日期 |
