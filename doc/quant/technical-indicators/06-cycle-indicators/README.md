# Cycle Indicators（周期指标）

> 基于 Hilbert 变换度量周期与相位，检测并跟踪价格行为中的周期性模式（如 HT_SINE、HT_DCPERIOD、HT_DCPHASE、HT_PHASOR、HT_TRENDMODE）。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（5 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/ht_dcperiod` | 希尔伯特变换主导周期 | [ht_dcperiod.md](ht_dcperiod.md) | [官方](https://twelvedata.com/docs/technical-indicators/ht_dcperiod) | |
| `/ht_dcphase` | 希尔伯特变换主导周期相位 | [ht_dcphase.md](ht_dcphase.md) | [官方](https://twelvedata.com/docs/technical-indicators/ht_dcphase) | |
| `/ht_phasor` | 希尔伯特变换相量分量 | [ht_phasor.md](ht_phasor.md) | [官方](https://twelvedata.com/docs/technical-indicators/ht_phasor) | |
| `/ht_sine` | 希尔伯特变换正弦波 | [ht_sine.md](ht_sine.md) | [官方](https://twelvedata.com/docs/technical-indicators/ht_sine) | |
| `/ht_trendmode` | 希尔伯特变换趋势/周期模式 | [ht_trendmode.md](ht_trendmode.md) | [官方](https://twelvedata.com/docs/technical-indicators/ht_trendmode) | |
