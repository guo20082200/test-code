# Volume Indicators（成交量指标）

> 利用成交量确认价格走势或预警衰竭——量价同步表明趋势强劲，背离则可能预示反转（如 OBV、Chaikin AD、AD 振荡器、相对成交量）。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（4 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/ad` | 累积/派发线 | [ad.md](ad.md) | [官方](https://twelvedata.com/docs/technical-indicators/ad) | |
| `/adosc` | 累积/派发线震荡指标 | [adosc.md](adosc.md) | [官方](https://twelvedata.com/docs/technical-indicators/adosc) | |
| `/obv` | 能量潮(OBV) | [obv.md](obv.md) | [官方](https://twelvedata.com/docs/technical-indicators/obv) | |
| `/rvol` | 相对成交量 | [rvol.md](rvol.md) | [官方](https://twelvedata.com/docs/technical-indicators/rvol) | |
