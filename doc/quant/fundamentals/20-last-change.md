# Last Changes — 数据更新时间接口

> 官方文档：https://twelvedata.com/docs#last-changes

## 概述

`/last_change/{endpoint}` 接口返回指定数据集基本面数据的最近更新信息。返回数据最后修改的时间戳，帮助用户仅在数据发生变化时才发起请求，从而优化数据获取流程并减少不必要的 API credits 消耗。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /last_change/{endpoint}` |
| API credits | `1` / request |
| 最低套餐 | 无限制（所有套餐可用） |
| 完整 URL | `https://api.twelvedata.com/last_change/{endpoint}` |

> 注意：路由中的 `{endpoint}` 为路径参数而非查询参数，例如 `/last_change/statistics`。

## 请求参数

### 路径参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `endpoint` | string | ✱ 是 | 目标数据集名称。支持：`price_target`、`recommendations`、`statistics`、`insider_transactions`、`profile`、`mutual_funds_world_summary`、`mutual_funds_world`、`institutional_holders`、`analyst_rating`、`income_statement`、`income_statement_quarterly`、`cash_flow`、`cash_flow_quarterly`、`balance_sheet`、`balance_sheet_quarterly`、`mutual_funds_list`、`mutual_funds_world_sustainability`、`mutual_funds_world_risk`、`mutual_funds_world_purchase_info`、`mutual_funds_world_composition`、`mutual_funds_world_performance`、`etfs_list`、`etfs_world`、`etfs_world_summary`、`etfs_world_performance`、`etfs_world_risk`、`etfs_world_composition`、`dividends`、`splits`、`money_market_funds_world` | `statistics` |

### 筛选与分页参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `start_date` | string | 否 | 数据筛选起始日期时间。格式 `2006-01-02T15:04:05` | `2023-10-14T00:00:00` |
| `symbol` | string | 否 | 按标的代码过滤 | `AAPL` |
| `exchange` | string | 否 | 按交易所名称过滤 | `NASDAQ` |
| `mic_code` | string | 否 | 按市场标识码（MIC，ISO 10383）过滤 | `XNAS` |
| `country` | string | 否 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `page` | integer | 否 | 页码 | `1`（默认） |
| `outputsize` | integer | 否 | 响应中的记录数 | `30`（默认） |

## 请求示例

```
GET https://api.twelvedata.com/last_change/statistics?symbol=AAPL&apikey=your_api_key
```

## 响应示例

```json
{
    "pagination": {
        "current_page": 1,
        "per_page": 30
    },
    "data": [
        {
            "symbol": "AAPL",
            "mic_code": "XNAS",
            "last_change": "2023-10-14 12:22:48"
        }
    ]
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `pagination.current_page` | integer | 当前页码 |
| `pagination.per_page` | integer | 每页记录数 |
| `data[].symbol` | string | 公司代码 |
| `data[].mic_code` | string | 市场标识码 |
| `data[].last_change` | string | 最后更新日期时间，格式 `2006-01-02 15:04:05` |

## 典型使用场景

1. **增量同步**：先查询 `last_change`，仅对 `last_change` 晚于本地缓存时间的标的数据重新拉取。
2. **降低成本**：避免对未变化的数据重复消耗 credits（尤其针对 `/statistics` 等高消耗接口）。
