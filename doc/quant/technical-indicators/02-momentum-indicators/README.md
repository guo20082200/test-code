# Momentum Indicators（动量指标）

> 震荡类指标，衡量价格运动的速度或强度，用于检测超买/超卖、背离与动量趋势变化（如 RSI、MACD、ROC、随机指标、ADX、CCI、Coppock 曲线、TRIX）。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（34 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/adx` | 平均趋向指数 | [adx.md](adx.md) | [官方](https://twelvedata.com/docs/technical-indicators/adx) | `High demand` |
| `/adxr` | 平均趋向指数评级 | [adxr.md](adxr.md) | [官方](https://twelvedata.com/docs/technical-indicators/adxr) | |
| `/apo` | 绝对价格振荡器 | [apo.md](apo.md) | [官方](https://twelvedata.com/docs/technical-indicators/apo) | |
| `/aroon` | Aroon 指标 | [aroon.md](aroon.md) | [官方](https://twelvedata.com/docs/technical-indicators/aroon) | |
| `/aroonosc` | Aroon 振荡器 | [aroonosc.md](aroonosc.md) | [官方](https://twelvedata.com/docs/technical-indicators/aroonosc) | |
| `/bop` | 多空力量 | [bop.md](bop.md) | [官方](https://twelvedata.com/docs/technical-indicators/bop) | |
| `/cci` | 商品通道指数 | [cci.md](cci.md) | [官方](https://twelvedata.com/docs/technical-indicators/cci) | |
| `/cmo` | 钱德动量振荡器 | [cmo.md](cmo.md) | [官方](https://twelvedata.com/docs/technical-indicators/cmo) | |
| `/coppock` | 估波曲线 | [coppock.md](coppock.md) | [官方](https://twelvedata.com/docs/technical-indicators/coppock) | |
| `/crsi` | 康纳斯相对强弱指标 | [crsi.md](crsi.md) | [官方](https://twelvedata.com/docs/technical-indicators/crsi) | |
| `/dpo` | 区间震荡线 | [dpo.md](dpo.md) | [官方](https://twelvedata.com/docs/technical-indicators/dpo) | |
| `/dx` | 动向指数 | [dx.md](dx.md) | [官方](https://twelvedata.com/docs/technical-indicators/dx) | |
| `/kst` | 确然信号 | [kst.md](kst.md) | [官方](https://twelvedata.com/docs/technical-indicators/kst) | |
| `/macd` | 指数平滑异同移动平均线 | [macd.md](macd.md) | [官方](https://twelvedata.com/docs/technical-indicators/macd) | `High demand` |
| `/macd_slope` | 指数平滑异同移动平均线斜率 | [macd_slope.md](macd_slope.md) | [官方](https://twelvedata.com/docs/technical-indicators/macd_slope) | |
| `/macdext` | 指数平滑异同移动平均线扩展 | [macdext.md](macdext.md) | [官方](https://twelvedata.com/docs/technical-indicators/macdext) | |
| `/mfi` | 资金流量指标 | [mfi.md](mfi.md) | [官方](https://twelvedata.com/docs/technical-indicators/mfi) | |
| `/minus_di` | 负向指标 | [minus_di.md](minus_di.md) | [官方](https://twelvedata.com/docs/technical-indicators/minus_di) | |
| `/minus_dm` | 负向变动 | [minus_dm.md](minus_dm.md) | [官方](https://twelvedata.com/docs/technical-indicators/minus_dm) | |
| `/mom` | 动量 | [mom.md](mom.md) | [官方](https://twelvedata.com/docs/technical-indicators/mom) | |
| `/percent_b` | %B 指标 | [percent_b.md](percent_b.md) | [官方](https://twelvedata.com/docs/technical-indicators/percent_b) | `High demand` |
| `/plus_di` | 正向指标 | [plus_di.md](plus_di.md) | [官方](https://twelvedata.com/docs/technical-indicators/plus_di) | |
| `/plus_dm` | 正向变动 | [plus_dm.md](plus_dm.md) | [官方](https://twelvedata.com/docs/technical-indicators/plus_dm) | |
| `/ppo` | 百分比价格振荡器 | [ppo.md](ppo.md) | [官方](https://twelvedata.com/docs/technical-indicators/ppo) | |
| `/roc` | 变动率 | [roc.md](roc.md) | [官方](https://twelvedata.com/docs/technical-indicators/roc) | |
| `/rocp` | 变动率百分比 | [rocp.md](rocp.md) | [官方](https://twelvedata.com/docs/technical-indicators/rocp) | |
| `/rocr` | 变动率比值 | [rocr.md](rocr.md) | [官方](https://twelvedata.com/docs/technical-indicators/rocr) | |
| `/rocr100` | 变动率比值 100 | [rocr100.md](rocr100.md) | [官方](https://twelvedata.com/docs/technical-indicators/rocr100) | |
| `/rsi` | 相对强弱指标 | [rsi.md](rsi.md) | [官方](https://twelvedata.com/docs/technical-indicators/rsi) | `High demand` |
| `/stoch` | 随机振荡器 | [stoch.md](stoch.md) | [官方](https://twelvedata.com/docs/technical-indicators/stoch) | `High demand` |
| `/stochf` | 快速随机振荡器 | [stochf.md](stochf.md) | [官方](https://twelvedata.com/docs/technical-indicators/stochf) | |
| `/stochrsi` | 随机相对强弱指标 | [stochrsi.md](stochrsi.md) | [官方](https://twelvedata.com/docs/technical-indicators/stochrsi) | |
| `/ultosc` | 终极振荡器 | [ultosc.md](ultosc.md) | [官方](https://twelvedata.com/docs/technical-indicators/ultosc) | |
| `/willr` | 威廉指标 | [willr.md](willr.md) | [官方](https://twelvedata.com/docs/technical-indicators/willr) | |
