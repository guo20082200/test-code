# Twelve Data — ETFs（交易所交易基金）接口文档

> 来源：https://twelvedata.com/docs/etfs
>
> 提供 ETF 专属的元数据与分析能力：标的目录、基金家族与类型分组、NAV 快照、绩效指标、风险度量以及当前持仓构成。针对交易所交易基金的独特特征和披露频率定制。

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
| 01 | [ETFs directory](01-etfs-directory.md) | `/etfs/list` | 1 / request | ETF 目录列表（按总资产降序，每日更新） |
| 07 | [ETFs families](07-etfs-families.md) | `/etfs/family` | 1 / request | ETF 基金家族列表（按国家分组） |
| 08 | [ETFs types](08-etfs-types.md) | `/etfs/type` | 1 / request | ETF 类型列表（按市场分组） |

### 数据类（均需 Ultra / Enterprise 套餐）

| # | 文档 | 接口 | Credits | 说明 |
|---|---|---|---|---|
| 02 | [ETF full data](02-etf-full-data.md) | `/etfs/world` | 800 / request | 全量数据打包（概况+绩效+风险+构成） |
| 03 | [Summary](03-summary.md) | `/etfs/world/summary` | 200 / request | 基金概况快照 |
| 04 | [Performance](04-performance.md) | `/etfs/world/performance` | 200 / request | 绩效指标 |
| 05 | [Risk](05-risk.md) | `/etfs/world/risk` | 200 / request | 风险指标 |
| 06 | [Composition](06-composition.md) | `/etfs/world/composition` | 200 / request | 持仓构成 |

## 通用说明

### 标的标识参数

数据类接口（02–06）要求以下参数**至少提供一个**（"One of these parameters is required"）：

| 参数 | 说明 |
|---|---|
| `symbol` | ETF 代码，如 `IVV` |
| `figi` | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 |
| `isin` | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 |
| `cusip` | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 |
| `country` | 国家名称或缩写，如 `United States` 或 `US` |

### 输出参数

| 参数 | 说明 |
|---|---|
| `dp` | 浮点值小数位数，范围 `[0, 11]`，默认 `5` |
| `format` / `delimiter` | 响应格式 `JSON`/`CSV` 及 CSV 分隔符（目录类接口支持） |
| `page` / `outputsize` | 分页参数（目录类接口支持） |

### 成本提示

`/etfs/world`（800 credits）= Summary + Performance + Risk + Composition（4 × 200）的打包，总成本相同，仅减少请求次数。若只需某一维度数据，直接调用对应的 200 credits 接口更划算。

### 套餐限制

- `/etfs/list`：Basic/Grow/Pro（个人）与 Venture（商业）套餐最多返回 **50 条**记录；Ultra/Enterprise/Custom 可获取 **40,000+ ETF 全量数据**
- `/etfs/world*` 系列接口：需 **Ultra（个人）/ Enterprise（商业）** 及以上套餐
