# MFs Directory — 共同基金目录接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/list` 接口返回每日更新的共同基金列表，按总资产降序排序。用于获取可投资共同基金的整体概览。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/list` |
| API credits | `1` / request |
| 最低套餐 | 无限制；但 Basic/Grow/Pro（个人）与 Venture（商业）套餐**最多返回 50 条**，Ultra/Enterprise/Custom 可获取 140,000+ 共同基金全量数据 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/list` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 否 | 按标的代码过滤 | `0P0001LCQ3` |
| `figi` | string | 否 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG00HMMLCH1` |
| `isin` | string | 否 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `LU1206782309` |
| `cusip` | string | 否 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `120678230` |
| `cik` | string | 否 | 标的的 CIK 编码 | `95953` |
| `country` | string | 否 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `fund_family` | string | 否 | 按管理该基金的投资公司过滤 | `Jackson National` |
| `fund_type` | string | 否 | 按基金类型过滤 | `Small Blend` |
| `performance_rating` | integer | 否 | 按绩效评级过滤，范围 `0`–`5` | `4` |
| `risk_rating` | integer | 否 | 按风险评级过滤，范围 `0`–`5` | `4` |
| `format` | string | 否 | 响应格式。支持：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | CSV 响应数据的分隔符 | `;`（默认） |
| `dp` | integer | 否 | 浮点值小数位数 | `5`（默认） |
| `page` | integer | 否 | 页码 | `1`（默认） |
| `outputsize` | integer | 否 | 响应中的记录数 | `100`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/mutual_funds/list?country=United%20States&apikey=your_api_key
```

## 响应示例

```json
{
    "result": {
        "count": 1000,
        "list": [
            {
                "symbol": "0P0001LCQ3",
                "name": "JNL Small Cap Index Fund (I)",
                "country": "United States",
                "fund_family": "Jackson National",
                "fund_type": "Small Blend",
                "performance_rating": 2,
                "risk_rating": 4,
                "currency": "USD",
                "exchange": "OTC",
                "mic_code": "OTCM"
            }
        ]
    },
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `result.count` | integer | 符合条件的基金总数 |
| `result.list[].symbol` | string | 基金代码 |
| `result.list[].name` | string | 基金名称 |
| `result.list[].country` | string | 所属国家 |
| `result.list[].fund_family` | string | 基金家族（管理公司） |
| `result.list[].fund_type` | string | 基金类型 |
| `result.list[].performance_rating` | integer | 绩效评级（0–5） |
| `result.list[].risk_rating` | integer | 风险评级（0–5） |
| `result.list[].currency` | string | 计价币种 |
| `result.list[].exchange` | string | 交易所名称 |
| `result.list[].mic_code` | string | 市场标识码（MIC，ISO 10383） |
| `status` | string | 请求状态（`ok`） |
