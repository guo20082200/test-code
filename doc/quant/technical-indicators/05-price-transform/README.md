# Price Transform（价格变换）

> 将原始 OHLC 数据转换为衍生序列或聚合值，为其他指标提供输入或提供价格的不同视角（如典型价格、HLC3、加权收盘价，以及 SUM、AVG、LOG、SQRT 等算术变换）。

> 来源：https://twelvedata.com/docs/technical-indicators

## 指标清单（18 个）

所有指标均为 `GET` 请求，路由前缀 `https://api.twelvedata.com`，计费 `1` credit / symbol。本目录内文档为中文翻译版（文件名与 API 路由一致），英文原版可从官方文档链接获取。

| API 路由 | 指标名称 | 本地文档 | 官方文档 | 标签 |
|---|---|---|---|---|
| `/add` | 加法 | [add.md](add.md) | [官方](https://twelvedata.com/docs/technical-indicators/add) | |
| `/avg` | 平均值 | [avg.md](avg.md) | [官方](https://twelvedata.com/docs/technical-indicators/avg) | |
| `/avgprice` | 平均价格 | [avgprice.md](avgprice.md) | [官方](https://twelvedata.com/docs/technical-indicators/avgprice) | |
| `/ceil` | 向上取整 | [ceil.md](ceil.md) | [官方](https://twelvedata.com/docs/technical-indicators/ceil) | |
| `/div` | 除法 | [div.md](div.md) | [官方](https://twelvedata.com/docs/technical-indicators/div) | |
| `/exp` | 指数 | [exp.md](exp.md) | [官方](https://twelvedata.com/docs/technical-indicators/exp) | |
| `/floor` | 向下取整 | [floor.md](floor.md) | [官方](https://twelvedata.com/docs/technical-indicators/floor) | |
| `/heikinashicandles` | 平均K线(Heikin Ashi) | [heikinashicandles.md](heikinashicandles.md) | [官方](https://twelvedata.com/docs/technical-indicators/heikinashicandles) | |
| `/hlc3` | 最高最低收盘价均值 | [hlc3.md](hlc3.md) | [官方](https://twelvedata.com/docs/technical-indicators/hlc3) | |
| `/ln` | 自然对数 | [ln.md](ln.md) | [官方](https://twelvedata.com/docs/technical-indicators/ln) | |
| `/log10` | 以10为底对数 | [log10.md](log10.md) | [官方](https://twelvedata.com/docs/technical-indicators/log10) | |
| `/medprice` | 中间价 | [medprice.md](medprice.md) | [官方](https://twelvedata.com/docs/technical-indicators/medprice) | |
| `/mult` | 乘法 | [mult.md](mult.md) | [官方](https://twelvedata.com/docs/technical-indicators/mult) | |
| `/sqrt` | 平方根 | [sqrt.md](sqrt.md) | [官方](https://twelvedata.com/docs/technical-indicators/sqrt) | |
| `/sub` | 减法 | [sub.md](sub.md) | [官方](https://twelvedata.com/docs/technical-indicators/sub) | |
| `/sum` | 求和 | [sum.md](sum.md) | [官方](https://twelvedata.com/docs/technical-indicators/sum) | |
| `/typprice` | 典型价格 | [typprice.md](typprice.md) | [官方](https://twelvedata.com/docs/technical-indicators/typprice) | |
| `/wclprice` | 加权收盘价 | [wclprice.md](wclprice.md) | [官方](https://twelvedata.com/docs/technical-indicators/wclprice) | |
