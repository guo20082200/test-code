# Sustainability — 共同基金 ESG 可持续性接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/sustainability` 接口返回全球共同基金的可持续性与 ESG（环境、社会、治理）评级详情，包括 ESG 评分、可持续性指标及基金标识信息。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/sustainability` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/sustainability` |

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
GET https://api.twelvedata.com/mutual_funds/world/sustainability?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "sustainability": {
            "score": 22,
            "corporate_esg_pillars": {
                "environmental": 3.73,
                "social": 10.44,
                "governance": 7.86
            },
            "sustainable_investment": false,
            "corporate_aum": 0.99486
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### mutual_fund.sustainability

| 字段 | 类型 | 说明 |
|---|---|---|
| `score` | integer | 综合 ESG / 可持续性总分 |
| `corporate_esg_pillars.environmental` | double | 环境（E）支柱评分 |
| `corporate_esg_pillars.social` | double | 社会（S）支柱评分 |
| `corporate_esg_pillars.governance` | double | 治理（G）支柱评分 |
| `sustainable_investment` | boolean | 是否为可持续投资（ESG 主题）基金 |
| `corporate_aum` | double | 企业资产管理规模占比 |
| `status` | string | 请求状态（`ok`） |

> 该接口为 Mutual Funds 独有（ETFs 无对应接口）。
