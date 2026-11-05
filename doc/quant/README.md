# doc/quant/ — Twelve Data API 文档库说明

> 本目录是 **Twelve Data 金融数据 API** 的完整中文文档库，覆盖行情、基本面、技术指标、共识分析、合规监管及开发者工具等全部核心领域。官方文档地址：https://twelvedata.com/docs

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数（batch 接口通过 `Authorization` 请求头） |
| 请求方式 | 绝大多数为 `GET`，仅 `/batch` 为 JSON `POST` |
| 计费单位 | API credits（按 request 或 per symbol 计，各接口不同） |
| 响应格式 | `JSON`（部分接口支持 `CSV`） |

## 目录结构

| 目录 | 主题 | 文档数 | 内容概要 |
|---|---|---|---|
| [market_data](market_data/) | 行情数据 | 5 | time_series（时间序列）、quote（实时报价）、price（最新价）、eod（收盘价）、time_series_cross（交叉汇率时间序列） |
| [asset_catalogs](asset_catalogs/) | 资产目录 | 17 | 股票/外汇/加密货币/ETF/基金/大宗商品/债券目录，代码搜索、交叉上市、交易所、市场状态、国家等；附 `.http` 调试请求文件 |
| [fundamentals](fundamentals/) | 公司基本面 | 20 | Logo、Profile、股息/拆股/财报及其日历、IPO 日历、统计、新闻稿、三大报表（标准版 + 合并版）、市值、高管、Last Change |
| [currency](currency/) | 货币转换 | 2 | exchange_rate（汇率）、currency_conversion（货币换算） |
| [etfs](etfs/) | ETF | 8 | ETF 目录/家族/类型 + 全量数据与 Summary、Performance、Risk、Composition |
| [mutual_funds](mutual_funds/) | 共同基金 | 10 | 基金目录/家族/类型 + 7 个数据维度（含 ETFs 没有的 Ratings、Purchase Info、Sustainability） |
| [analysis](analysis/) | 共识分析 | 9 | EPS/营收预估、EPS 趋势与修正、增长共识、分析师建议、目标价、评级快照与美股明细 |
| [regulatory](regulatory/) | 合规监管 | 7 | SEC EDGAR 申报、内幕交易、机构/基金/直接持有人、税务信息、制裁实体清单 |
| [technical-indicators](technical-indicators/) | 技术指标 | 102 | 7 大类：重叠研究 21、动量 34、成交量 4、波动率 5、价格变换 18、周期 5、统计函数 15（中文翻译版，按子类分目录） |
| [advanced](advanced/) | 开发者工具 | 2 | batch（批量请求，JSON POST）、api_usage（用量与配额查询） |

**合计：约 182 个接口的文档。**

## 文档约定

- 每个子目录的 `README.md` 为该类的索引与通用说明（参数表、套餐限制、成本提示），各接口文档包含：概述、路由、API credits、最低套餐、完整参数表（类型/必填/默认值/示例）、请求示例与响应示例及字段说明
- 技术指标目录内文件名与 API 路由一致（如 [rsi.md](technical-indicators/02-momentum-indicators/rsi.md) → `GET /rsi`），并保留了官方状态标签（`High demand` / `Useful` / `New`）
- 文档头部标注来源官方文档链接，便于与官网同步校对

## 通用参数速查

### 标的标识参数（多数接口至少提供一个）

| 参数 | 说明 |
|---|---|
| `symbol` | 标的代码，如 `AAPL`、`EUR/USD`、`ETH/BTC`；优先股用点号分隔（`BRK.A`） |
| `figi` | FIGI 标识（Ultra/Enterprise 套餐） |
| `isin` / `cusip` | ISIN / CUSIP 查询；返回对应值需附加包 |
| `exchange` / `mic_code` | 交易所名称 / 市场标识码（ISO 10383，如 `XNAS`） |
| `country` | 国家名称或缩写（部分接口不支持，如 `/tax_info`） |

### 行情类接口通用参数

| 参数 | 说明 |
|---|---|
| `interval` | 时间粒度：`1min`、`5min`、`15min`、`30min`、`45min`、`1h`、`2h`、`4h`、`8h`、`1day`、`1week`、`1month` |
| `outputsize` | 返回数据点数（默认 100） |
| `timezone` | 输出时区：`Exchange` / `UTC` / IANA 名称（区分大小写） |
| `dp` | 浮点值小数位数，范围 `[0, 11]`，默认 `5` |
| `format` / `delimiter` | `JSON`/`CSV` 及 CSV 分隔符（默认 `;`） |

## 套餐与成本注意事项

1. **套餐门槛**：多数数据接口需 Pro 及以上；ETF/MF 数据类与 analysis、regulatory 大部分接口需 **Ultra/Enterprise**
2. **高成本接口**（调用前务必评估）：
   - 持有人类（institutional/fund/direct holders）：**1500 credits/symbol**，全站最贵
   - MF full data：1000 credits；ETF full data：800 credits
   - US Equities 分析师评级：200 credits/symbol；Key Executives：1000 credits/symbol
3. **低成本替代策略**：ETF/MF 的全量打包接口（800/1000 credits）与其子接口（各 200）总成本相同，仅省请求次数；只需要单一维度时直接调用子接口
4. **配额监控**：使用 `/api_usage`（1 credit/次）实现用量告警与自适应限流；批量场景用 `/batch` 合并请求（不省 credits，省请求数）
