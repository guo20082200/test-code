# Twelve Data — Analysis（分析）接口文档

> 来源：https://twelvedata.com/docs/analysis
>
> 前瞻性与共识分析数据：盈利与营收预估、EPS 趋势与修正、增长预测、分析师评级与建议、目标价等共识指标。适合将专家预测与市场情绪纳入模型和仪表盘。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数 |
| 请求方式 | `GET` |
| 响应格式 | `JSON` |
| 套餐要求 | 全部接口需 **Ultra（个人）/ Enterprise（商业）** 及以上 |
| 接口总数 | 9 个 |

## 接口目录

| # | 文档 | 接口 | Credits | 说明 |
|---|---|---|---|---|
| 01 | [Earnings estimate](01-earnings-estimate.md) | `/earnings_estimate` | 20 / symbol | 分析师 EPS 预估（季度/年度） |
| 02 | [Revenue estimate](02-revenue-estimate.md) | `/revenue_estimate` | 20 / symbol | 营收预估（含销售增长率） |
| 03 | [EPS trend](03-eps-trend.md) | `/eps_trend` | 20 / symbol | EPS 预估演变（7/30/60/90 天前对比） |
| 04 | [EPS revisions](04-eps-revisions.md) | `/eps_revisions` | 20 / symbol | EPS 上调/下调次数统计 |
| 05 | [Growth estimates](05-growth-estimates.md) | `/growth_estimates` | 20 / symbol | 增长率共识（多时间跨度） |
| 06 | [Recommendations](06-recommendations.md) | `/recommendations` | 100 / symbol | 分析师评级汇总 + 综合评分 |
| 07 | [Price target](07-price-target.md) | `/price_target` | 75 / symbol | 分析师目标价（最高/最低/中位/平均） |
| 08 | [Analyst ratings snapshot](08-analyst-ratings-light.md) | `/analyst_ratings/light` | 75 / symbol | 评级快照（全球市场，轻量） |
| 09 | [Analyst ratings US equities](09-analyst-ratings-us-equities.md) | `/analyst_ratings/us_equities` | 200 / symbol | 美股详细评级（含分析师与目标价调整） |

## 通用说明

### 标的标识参数

以下参数（"✱ One of these parameters is required"）**至少提供一个**：

| 参数 | 说明 |
|---|---|
| `symbol` | 标的代码，如 `AAPL` |
| `figi` | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 |
| `isin` | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 |
| `cusip` | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 |
| `country` | 国家名称或缩写，如 `United States` 或 `US` |
| `exchange` | 交易所名称，如 `NASDAQ` |
| `mic_code` | 市场标识码（ISO 10383），如 `XNAS`、`XLON` |

### 其他通用参数

| 参数 | 说明 |
|---|---|
| `dp` | 浮点值小数位数，范围 `[0, 11]`，默认 `5`（部分接口提供） |
| `outputsize` | 返回记录数，默认 `30`（评级类接口提供） |
| `rating_change` | 评级动作过滤（仅评级类接口）：`Maintains`、`Upgrade`、`Downgrade`、`Initiates`、`Reiterates` |

### 响应结构

所有接口响应均含 `meta`（标的元信息：`symbol`、`name`、`currency`、`exchange_timezone`、`exchange`、`mic_code`、`type`）+ 业务数据分组 + `status`（`ok`）。

### 成本分级

| 分级 | Credits | 接口 |
|---|---|---|
| 低成本预估类 | 20 | Earnings/Revenue estimate、EPS trend/revisions、Growth estimates |
| 中成本共识类 | 75 | Price target、Analyst ratings snapshot |
| 高成本建议类 | 100 | Recommendations |
| 最高成本明细类 | 200 | Analyst ratings US equities |
