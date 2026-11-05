# Overlap Studies（重叠研究）

> 叠加在价格图上，用于平滑或包络价格数据，突出趋势方向、支撑/阻力位与均值回归水平（如各类均线、布林带、抛物线 SAR、一目均衡表、肯特纳通道、McGinley Dynamic）。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（21 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/bbands` | 布林带 | [bbands.md](bbands.md) | [官方](https://twelvedata.com/docs/technical-indicators/bbands) | `High demand` |
| `/dema` | 双指数移动平均线 | [dema.md](dema.md) | [官方](https://twelvedata.com/docs/technical-indicators/dema) | |
| `/ema` | 指数移动平均线 | [ema.md](ema.md) | [官方](https://twelvedata.com/docs/technical-indicators/ema) | `High demand` |
| `/ht_trendline` | 希尔伯特变换瞬时趋势线 | [ht_trendline.md](ht_trendline.md) | [官方](https://twelvedata.com/docs/technical-indicators/ht_trendline) | |
| `/ichimoku` | 一目均衡表 | [ichimoku.md](ichimoku.md) | [官方](https://twelvedata.com/docs/technical-indicators/ichimoku) | |
| `/kama` | 考夫曼自适应移动平均线 | [kama.md](kama.md) | [官方](https://twelvedata.com/docs/technical-indicators/kama) | |
| `/keltner` | 肯特纳通道 | [keltner.md](keltner.md) | [官方](https://twelvedata.com/docs/technical-indicators/keltner) | |
| `/ma` | 移动平均线 | [ma.md](ma.md) | [官方](https://twelvedata.com/docs/technical-indicators/ma) | |
| `/mama` | MESA 自适应移动平均线 | [mama.md](mama.md) | [官方](https://twelvedata.com/docs/technical-indicators/mama) | |
| `/mcginley_dynamic` | 麦吉利动态指标 | [mcginley_dynamic.md](mcginley_dynamic.md) | [官方](https://twelvedata.com/docs/technical-indicators/mcginley_dynamic) | |
| `/midpoint` | 中点价 | [midpoint.md](midpoint.md) | [官方](https://twelvedata.com/docs/technical-indicators/midpoint) | |
| `/midprice` | 中间价 | [midprice.md](midprice.md) | [官方](https://twelvedata.com/docs/technical-indicators/midprice) | |
| `/pivot_points_hl` | 枢轴点高低点 | [pivot_points_hl.md](pivot_points_hl.md) | [官方](https://twelvedata.com/docs/technical-indicators/pivot_points_hl) | |
| `/sar` | 抛物线止损与反转 | [sar.md](sar.md) | [官方](https://twelvedata.com/docs/technical-indicators/sar) | |
| `/sarext` | 抛物线止损与反转扩展版 | [sarext.md](sarext.md) | [官方](https://twelvedata.com/docs/technical-indicators/sarext) | |
| `/sma` | 简单移动平均线 | [sma.md](sma.md) | [官方](https://twelvedata.com/docs/technical-indicators/sma) | `High demand` |
| `/t3ma` | 三重指数移动平均线 | [t3ma.md](t3ma.md) | [官方](https://twelvedata.com/docs/technical-indicators/t3ma) | |
| `/tema` | 三重指数移动平均线 | [tema.md](tema.md) | [官方](https://twelvedata.com/docs/technical-indicators/tema) | |
| `/trima` | 三角移动平均线 | [trima.md](trima.md) | [官方](https://twelvedata.com/docs/technical-indicators/trima) | |
| `/vwap` | 成交量加权平均价 | [vwap.md](vwap.md) | [官方](https://twelvedata.com/docs/technical-indicators/vwap) | |
| `/wma` | 加权移动平均线 | [wma.md](wma.md) | [官方](https://twelvedata.com/docs/technical-indicators/wma) | |
