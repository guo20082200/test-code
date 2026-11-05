# 肯特纳通道

肯特纳通道（Keltner Channel）端点提供基于波动率的技术指标数据，该指标结合指数移动平均线（EMA）和真实波动幅度均值（ATR），围绕证券价格形成一条通道。该端点返回通道的上轨、中轨和下轨，可用于识别潜在的超买或超卖状态、评估趋势方向以及发现可能的价格突破。

**API credits 消耗：** 每个标的 `1`

## 参数

- `symbol` (string, 可选, 见备注)
  标的代码。例如 `AAPL`、`EUR/USD`、`ETH/BTC`...
  示例值: `AAPL`


- `figi` (string, 可选, 见备注)
  所请求标的的 FIGI 编码。该参数适用于 Ultra 套餐（个人）及 Enterprise 套餐（企业）或更高版本。
  示例值: `BBG000B9Y5X2`


- `isin` (string, 可选, 见备注)
  通过国际证券识别码（ISIN）查找标的。所有套餐均可用。在响应中返回 ISIN 值需要 ISIN Data 附加组件。
  示例值: `US0378331005`


- `cusip` (string, 可选, 见备注)
  通过美国证券统一识别程序委员会（CUSIP）编号查找标的。所有套餐均可用。在响应中返回 CUSIP 值需要 CUSIP Data 附加组件。
  示例值: `594918104`


- `interval` (string, 必填)
  时间序列中两个相邻数据点之间的间隔
  示例值: `1min`
  可选值: `1min`, `5min`, `15min`, `30min`, `45min`, `1h`, `2h`, `4h`, `8h`, `1day`, `1week`, `1month`


- `exchange` (string, 可选)
  标的交易所
  示例值: `NASDAQ`


- `mic_code` (string, 可选)
  ISO 10383 标准下的市场识别码（MIC）
  示例值: `XNAS`


- `country` (string, 可选)
  标的交易所在的国家，例如 `United States` 或 `US`
  示例值: `United States`


- `time_period` (integer, 可选)
  用于计算平均的周期数。取值范围为 `1` 到 `800`
  默认值: `20`


- `atr_time_period` (integer, 可选)
  用于计算真实波动幅度均值（ATR）的周期。取值范围为 `1` 到 `800`
  默认值: `10`


- `multiplier` (integer, 可选)
  用于调整指标灵敏度的系数
  默认值: `2`


- `series_type` (string, 可选)
  技术指标计算所使用的价格类型
  可选值: `close`, `open`, `high`, `low`, `volume`
  默认值: `close`


- `ma_type` (string, 可选)
  所使用的移动平均线类型
  可选值: `SMA`, `EMA`, `WMA`, `DEMA`, `TEMA`, `TRIMA`, `KAMA`, `MAMA`, `T3MA`
  默认值: `SMA`


- `type` (string, 可选)
  标的所属的资产类别
  示例值: `Common Stock`
  可选值: `American Depositary Receipt`, `Bond`, `Bond Fund`, `Closed-end Fund`, `Common Stock`, `Depositary Receipt`, `Digital Currency`, `ETF`, `Exchange-Traded Note`, `Global Depositary Receipt`, `Limited Partnership`, `Mutual Fund`, `Physical Currency`, `Preferred Stock`, `REIT`, `Right`, `Structured Product`, `Trust`, `Unit`, `Warrant`


- `outputsize` (integer, 可选)
  要获取的数据点数量。支持 `1` 到 `5000` 范围内的值。未设置日期参数时默认为 `30`，否则设为最大值
  默认值: `30`


- `format` (string, 可选)
  响应数据的格式
  可选值: `JSON`, `CSV`
  默认值: `JSON`


- `delimiter` (string, 可选)
  CSV 响应数据中使用的分隔符
  默认值: `;`


- `prepost` (boolean, 可选)
  返回包含盘前和盘后数据的报价。仅适用于 `Pro` 套餐（个人）和 `Venture` 套餐（企业）或更高版本。
仅 `1min`、`5min`、`15min` 和 `30min` 间隔适用于美股。
提供开盘价、最高价、最低价、收盘价，不包含成交量
  默认值: `false`


- `dp` (integer, 可选)
  指定浮点数值的小数位数。
取值范围应为 [0, 11]（含边界）。
默认情况下，小数位数会根据所提供的数值自动确定
  默认值: `-1`


- `order` (string, 可选)
  输出结果的排序方式
  可选值: `asc`, `desc`
  默认值: `desc`


- `include_ohlc` (boolean, 可选)
  指定是否在输出中添加 OHLC 值
  可选值: `true`, `false`
  默认值: `false`


- `timezone` (string, 可选)
  输出日期时间所使用的时区。支持：

1. Exchange 表示交易所当地时间
2. UTC 表示通用 UTC 标准时间
3. IANA 时区数据库中的时区名称。例如 America/New_York、Asia/Singapore。完整的时区列表可在此处查看

间隔限制：timezone 参数仅适用于日内间隔（小于 1 天）。对于 1day、1week 或 1month 间隔，timezone 参数将被忽略，数据严格按交易所当地时间返回。
请注意，IANA 时区名称区分大小写
  默认值: `Exchange`


- `date` (string, 可选)
  指定获取数据的确切日期。可以是确切日期，例如 `2021-10-27`，也可以是自然语言 `today` 或 `yesterday`
  示例值: `2021-10-27`


- `start_date` (string, 可选)
  可单独使用，也可与 `end_date` 一起使用。格式为 `2006-01-02` 或 `2006-01-02T15:04:05`

默认时区：

外汇和加密货币 - UTC
股票 - 交易所所在地（例如 AAPL 为 America/New_York）

如果提供了 timezone 参数，两个参数都会将其纳入考虑。
如果指定了 timezone，则 start_date 和 end_date 将按指定的时区使用

示例：

1. &symbol=AAPL&start_date=2019-08-09T15:50:00&…
返回从纽约时间 2019-08-09T15:50:00 起至当前日期的所有记录
2. &symbol=EUR/USD&timezone=Asia/Singapore&start_date=2019-08-09T15:50:00&…
返回从新加坡时间 2019-08-09T15:50:00 起至当前日期的所有记录
3. &symbol=ETH/BTC&timezone=Europe/Zurich&start_date=2019-08-09T15:50:00&end_date=2019-08-09T15:55:00&...
返回从苏黎世时间 2019-08-09T15:50:00 起至 2019-08-09T15:55:00 的所有记录

  示例值: `2024-08-22T15:04:05`


- `end_date` (string, 可选)
  数据选择的结束日期和时间，详情见 `start_date` 说明。
  示例值: `2024-08-22T16:04:05`


- `previous_close` (boolean, 可选)
  一个布尔参数，用于在 time_series 数据中包含前一收盘价。若为 true，则将前一根 K 线的收盘价添加到当前对象中
  默认值: `false`


- `adjust` (string, 可选)
  价格的复权方式
  可选值: `all`, `splits`, `dividends`, `none`
  默认值: `splits`


**备注：**

 - 以下参数至少需要提供一个：`symbol`、`isin`、`figi`、`cusip`。

## 响应

- `meta` (object)
  包含请求通用信息的 JSON 对象
  - `symbol` (string)
    所请求数据的标的代码。
  - `interval` (string)
    相邻数据点之间的时间间隔。
  - `currency` (string)
    交易标的的货币。
  - `exchange_timezone` (string)
    标的交易所所在时区。
  - `exchange` (string)
    标的的交易所名称。
  - `mic_code` (string)
    标的交易所的市场识别码（MIC）。
  - `type` (string)
    标的所属的资产类别。
  - `indicator` (object)
    技术指标信息
    - `name` (string)
      技术指标名称
    - `time_period` (integer)
      用于计算平均的周期数
    - `atr_time_period` (integer)
      用于计算真实波动幅度均值（ATR）的周期
    - `multiplier` (integer)
      用于调整指标灵敏度的系数
    - `series_type` (string)
      技术指标计算所使用的价格类型
    - `ma_type` (string)
      所使用的移动平均线类型

- `values` (array of object)
  时间序列数据点数组
  - `datetime` (string)
    股票为当地时间、外汇和加密货币为 UTC 时间，表示指定间隔的 K 线开始时间
  - `upper_line` (string)
    上线值
  - `middle_line` (string)
    中线值
  - `lower_line` (string)
    下线值
  - `open` (string)
    当前 K 线的开盘价。当 `include_ohlc` 为 `true` 时返回。
  - `high` (string)
    当前 K 线期间的最高价。当 `include_ohlc` 为 `true` 时返回。
  - `low` (string)
    当前 K 线期间的最低价。当 `include_ohlc` 为 `true` 时返回。
  - `close` (string)
    当前 K 线结束时的收盘价。当 `include_ohlc` 为 `true` 时返回。
  - `volume` (string)
    当前 K 线期间的成交量。当 `include_ohlc` 为 `true` 时返回。

- `status` (string)
  响应状态


## 请求示例

```bash
curl "https://api.twelvedata.com/keltner?symbol=AAPL&interval=1min&apikey=demo"
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
            "name": "KELTNER - Keltner Channels",
            "time_period": 20,
            "atr_time_period": 10,
            "multiplier": 2,
            "series_type": "close",
            "ma_type": "SMA"
        }
    },
    "values": [
        {
            "datetime": "2019-08-09 15:59:00",
            "upper_line": "202.25298",
            "middle_line": "201.80985",
            "lower_line": "201.36672",
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
