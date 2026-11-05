# Statistic Functions（统计函数）

> 对价格序列计算基础统计度量——离散度、回归、相关性、预测成分——用于独立分析或作为其他模型的输入（如 STDDEV、VAR、LINEARREG、CORREL、TSF、BETA）。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（15 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/beta` | Beta 指标 | [beta.md](beta.md) | [官方](https://twelvedata.com/docs/technical-indicators/beta) | |
| `/correl` | 相关性 | [correl.md](correl.md) | [官方](https://twelvedata.com/docs/technical-indicators/correl) | |
| `/linearreg` | 线性回归 | [linearreg.md](linearreg.md) | [官方](https://twelvedata.com/docs/technical-indicators/linearreg) | |
| `/linearregangle` | 线性回归角度 | [linearregangle.md](linearregangle.md) | [官方](https://twelvedata.com/docs/technical-indicators/linearregangle) | |
| `/linearregintercept` | 线性回归截距 | [linearregintercept.md](linearregintercept.md) | [官方](https://twelvedata.com/docs/technical-indicators/linearregintercept) | |
| `/linearregslope` | 线性回归斜率 | [linearregslope.md](linearregslope.md) | [官方](https://twelvedata.com/docs/technical-indicators/linearregslope) | |
| `/max` | 最大值 | [max.md](max.md) | [官方](https://twelvedata.com/docs/technical-indicators/max) | |
| `/maxindex` | 最大值索引 | [maxindex.md](maxindex.md) | [官方](https://twelvedata.com/docs/technical-indicators/maxindex) | |
| `/min` | 最小值 | [min.md](min.md) | [官方](https://twelvedata.com/docs/technical-indicators/min) | |
| `/minindex` | 最小值索引 | [minindex.md](minindex.md) | [官方](https://twelvedata.com/docs/technical-indicators/minindex) | |
| `/minmax` | 最小值和最大值 | [minmax.md](minmax.md) | [官方](https://twelvedata.com/docs/technical-indicators/minmax) | |
| `/minmaxindex` | 最小值和最大值索引 | [minmaxindex.md](minmaxindex.md) | [官方](https://twelvedata.com/docs/technical-indicators/minmaxindex) | |
| `/stddev` | 标准差 | [stddev.md](stddev.md) | [官方](https://twelvedata.com/docs/technical-indicators/stddev) | |
| `/tsf` | 时间序列预测 | [tsf.md](tsf.md) | [官方](https://twelvedata.com/docs/technical-indicators/tsf) | |
| `/var` | 方差 | [var.md](var.md) | [官方](https://twelvedata.com/docs/technical-indicators/var) | |
