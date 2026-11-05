# Purchase Info — 共同基金购买信息接口

> 官方文档：https://twelvedata.com/docs/mutual-funds

## 概述

`/mutual_funds/world/purchase_info` 接口返回全球共同基金的详细购买信息，包括最低投资要求、当前定价以及可购买该基金的券商列表。适用于了解投资特定共同基金的准入门槛和可选渠道。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /mutual_funds/world/purchase_info` |
| API credits | `200` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/mutual_funds/world/purchase_info` |

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
GET https://api.twelvedata.com/mutual_funds/world/purchase_info?symbol=0P0001LCQ3&apikey=your_api_key
```

## 响应示例

```json
{
    "mutual_fund": {
        "purchase_info": {
            "expenses": {
                "expense_ratio_gross": 0.0022,
                "expense_ratio_net": 0.001
            },
            "minimums": {
                "initial_investment": 0,
                "additional_investment": 0,
                "initial_ira_investment": 0,
                "additional_ira_investment": 0
            },
            "pricing": {
                "nav": 10.09,
                "12_month_low": 9.630000114441,
                "12_month_high": 12.10000038147,
                "last_month": 11.050000190735
            },
            "brokerages": []
        }
    },
    "status": "ok"
}
```

## 响应字段说明

### mutual_fund.purchase_info

| 字段 | 类型 | 说明 |
|---|---|---|
| `expenses.expense_ratio_gross` | double | 总费率（未扣除豁免/报销） |
| `expenses.expense_ratio_net` | double | 净费率（扣除豁免/报销后） |
| `minimums.initial_investment` | double | 初始最低投资额 |
| `minimums.additional_investment` | double | 追加最低投资额 |
| `minimums.initial_ira_investment` | double | IRA 账户初始最低投资额 |
| `minimums.additional_ira_investment` | double | IRA 账户追加最低投资额 |
| `pricing.nav` | double | 单位净值（NAV） |
| `pricing.12_month_low` | double | 12 个月最低价 |
| `pricing.12_month_high` | double | 12 个月最高价 |
| `pricing.last_month` | double | 上月末净值 |
| `brokerages` | array | 可购买该基金的券商列表 |
| `status` | string | 请求状态（`ok`） |

> 该接口为 Mutual Funds 独有（ETFs 无对应接口）。
