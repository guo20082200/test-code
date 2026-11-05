# Twelve Data — Exchange Schedule（交易所交易时间表）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `11-交易所目录`、`13-加密货币交易所`、`14-市场状态`、`15-国家目录`、`16-工具类型目录`、`17-技术指标目录`

## 1. 接口概述

`exchange_schedule` 端点返回各股票交易所的**交易时间表**（交易时段与营业日信息）。适合需要知道特定交易所何时开市、据此安排交易活动的场景。

- **接口地址**：`https://api.twelvedata.com/exchange_schedule`
- **API 积分消耗**：每次请求 `100` 点（⚠️ 本目录中消耗最高的接口）
- **套餐限制**：需 Ultra（个人）或 Enterprise（企业）及以上套餐

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/exchange_schedule?apikey=your_api_key"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `date` | string | 指定查询日期。不传则返回默认（常规）时间表。支持三种格式：① 精确日期 `2021-10-27`；② 关键字 `today` / `yesterday`；③ UTC 完整时间串 `2025-04-11T20:00:00`（返回该时刻对应的当地日历日的时间表） | `2021-10-27` | — |
| `mic_name` | string | 按交易所名称过滤 | `NASDAQ` | — |
| `mic_code` | string | 按市场识别码（MIC，ISO 10383 标准）过滤 | `XNGS` | — |
| `country` | string | 按国家名称或缩写过滤 | `United States` / `US` | — |

**date 的时区换算规则**：使用 datetime 时，返回的时间表对应"该时刻的当地日历日"。例如 `2025-04-11T20:00:00` UTC 对应 `America/New_York` 时区的 2025-04-11、`Australia/Sydney` 时区的 2025-04-12。

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | object 数组 | 交易所时间表列表，见下表 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `title` | string | 交易所官方名称 |
| `name` | string | 交易所名称（简称） |
| `code` | string | 市场识别码（MIC） |
| `country` | string | 交易所所属国家 |
| `time_zone` | string | 交易所所在时区 |
| `sessions` | object 数组 | 交易时段列表，见下表 |

**`sessions` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `open_time` | string | 时段开盘时间（HH:MM:SS） |
| `close_time` | string | 时段收盘时间（HH:MM:SS） |
| `session_name` | string | 时段名称，如 `Pre market`（盘前） |
| `session_type` | string | 时段类型，如 `pre`（盘前）/ `regular`（常规） |

## 5. 响应示例

```json
{
    "data": [
        {
            "title": "NASDAQ/NGS (Global Select Market)",
            "name": "NASDAQ",
            "code": "XNYS",
            "country": "United States",
            "time_zone": "America/New_York",
            "sessions": [
                {
                    "open_time": "04:00:00",
                    "close_time": "09:30:00",
                    "session_name": "Pre market",
                    "session_type": "pre"
                }
            ]
        }
    ]
}
```
