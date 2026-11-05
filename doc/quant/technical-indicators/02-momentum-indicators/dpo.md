# 区间震荡线

区间震荡线（DPO）端点计算并返回指定金融工具在给定时间段内的 DPO 值。该端点通过突出短期价格周期、识别潜在的超买或超卖状态（不受长期趋势影响），为交易者提供帮助。响应包含一系列 DPO 值，可用于评估市场中的价格动量和周期性形态。

**API credits 消耗：**每个标的 `1`

## 参数

- `symbol` (string, optional, see notes)
  标的代码。例如 `AAPL`、`EUR/USD`、`ETH/BTC`、...
  示例值：`AAPL`


- `figi` (string, optional, see notes)
  所请求数据对应的金融工具的 FIGI。该参数在 Ultra 套餐（个人）及 Enterprise 套餐（企业）及以上可用。
  示例值：`BBG000B9Y5X2`


- `isin` (string, optional, see notes)
  通过国际证券识别码（ISIN）查找金融工具。所有套餐均可用。在响应中返回 ISIN 值需要 ISIN Data 附加组件。
  示例值：`US0378331005`


- `cusip` (string, optional, see notes)
  通过美国证券统一识别程序委员会（CUSIP）编号查找金融工具。所有套餐均可用。在响应中返回 CUSIP 值需要 CUSIP Data 附加组件。
  示例值：`594918104`


- `interval` (string, required)
  时间序列中两个相邻数据点之间的间隔
  示例值：`1min`
  可选值：`1min`、`5min`、`15min`、`30min`、`45min`、`1h`、`2h`、`4h`、`8h`、`1day`、`1week`、`1month`


- `exchange` (string, optional)
  标的交易所
  示例值：`NASDAQ`


- `mic_code` (string, optional)
  符合 ISO 10383 标准的市场识别码（MIC）
  示例值：`XNAS`


- `country` (string, optional)
  标的交易所在的国家，例如 `United States` 或 `US`
  示例值：`United States`


- `series_type` (string, optional)
  计算技术指标所使用的价格类型
  可选值：`close`、`open`、`high`、`low`、`volume`
  默认值：`close`


- `time_period` (integer, optional)
  用于求平均的周期数。取值范围为 `1` 到 `800`
  默认值：`9`


- `centered` (boolean, optional)
  指定是否应进行偏移以与当前价格对齐
  默认值：`false`


- `type` (string, optional)
  标的所属的资产类别
  示例值：`Common Stock`
  可选值：`American Depositary Receipt`、`Bond`、`Bond Fund`、`Closed-end Fund`、`Common Stock`、`Depositary Receipt`、`Digital Currency`、`ETF`、`Exchange-Traded Note`、`Global Depositary Receipt`、`Limited Partnership`、`Mutual Fund`、`Physical Currency`、`Preferred Stock`、`REIT`、`Right`、`Structured Product`、`Trust`、`Unit`、`Warrant`


- `outputsize` (integer, optional)
  要检索的数据点数量。支持 `1` 到 `5000` 范围内的取值。未设置日期参数时默认为 `30`，否则设为最大值
  默认值：`30`


- `format` (string, optional)
  响应数据的格式
  可选值：`JSON`、`CSV`
  默认值：`JSON`


- `delimiter` (string, optional)
  CSV 响应数据中使用的分隔符
  默认值：`;`


- `prepost` (boolean, optional)
  返回包含盘前和盘后数据的行情。仅适用于 `Pro` 套餐（个人）及 `Venture` 套餐（企业）及以上。
在美股的 `1min`、`5min`、`15min` 和 `30min` 周期下可用。
提供开盘价、最高价、最低价、收盘价，不含成交量
  默认值：`false`


- `dp` (integer, optional)
  指定浮点数值的小数位数。
取值范围应为 [0, 11]（含边界）。
默认情况下，小数位数根据所提供的数值自动确定
  默认值：`-1`


- `order` (string, optional)
  输出结果的排序方式
  可选值：`asc`、`desc`
  默认值：`desc`


- `include_ohlc` (boolean, optional)
  指定是否在输出中添加 OHLC 值
  可选值：`true`、`false`
  默认值：`false`


- `timezone` (string, optional)
  输出日期时间所显示的时区。支持：

1. Exchange - 交易所当地时间
2. UTC - 通用 UTC 标准时间的日期时间
3. IANA 时区数据库中的时区名称。例如 America/New_York、Asia/Singapore。完整的时区列表可在此处查看

周期限制：timezone 参数仅适用于日内周期（小于 1 天）。对于 1day、1week 或 1month 周期，timezone 参数将被忽略，数据严格按交易所当地时间返回。
请注意，IANA 时区名称区分大小写
  默认值：`Exchange`


- `date` (string, optional)
  指定获取数据的确切日期。可以是确切日期，例如 `2021-10-27`，也可以是自然语言 `today` 或 `yesterday`
  示例值：`2021-10-27`


- `start_date` (string, optional)
  可以单独使用，也可以与 `end_date` 一起使用。格式为 `2006-01-02` 或 `2006-01-02T15:04:05`

默认时区：

外汇和加密货币 - UTC
股票 - 交易所所在地（例如 AAPL 为 America/New_York）

如果提供了 timezone 参数，这两个参数都会将其考虑在内。
若指定了 timezone，则 start_date 和 end_date 将按指定的时区进行解释

示例：

1. &symbol=AAPL&start_date=2019-08-09T15:50:00&…
返回从纽约时间 2019-08-09T15:50:00 起至当前日期的所有记录
2. &symbol=EUR/USD&timezone=Asia/Singapore&start_date=2019-08-09T15:50:00&…
返回从新加坡时间 2019-08-09T15:50:00 起至当前日期的所有记录
3. &symbol=ETH/BTC&timezone=Europe/Zurich&start_date=2019-08-09T15:50:00&end_date=2019-08-09T15:55:00&...
返回从苏黎世时间 2019-08-09T15:50:00 起至 2019-08-09T15:55:00 的所有记录

  示例值：`2024-08-22T15:04:05`


- `end_date` (string, optional)
  数据选择的结束日期和时间，详情参见 `start_date` 的说明。
  示例值：`2024-08-22T16:04:05`


- `previous_close` (boolean, optional)
  一个布尔参数，用于在 time_series 数据中包含前一收盘价。若为 true，则将前一根 K 线的收盘价添加到当前对象中
  默认值：`false`


- `adjust` (string, optional)
  价格的复权方式
  可选值：`all`、`splits`、`dividends`、`none`
  默认值：`splits`


**备注：**

 - 以下参数至少需要提供一个：`symbol`、`isin`、`figi`、`cusip`。

## 响应

- `meta` (object)
  包含请求通用信息的 JSON 对象
  - `symbol` (string)
    所请求数据对应的金融工具的代码。
  - `interval` (string)
    相邻数据点之间的时间间隔。
  - `currency` (string)
    交易标的的货币。
  - `exchange_timezone` (string)
    标的交易所的时区。
  - `exchange` (string)
    标的交易所的名称。
  - `mic_code` (string)
    标的交易所的市场识别码（MIC）。
  - `type` (string)
    标的所属的资产类别。
  - `indicator` (object)
    技术指标信息
    - `name` (string)
      技术指标的名称
    - `series_type` (string)
      计算技术指标所使用的价格类型
    - `time_period` (integer)
      用于求平均的周期数
    - `centered` (boolean)
      指定是否应进行偏移以与当前价格对齐

- `values` (array of object)
  时间序列数据点数组
  - `datetime` (string)
    股票为当地时间、外汇和加密货币为 UTC 时间的日期时间，表示指定周期的 K 线的开始时间
  - `dpo` (string)
    DPO 值
  - `open` (string)
    当前 K 线的开盘价。当 `include_ohlc` 为 `true` 时返回。
  - `high` (string)
    当前 K 线期间的最高价。当 `include_ohlc` 为 `true` 时返回。
  - `low` (string)
    当前 K 线期间的最低价。当 `include_ohlc` 为 `true` 时返回。
  - `close` (string)
    当前 K 线的收盘价。当 `include_ohlc` 为 `true` 时返回。
  - `volume` (string)
    当前 K 线期间的成交量。当 `include_ohlc` 为 `true` 时返回。

- `status` (string)
  响应状态


## 请求示例

```bash
curl "https://api.twelvedata.com/dpo?symbol=AAPL&interval=1min&apikey=demo"
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "interval": "1min",
        "currency": "USD",
        "exchange_timezone": "America/New_York",
        "exchange": "NASDAQ",
        "mic_code": "XNAS",
        "type": "Common Stock",
        "indicator": {
            "name": "DPO - Detrended Price Oscillator",
            "series_type": "close",
            "time_period": 21,
            "centered": false
        }
    },
    "values": [
        {
            "datetime": "2025-04-01",
            "dpo": "-7.99619",
            "open": "148.73500",
            "high": "148.86000",
            "low": "148.73000",
            "close": "148.85001",
            "volume": "624277"
        }
    ],
    "status": "ok"
}
```
