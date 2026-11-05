# Summary — ETF 概况接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/world/summary` 接口返回全球交易所交易基金的简明概览，包括 ETF 名称、代码和当前市场价值等关键数据点。适用于快速了解全球 ETF 整体格局而无需深入详细分析的快照场景。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/world/summary` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/etfs/world/summary` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | ETF 代码 | `IVV` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000BVZ697` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US4642872000` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `464287200` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/etfs/world/summary?symbol=IVV&apikey=your_api_key
```

## 响应示例

```json
{
    "etf": {
        "summary": {
            "symbol": "IVV",
            "name": "iShares Core S&P 500 ETF",
            "fund_family": "iShares",
            "fund_type": "Large Blend",
            "currency": "USD",
            "share_class_inception_date": "2000-11-13",
            "ytd_return": -0.0537,
            "expense_ratio_net": -0.004,
            "yield": 0.0133,
            "nav": 413.24,
            "last_price": 413.24,
            "turnover_rate": 0.04,
            "net_assets": 753409982464,
            "overview": "The investment seeks to track the performance of the Standard & Poor's 500..."
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### etf.summary

| 字段 | 类型 | 说明 |
|---|---|---|
| `symbol` | string | ETF 代码 |
| `name` | string | ETF 名称 |
| `fund_family` | string | 基金家族（管理公司） |
| `fund_type` | string | 基金类型 |
| `currency` | string | 计价币种 |
| `share_class_inception_date` | string | 份额类别成立日期（`YYYY-MM-DD`） |
| `ytd_return` | double | 年初至今收益率 |
| `expense_ratio_net` | double | 净费率（费用比率） |
| `yield` | double | 收益率（股息/分红收益） |
| `nav` | double | 单位净值（NAV） |
| `last_price` | double | 最新市场价格 |
| `turnover_rate` | double | 换手率 |
| `net_assets` | integer | 净资产规模 |
| `overview` | string | 基金简介 |
| `status` | string | 请求状态（`ok`） |
