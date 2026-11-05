# Twelve Data — Technical Indicators（技术指标）接口文档

> 来源：https://twelvedata.com/docs/technical-indicators
>
> 对任意支持的时间序列按需计算常用技术指标（SMA、EMA、RSI、MACD、布林带等）。无需外部库即可实现图表叠加、信号生成与回测。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey` 查询参数 |
| 请求方式 | `GET` |
| 响应格式 | `JSON`（支持 `CSV`） |
| 计费单位 | `1` API credit / symbol |
| 接口总数 | 102 个，分 7 大类 |

## 分类目录

| # | 分类 | 中文名 | 指标数 | 说明 |
|---|---|---|---|---|
| 01 | [Overlap Studies](01-overlap-studies/README.md) | 重叠研究 | 21 | 叠加在价格图上，标注趋势/支撑阻力/均值回归（BBANDS、EMA、SMA、Ichimoku、Keltner、SAR 等） |
| 02 | [Momentum Indicators](02-momentum-indicators/README.md) | 动量指标 | 34 | 震荡指标，检测超买超卖/背离/动量变化（RSI、MACD 系列、Stochastic、ADX、CCI 等） |
| 03 | [Volume Indicators](03-volume-indicators/README.md) | 成交量指标 | 4 | 用成交量确认价格走势或预警衰竭（OBV、AD、ADOSC、Relative Volume） |
| 04 | [Volatility Indicators](04-volatility-indicators/README.md) | 波动率指标 | 5 | 量化价格波动幅度，评估风险/设置止损/识别突破（ATR、NATR、Supertrend 等） |
| 05 | [Price Transform](05-price-transform/README.md) | 价格变换 | 18 | OHLC 衍生序列与算术变换（TYPICAL、WCL、AVGPRICE、SUM、LOG 等） |
| 06 | [Cycle Indicators](06-cycle-indicators/README.md) | 周期指标 | 5 | 基于 Hilbert 变换检测周期性模式（HT_SINE、HT_DCPERIOD 等） |
| 07 | [Statistic Functions](07-statistic-functions/README.md) | 统计函数 | 15 | 统计度量，可作其他模型输入（STDDEV、VAR、LINEARREG、CORREL、BETA、TSF） |

每个子目录中包含该分类下所有指标的**中文翻译版文档**（从官方 `/docs/llms/` 源的英文版翻译），文件名与 API 路由一致，如 [bbands.md](01-overlap-studies/bbands.md)、[rsi.md](02-momentum-indicators/rsi.md)。

## 通用参数

所有指标接口共享以下参数结构：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，如 `AAPL`、`EUR/USD`、`ETH/BTC` |
| `figi` | string | 二选一 | FIGI 标识（Ultra/Enterprise 套餐） |
| `isin` / `cusip` | string | 可选 | ISIN / CUSIP 查询；返回对应值需附加包 |
| `interval` | string | ✱ 是 | 时间粒度：`1min`、`5min`、`15min`、`30min`、`45min`、`1h`、`2h`、`4h`、`8h`、`1day`、`1week`、`1month` |
| `exchange` | string | 否 | 交易所（如 `NASDAQ`） |
| `mic_code` | string | 否 | 市场标识码（ISO 10383，如 `XNAS`） |
| `country` | string | 否 | 国家名称或缩写（如 `US`） |
| `time_period` | integer | 视指标而定 | 计算周期 |
| `series_type` | string | 视指标而定 | 价格序列类型：`close`（默认）、`open`、`high`、`low` |
| `outputsize` | integer | 否 | 返回数据点数 |
| `timezone` | string | 否 | 输出时间戳时区（`Exchange` / `UTC` / IANA 名称，区分大小写） |
| `format` / `delimiter` | string | 否 | `JSON`/`CSV` 及 CSV 分隔符（默认 `;`） |
| `dp` | integer | 否 | 浮点值小数位数（默认 `5`） |
| `apikey` | string | ✱ 是 | API 密钥 |

各指标的专有参数（如 BBANDS 的 `ma_type`/`sd`、MACD 的 `fast_period`/`slow_period`/`signal_period`、STOCH 的 `slow_k`/`slow_d` 等）见对应指标文档。

## 请求示例

```
GET https://api.twelvedata.com/rsi?symbol=AAPL&interval=1min&time_period=14&apikey=your_api_key
```
