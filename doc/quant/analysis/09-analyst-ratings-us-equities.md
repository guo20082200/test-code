# Analyst Ratings US Equities — 美股分析师评级接口

> 官方文档：https://twelvedata.com/docs/analysis

## 概述

`/analyst_ratings/us_equities` 接口提供美国股票分析师评级的详细信息，包括各评级机构发布的最新评级、发布机构、评级变化等。适用于跟踪专业分析师对美股的覆盖意见及其变化。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /analyst_ratings/us_equities` |
| API credits | `200` / symbol |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/analyst_ratings/us_equities` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 按标的代码过滤 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 按交易所名称过滤 | `NASDAQ` |
| `mic_code` | string | 可选 | 交易所市场标识码（ISO 10383） | `XNAS` |
| `rating_change` | string | 否 | 按评级动作过滤。可选值：`Maintains`、`Upgrade`、`Downgrade`、`Initiates`、`Reiterates` | `Upgrade` |
| `outputsize` | integer | 否 | 响应记录数 | `30`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/analyst_ratings/us_equities?symbol=AAPL&apikey=your_api_key
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
            "analyst_name": "Brandon Nispel",
            "rating_change": "Maintains",
            "rating_current": "Overweight",
            "rating_prior": "Overweight",
            "time": "08:29:48",
            "action_price_target": "Raises",
            "price_target_current": 185.14,
            "price_target_prior": 177.01
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
| `time` | string | 评级发布时间 |
| `firm` | string | 发布评级的机构名称 |
| `analyst_name` | string | 分析师姓名 |
| `rating_change` | string | 评级动作（`Maintains` 维持 / `Upgrade` 上调 / `Downgrade` 下调 / `Initiates` 首次覆盖 / `Reiterates` 重申） |
| `rating_current` | string | 当前评级 |
| `rating_prior` | string | 此前评级 |
| `action_price_target` | string | 目标价动作（如 `Raises` 上调） |
| `price_target_current` | double | 当前目标价 |
| `price_target_prior` | double | 此前目标价 |
| `status` | string | 请求状态（`ok`） |

> 与 Snapshot 版本的差异：本接口多出 `analyst_name`、`time`、`action_price_target`、`price_target_current`、`price_target_prior` 字段，但仅覆盖美国市场；credits 消耗也更高（200 vs 75）。
