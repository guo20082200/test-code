# 加法

加法（ADD）端点计算两个输入数据序列（如技术指标或价格数据）之和，并返回合并后的结果。该端点适用于需要聚合数据点以创建自定义指标，或在金融分析中分析多个数据序列组合效应的用户。

**API credits 消耗：** 每个标的 `1`

## 参数

- `symbol` (string, 可选, 见备注)
  标的代码。例如 `AAPL`、`EUR/USD`、`ETH/BTC`...
  示例值：`AAPL`


- `figi` (string, 可选, 见备注)
  请求数据的标的 FIGI。该参数在 Ultra 套餐（个人）和 Enterprise 套餐（企业）及以上可用。
  示例值：`BBG000B9Y5X2`


- `isin` (string, 可选, 见备注)
  通过国际证券识别码（ISIN）查找标的。所有套餐均可用。在响应中返回 ISIN 值需要 ISIN Data 附加组件。
  示例值：`US0378331005`


- `cusip` (string, 可选, 见备注)
  通过美国证券统一识别程序委员会（CUSIP）编号查找标的。所有套餐均可用。在响应中返回 CUSIP 值需要 CUSIP Data 附加组件。
  示例值：`594918104`


- `interval` (string, 必填)
  时间序列中两个相邻数据点之间的间隔
  示例值：`1min`
  可选值：`1min`, `5min`, `15min`, `30min`, `45min`, `1h`, `2h`, `4h`, `8h`, `1day`, `1week`, `1month`


- `exchange` (string, 可选)
  标的交易所
  示例值：`NASDAQ`


- `mic_code` (string, 可选)
  符合 ISO 10383 标准的市场识别码（MIC）
  示例值：`XNAS`


- `country` (string, 可选)
  标的交易所在的国家，例如 `United States` 或 `US`
  示例值：`United States`


- `series_type_1` (string, 可选)
  用作技术指标第一部分的价格类型
  可选值：`close`, `open`, `high`, `low`, `volume`
  默认值：`open`


- `series_type_2` (string, 可选)
  用作技术指标第二部分的价格类型
  可选值：`close`, `open`, `high`, `low`, `volume`
  默认值：`close`


- `type` (string, 可选)
  标的所属的资产类别
  示例值：`Common Stock`
  可选值：`American Depositary Receipt`, `Bond`, `Bond Fund`, `Closed-end Fund`, `Common Stock`, `Depositary Receipt`, `Digital Currency`, `ETF`, `Exchange-Traded Note`, `Global Depositary Receipt`, `Limited Partnership`, `Mutual Fund`, `Physical Currency`, `Preferred Stock`, `REIT`, `Right`, `Structured Product`, `Trust`, `Unit`, `Warrant`


- `outputsize` (integer, 可选)
  要获取的数据点数量。支持 `1` 到 `5000` 范围内的值。未设置日期参数时默认为 `30`，否则设置为最大值
  默认值：`30`


- `format` (string, 可选)
  响应数据的格式
  可选值：`JSON`, `CSV`
  默认值：`JSON`


- `delimiter` (string, 可选)
  CSV 响应数据中使用的分隔符
  默认值：`;`


- `prepost` (boolean, 可选)
  返回包含盘前和盘后数据的行情。仅适用于 `Pro` 套餐（个人）和 `Venture` 套餐（企业）及以上。
美股在 `1min`、`5min`、`15min` 和 `30min` 周期下可用。
提供开盘、最高、最低、收盘价，不含成交量
  默认值：`false`


- `dp` (integer, 可选)
  指定浮点数的小数位数。
应在 [0, 11] 范围内（含边界）。
默认根据所提供的值自动确定小数位数
  默认值：`-1`


- `order` (string, 可选)
  输出结果的排序方式
  可选值：`asc`, `desc`
  默认值：`desc`


- `include_ohlc` (boolean, 可选)
  指定是否在输出中附加 OHLC 值
  可选值：`true`, `false`
  默认值：`false`


- `timezone` (string, 可选)
  输出日期时间所显示的时区。支持：

1. Exchange（交易所当地时间）
2. UTC（通用 UTC 标准时间）
3. IANA 时区数据库中的时区名称，例如 America/New_York、Asia/Singapore。完整时区列表可在此处查看

周期限制：timezone 参数仅适用于日内周期（小于 1 天）。对于 1day、1week 或 1month 周期，timezone 参数将被忽略，数据严格按照交易所当地时间返回。
注意 IANA 时区名称区分大小写
  默认值：`Exchange`


- `date` (string, 可选)
  指定要获取数据的确切日期。可以是具体日期，例如 `2021-10-27`，也可以用自然语言 `today`（今天）或 `yesterday`（昨天）
  示例值：`2021-10-27`


- `start_date` (string, 可选)
  可单独使用，也可与 `end_date` 一起使用。格式为 `2006-01-02` 或 `2006-01-02T15:04:05`

默认时区：

外汇和加密货币 - UTC
股票 - 交易所所在地时区（例如 AAPL 为 America/New_York）

如果提供了 timezone 参数，这两个参数都会将其纳入考虑。
如果指定了 timezone，则 start_date 和 end_date 将按指定时区使用

示例：

1. &symbol=AAPL&start_date=2019-08-09T15:50:00&…
返回从纽约时间 2019-08-09T15:50:00 起至当前日期的所有记录
2. &symbol=EUR/USD&timezone=Asia/Singapore&start_date=2019-08-09T15:50:00&…
返回从新加坡时间 2019-08-09T15:50:00 起至当前日期的所有记录
3. &symbol=ETH/BTC&timezone=Europe/Zurich&start_date=2019-08-09T15:50:00&end_date=2019-08-09T15:55:00&...
返回从苏黎世时间 2019-08-09T15:50:00 起至 2019-08-09T15:55:00 的所有记录

  示例值：`2024-08-22T15:04:05`


- `end_date` (string, 可选)
  数据选择的结束日期和时间，详见 `start_date` 说明。
  示例值：`2024-08-22T16:04:05`


- `previous_close` (boolean, 可选)
  布尔参数，用于在 time_series 数据中包含前一收盘价。若为 true，则将前一根K线的收盘价添加到当前对象中
  默认值：`false`


- `adjust` (string, 可选)
  价格复权模式
  可选值：`all`, `splits`, `dividends`, `none`
  默认值：`splits`


**备注：**

 - 以下参数至少需要提供一个：`symbol`、`isin`、`figi`、`cusip`。

## 响应

- `meta` (object)
  包含请求通用信息的 JSON 对象
  - `symbol` (string)
    请求数据的标的代码。
  - `interval` (string)
    相邻数据点之间的时间间隔。
  - `currency` (string)
    交易标的的货币。
  - `exchange_timezone` (string)
    标的交易所在地的时区。
  - `exchange` (string)
    标的交易的交易所名称。
  - `mic_code` (string)
    标的所在交易所的市场识别码（MIC）。
  - `type` (string)
    标的所属的资产类别。
  - `indicator` (object)
    技术指标信息
    - `name` (string)
      技术指标名称
    - `series_type_1` (string)
      用作技术指标第一部分的价格类型
    - `series_type_2` (string)
      用作技术指标第二部分的价格类型

- `values` (array of object)
  时间序列数据点数组
  - `datetime` (string)
    对应指定周期K线的开盘时间：股票为当地市场时间，外汇和加密货币为 UTC 时间
  - `add` (string)
    加法运算结果值
  - `open` (string)
    当前K线的开盘价。当 `include_ohlc` 为 `true` 时返回。
  - `high` (string)
    当前K线期间出现的最高价。当 `include_ohlc` 为 `true` 时返回。
  - `low` (string)
    当前K线期间出现的最低价。当 `include_ohlc` 为 `true` 时返回。
  - `close` (string)
    K线收盘时的价格。当 `include_ohlc` 为 `true` 时返回。
  - `volume` (string)
    当前K线期间的成交量。当 `include_ohlc` 为 `true` 时返回。

- `status` (string)
  响应状态


## 请求示例

```bash
curl "https://api.twelvedata.com/add?symbol=AAPL&interval=1min&apikey=demo"
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
            "name": "ADD - Arithmetic Addition",
            "series_type_1": "open",
            "series_type_2": "close"
        }
    },
    "values": [
        {
            "datetime": "2019-08-09 15:59:00",
            "add": "402.10798",
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
