# Press Releases — 新闻稿接口

> 官方文档：https://twelvedata.com/docs#press-releases

## 概述

`/press_releases` 接口提供对全球市场上市公司官方新闻稿和公司公告的结构化实时访问。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /press_releases` |
| API credits | `1` / request |
| 最低套餐 | Basic（个人）及以上 |
| 完整 URL | `https://api.twelvedata.com/press_releases` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码，优先股用点号分隔，如 `BRK.A`、`BRK.B` | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 标的所在交易所名称 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（MIC，ISO 10383） | `XNAS` |

### 筛选与分页参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `start_date` | string | 否 | 起始日期，仅返回发布日期在此日期及之后的新闻稿。格式 `2025-12-24T02:07:00` | `2025-12-01T00:00:00` |
| `end_date` | string | 否 | 结束日期，仅返回发布日期在此日期及之前的新闻稿。格式 `2025-12-24T02:07:00` | `2025-12-31T23:59:00` |
| `language` | string | 否 | 逗号分隔的语言列表，按语言过滤新闻稿 | `en,en-US` |
| `timezone` | string | 否 | 日期过滤使用的时区，默认为标的时区 | `America/New_York` |
| `outputsize` | integer | 否 | 每页返回的新闻稿数量。默认 `2`，最大 `10` | `2` |
| `page` | integer | 否 | 页码，从 `1` 开始。与 `outputsize` 配合使用遍历结果集 | `1`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/press_releases?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "press_releases": [
        {
            "id": "20251201SF35699",
            "datetime": "2021-11-12T11:21:00+01:00",
            "title": "NVIDIA and Synopsys Announce Strategic Partnership to Revolutionize Engineering and Design",
            "body": "<b>Key Highlights</b><ul><li>Multi-year collaboration spans NVIDIA CUDA accelerated computing...</li></ul>...",
            "style": "/* Style Definitions */ ...",
            "language": [
                "en",
                "en-US"
            ]
        }
    ],
    "status": "ok",
    "pagination": {
        "current_page": 1,
        "per_page": 10
    }
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `press_releases[].id` | string | 新闻稿唯一标识 |
| `press_releases[].datetime` | string | 发布时间（ISO 8601，带时区偏移） |
| `press_releases[].title` | string | 新闻稿标题 |
| `press_releases[].body` | string | 新闻稿正文（HTML 格式） |
| `press_releases[].style` | string | 正文内嵌的样式定义 |
| `press_releases[].language` | array | 语言列表 |
| `status` | string | 请求状态（`ok`） |
| `pagination.current_page` | integer | 当前页码 |
| `pagination.per_page` | integer | 每页记录数 |
