# Twelve Data — Market State（市场状态）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `13-加密货币交易所`、`15-国家目录`、`16-工具类型目录`、`17-技术指标目录`

## 1. 接口概述

`market_state` 端点返回全部可用股票交易所的**实时开闭市状态**：每个交易所当前是开市还是休市，以及距下次开市/收盘的剩余时间。适合监控交易所营业状态、规划交易活动的场景。

- **接口地址**：`https://api.twelvedata.com/market_state`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/market_state?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `exchange` | string | 按交易所名称过滤 | `NYSE` | — |
| `code` | string | 按市场识别码（MIC）过滤 | `XNYS` | — |
| `country` | string | 按国家名称或缩写过滤 | `United States` / `US` | — |

## 4. 响应字段

⚠️ 响应为**顶层数组**（无 `data` / `status` 包装层），数组元素字段如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | string | 交易所全称 |
| `code` | string | 市场识别码（MIC，ISO 10383 标准） |
| `country` | string | 交易所所在国家 |
| `is_market_open` | boolean | 交易所当前是否开市 |
| `time_after_open` | string | 开市后已过时间（HH:MM:SS）；当前休市时返回 `00:00:00` |
| `time_to_open` | string | 距下次开市时间（HH:MM:SS）；当前开市时返回 `00:00:00` |
| `time_to_close` | string | 距本次收盘时间（HH:MM:SS）；当前休市时返回 `00:00:00` |

三个时间字段的互斥关系：开市时 `time_after_open` 和 `time_to_close` 有效、`time_to_open` 为 0；休市时只有 `time_to_open` 有效。

## 5. 响应示例

```json
[
    {
        "name": "NYSE",
        "code": "XNYS",
        "country": "United States",
        "is_market_open": true,
        "time_after_open": "02:39:03",
        "time_to_open": "00:00:00",
        "time_to_close": "05:20:57"
    }
]
```
