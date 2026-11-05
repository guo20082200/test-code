# EDGAR Filings — SEC 申报文件接口

> 官方文档：https://twelvedata.com/docs/regulatory

## 概述

`/edgar_filings/archive` 接口提供提交至美国证监会（SEC）的财务文件完整集合，包括实时与历史的表单、申报和附件。可获取公司披露、财务报表和监管申报的详细信息，直接访问 SEC EDGAR 系统中的核心合规与财务数据。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /edgar_filings/archive` |
| API credits | `50` / request |
| 最低套餐 | Ultra（个人）/ Enterprise（商业）及以上 |
| 完整 URL | `https://api.twelvedata.com/edgar_filings/archive` |

## 请求参数

✱ 以下标识参数**至少提供一个**：

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `symbol` | string | 二选一 | 标的代码 | `AAPL` |
| `figi` | string | 二选一 | 金融工具全球标识符（FIGI），需 Ultra/Enterprise 套餐 | `BBG000B9Y5X2` |
| `isin` | string | 可选 | 国际证券识别码（ISIN）；返回 ISIN 值需 ISIN Data 附加包 | `US0378331005` |
| `cusip` | string | 可选 | 美国 CUSIP 码；返回 CUSIP 值需 CUSIP Data 附加包 | `594918104` |
| `exchange` | string | 可选 | 按交易所名称过滤 | `NASDAQ` |
| `mic_code` | string | 可选 | 市场标识码（ISO 10383） | `XNGS` |
| `country` | string | 可选 | 按国家名称或缩写过滤，如 `United States` 或 `US` | `United States` |
| `form_type` | string | 否 | 按表单类型过滤 | `8-K` |
| `filled_from` | string | 否 | 按申报起始日期过滤（`YYYY-MM-DD`） | `2024-01-01` |
| `filled_to` | string | 否 | 按申报截止日期过滤（`YYYY-MM-DD`） | `2024-01-01` |
| `page` | integer | 否 | 页码 | `1`（默认） |
| `page_size` | integer | 否 | 响应记录数 | `10`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/edgar_filings/archive?symbol=AAPL&form_type=8-K&filled_from=2024-01-01&apikey=your_api_key
```

## 响应示例

```json
{
    "meta": {
        "symbol": "AAPL",
        "exchange": "NASDAQ",
        "mic_code": "XNGS",
        "type": "Common Stock"
    },
    "values": [
        {
            "cik": 1711463,
            "filed_at": 1726617600,
            "files": [
                {
                    "name": "primary_doc.html",
                    "size": 2980,
                    "type": "144",
                    "url": "https://www.sec.gov/Archives/edgar/data/1711463/000197185724000581/primary_doc.xml"
                }
            ],
            "filing_url": "https://www.sec.gov/Archives/edgar/data/1711463/0001971857-24-000581-index.htm",
            "form_type": "144",
            "ticker": [
                "AAPL"
            ]
        }
    ]
}
```

## 响应字段说明

### meta（标的元信息）

| 字段 | 说明 |
|---|---|
| `symbol` | 标的代码 |
| `exchange` / `mic_code` | 交易所 / 市场标识码 |
| `type` | 标的类型 |

### values[]

| 字段 | 类型 | 说明 |
|---|---|---|
| `cik` | integer | SEC 中央索引键（CIK），公司唯一标识 |
| `filed_at` | integer | 申报时间戳（Unix 时间） |
| `files[].name` | string | 文件名（如 `primary_doc.html`） |
| `files[].size` | integer | 文件大小（字节） |
| `files[].type` | string | 文件表单类型（如 `144`） |
| `files[].url` | string | 文件在 SEC.gov 的原始链接 |
| `filing_url` | string | 申报索引页链接 |
| `form_type` | string | 表单类型（如 `8-K`、`10-K`、`144`） |
| `ticker[]` | array | 关联标代码列表 |

## 使用场景

- 财报与重大事项跟踪：按 `form_type=8-K` 过滤重大事件披露
- 尽职调查：拉取 `10-K`/`10-Q` 历史归档
- 内幕交易合规审计：结合 `144` 表单与 `/insider_transactions` 交叉验证
