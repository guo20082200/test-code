# Ratings — 共同基金评级接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/ratings` 接口返回全球市场共同基金的详细评级，包括由 Twelve Data 自行计算及来自各金融机构的绩效与质量评级数据。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/ratings` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/ratings` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 基金代码 | `0P0001LCQ3` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG00HMMLCH1` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `LU1206782309` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `120678230` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `dp` | integer | 否 | 浮点值小数位数，范围 `[0, 11]` | `5`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/mutual_funds/world/ratings?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "ratings": {
            "performance_rating": 2,
            "risk_rating": 4,
            "return_rating": 0
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### mutual_fund.ratings

| 字段 | 类型 | 说明 |
|---|---|---|
| `performance_rating` | integer | 绩效评级（0–5 星） |
| `risk_rating` | integer | 风险评级（0–5 星） |
| `return_rating` | integer | 收益评级（0–5 星） |
| `status` | string | 请求状态（`ok`） |

> 该接口为 Mutual Funds 独有（ETFs 无对应接口）。评级数据来源为 Twelve Data 自有计算及各金融机构。
