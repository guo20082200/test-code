# Analyst Ratings Snapshot — 分析师评级快照接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/analyst_ratings/light` 接口提供美国及国际市场分析师评级机构意见的精简汇总，包含买入、持有、卖出等核心评级数据，便于快速评估分析师对特定股票的整体情绪。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /analyst_ratings/light` |
| API credits | `75` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/analyst_ratings/light` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `exchange` | string | 可选 | 按交易所名称过滤 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383） | `XNAS` |
| `rating_change` | string | 否 | 按评级动作过滤。可选值：`Maintains`、`Upgrade`、`Downgrade`、`Initiates`、`Reiterates` | `Upgrade` |
| `outputsize` | integer | 否 | 响应记录数 | `30`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/analyst_ratings/light?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "name": "Apple Inc",
        "currency": "USD",
        "exchange_timezone": "America/New_York",
        "exchange": "NASDAQ",
        "mic_code": "XNGS",
        "type": "Common Stock"
    },
    "ratings": [
        {
            "date": "2022-08-19",
            "firm": "Keybanc",
            "rating_change": "Maintains",
            "rating_current": "Overweight",
            "rating_prior": "Overweight"
        }
    ],
    "status": "ok"
}
```

## 响应字段说明

### meta（标的元信息）

| 字段 | 说明 |
|---|---|
| `symbol` / `name` | 标的代码 / 公司名称 |
| `currency` | 计价币种 |
| `exchange_timezone` | 交易所时区（IANA） |
| `exchange` / `mic_code` | 交易所 / 市场标识码 |
| `type` | 标的类型（如 Common Stock） |

### ratings[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `date` | string | 评级发布日期（`YYYY-MM-DD`） |
| `firm` | string | 发布评级的机构名称 |
| `rating_change` | string | 评级动作（`Maintains` 维持 / `Upgrade` 上调 / `Downgrade` 下调 / `Initiates` 首次覆盖 / `Reiterates` 重申） |
| `rating_current` | string | 当前评级（如 Overweight、Buy、Hold） |
| `rating_prior` | string | 此前评级 |
| `status` | string | 请求状态（`ok`） |

> 与 US Equities 版本的差异：本接口为轻量快照，不含 `analyst_name`（分析师姓名）、`time`（发布时间）及目标价调整字段，且覆盖全球市场。
