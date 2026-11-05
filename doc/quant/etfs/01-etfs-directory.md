# ETFs Directory — ETF 目录接口

> 官方文档：https://twelvedata.com/docs/etfs

## 概述

`/etfs/list` 接口返回每日更新的交易所交易基金列表，按总资产降序排序。用于获取包含基金名称和资产规模的全面 ETF 数据，帮助用户快速识别可用的 ETF。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /etfs/list` |
| API credits | `1` / request |
| 最低套餐 | 无限制；但 Basic/Grow/Pro（个人）与 Venture（商业）套餐**最多返回 50 条**，Ultra/Enterprise/Custom 可获取 40,000+ ETF 全量数据 |
| 完整 URL | `https://api.twelvedata.com/etfs/list` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 否 | 按标的代码过滤 | `IVV` |
| `figi` | string | 否 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000BVZ697` |
| `isin` | string | 否 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US4642872000` |
| `cusip` | string | 否 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `464287200` |
| `cik` | string | 否 | 标的的 CIK 编码 | `95953` |
| `country` | string | 否 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `fund_family` | string | 否 | 按管理该基金的投资公司过滤 | `iShares` |
| `fund_type` | string | 否 | 按基金类型过滤 | `Large Blend` |
| `format` | string | 否 | 响应格式。支持：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | CSV 响应数据的分隔符 | `;`（默认） |
| `dp` | integer | 否 | 浮点值小数位数 | `5`（默认） |
| `page` | integer | 否 | 页码 | `1`（默认） |
| `outputsize` | integer | 否 | 响应中的记录数 | `50`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/etfs/list?country=United%20States&apikey=your_api_key
```

## 响应示例

```json
{
    "result": {
        "count": 1000,
        "list": [
            {
                "symbol": "IVV",
                "name": "iShares Core S&P 500 ETF",
                "country": "United States",
                "mic_code": "XNAS",
                "fund_family": "iShares",
                "fund_type": "Large Blend"
            }
        ]
    },
    "status": "ok"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `result.count` | integer | 符合条件的 ETF 总数 |
| `result.list[].symbol` | string | ETF 代码 |
| `result.list[].name` | string | ETF 名称 |
| `result.list[].country` | string | 所属国家 |
| `result.list[].mic_code` | string | 市场标识码（MIC，ISO 10383） |
| `result.list[].fund_family` | string | 基金家族（管理公司） |
| `result.list[].fund_type` | string | 基金类型 |
| `status` | string | 请求状态（`ok`） |
