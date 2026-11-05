# Twelve Data — Mutual Funds（共同基金）接口文档

> 来源：https://twelvedata.com/docs/mutual-funds
>
> 提供共同基金专属的目录与快照能力：基金目录、管理公司家族、基金类型、净值历史、分红记录、关键比率及组合持仓。适用于长期绩效分析与投资组合归因。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数 |
| 请求方式 | `GET` |
| 响应格式 | `JSON`（部分接口支持 `CSV`） |
| 计费单位 | API credits（按 request 计） |

## 接口目录

### 目录与分组类

| # | 文档 | 接口 | Credits | 说明 |
|---|---|---|---|---|
| 01 | [MFs directory](01-mfs-directory.md) | `/mutual_funds/list` | 1 / request | 共同基金目录（按总资产降序，每日更新） |
| 10 | [MFs families](10-mfs-families.md) | `/mutual_funds/family` | 1 / request | 基金家族列表（按国家分组） |
| 11 | [MFs types](11-mfs-types.md) | `/mutual_funds/type` | 1 / request | 基金类型列表（按市场分组） |

### 数据类（均需 Ultra / Enterprise 套餐）

| # | 文档 | 接口 | Credits | 说明 |
|---|---|---|---|---|
| 02 | [MF full data](02-mf-full-data.md) | `/mutual_funds/world` | 1000 / request | 全量数据打包（7 个维度） |
| 03 | [Summary](03-summary.md) | `/mutual_funds/world/summary` | 200 / request | 基金概况快照（含基金经理） |
| 04 | [Performance](04-performance.md) | `/mutual_funds/world/performance` | 200 / request | 绩效指标（含季度收益） |
| 05 | [Risk](05-risk.md) | `/mutual_funds/world/risk` | 200 / request | 风险指标 |
| 06 | [Ratings](06-ratings.md) | `/mutual_funds/world/ratings` | 200 / request | 评级（0–5 星） |
| 07 | [Composition](07-composition.md) | `/mutual_funds/world/composition` | 200 / request | 持仓构成 |
| 08 | [Purchase info](08-purchase-info.md) | `/mutual_funds/world/purchase_info` | 200 / request | 购买信息（费率/门槛/券商） |
| 09 | [Sustainability](09-sustainability.md) | `/mutual_funds/world/sustainability` | 200 / request | ESG 可持续性评分 |

## 通用说明

### 标的标识参数

数据类接口（02–09）要求以下参数**至少提供一个**（"One of these parameters is required"）：

| 参数 | 说明 |
|---|---|
| `symbol` | 基金代码，如 `0P0001LCQ3` |
| `figi` | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 |
| `isin` | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 |
| `cusip` | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 |
| `country` | 国家名称或缩写，如 `United States` 或 `US` |

### 输出参数

| 参数 | 说明 |
|---|---|
| `dp` | 浮点值小数位数，范围 `[0, 11]`，默认 `5` |
| `format` / `delimiter` | 响应格式 `JSON`/`CSV` 及 CSV 分隔符（目录类接口支持） |
| `page` / `outputsize` | 分页参数（目录类接口支持，`outputsize` 默认 `100`） |

### 成本提示

`/mutual_funds/world`（1000 credits）= Summary + Performance + Risk + Ratings + Composition + Purchase Info + Sustainability（5 × 200）的打包，总成本相同，仅减少请求次数。若只需某一维度数据，直接调用对应的 200 credits 接口更划算。

### 套餐限制

- `/mutual_funds/list`：Basic/Grow/Pro（个人）与 Venture（商业）套餐最多返回 **50 条**记录；Ultra/Enterprise/Custom 可获取 **140,000+ 共同基金全量数据**
- `/mutual_funds/world*` 系列接口：需 **Ultra（个人）/ Enterprise（商业）** 及以上套餐

### 与 ETFs 接口的差异

| 差异点 | Mutual Funds | ETFs |
|---|---|---|
| 数据维度 | 7 个（多 Ratings、Purchase Info、Sustainability） | 4 个 |
| 全量打包成本 | 1000 credits | 800 credits |
| 目录覆盖量 | 140,000+ | 40,000+ |
| 目录独有过滤参数 | `performance_rating`、`risk_rating` | 无 |
| Summary 独有字段 | `people`（基金经理及任职时间）、`min_investment` | — |
| Performance 独有 | 季度收益、销售费调整后收益、同类排名 | — |
