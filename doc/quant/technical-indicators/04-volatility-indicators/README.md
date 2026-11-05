# Volatility Indicators（波动率指标）

> 量化价格随时间的波动范围或离散度，用于评估风险、设置止损或识别突破（如 ATR、NATR、True Range），以及 SuperTrend 等自适应叠加指标。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（5 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/atr` | 真实波动幅度均值 | [atr.md](atr.md) | [官方](https://twelvedata.com/docs/technical-indicators/atr) | |
| `/natr` | 归一化真实波动幅度均值 | [natr.md](natr.md) | [官方](https://twelvedata.com/docs/technical-indicators/natr) | |
| `/supertrend` | 超级趋势线 | [supertrend.md](supertrend.md) | [官方](https://twelvedata.com/docs/technical-indicators/supertrend) | |
| `/supertrend_heikinashicandles` | 超级趋势线 Heikin Ashi 蜡烛图 | [supertrend_heikinashicandles.md](supertrend_heikinashicandles.md) | [官方](https://twelvedata.com/docs/technical-indicators/supertrend_heikinashicandles) | |
| `/trange` | 真实波幅 | [trange.md](trange.md) | [官方](https://twelvedata.com/docs/technical-indicators/trange) | |
