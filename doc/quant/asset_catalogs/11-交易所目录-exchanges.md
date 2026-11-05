# Twelve Data — Exchanges（交易所目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `10-最早时间戳`、`12-交易所交易时间表`、`13-加密货币交易所`、`14-市场状态`、`15-国家目录`、`16-工具类型目录`、`17-技术指标目录`

## 1. 接口概述

`exchanges` 端点（高需求接口）返回**每日更新**的全部可用股票交易所列表，每条记录包含交易所代码、名称、国家、时区等详细信息。

- **接口地址**：`https://api.twelvedata.com/exchanges`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/exchanges?apikey=demo"
```

## 3. 请求参数

所有参数均可选（用于过滤结果）：

| 参数 | 类型 | 说明 | 示例值 | 默认值 |
| --- | --- | --- | --- | --- |
| `type` | string | 按交易所支持的资产类别过滤，20 种取值 | `ETF` | — |
| `name` | string | 按交易所名称过滤 | `NASDAQ` | — |
| `code` | string | 按市场识别码（MIC，ISO 10383 标准）过滤 | `XBUE` | — |
| `country` | string | 按国家名称或缩写过滤 | `United States` / `US` | — |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | `;` | `;` |
| `show_plan` | boolean | 是否在响应中附加该交易所可用的套餐信息（`access` 字段） | `true` | `false` |

**`type` 可选值（20 种）**：`American Depositary Receipt`（美国存托凭证）、`Bond`（债券）、`Bond Fund`（债券基金）、`Closed-end Fund`（封闭式基金）、`Common Stock`（普通股）、`Depositary Receipt`（存托凭证）、`Digital Currency`（数字货币）、`ETF`、`Exchange-Traded Note`（交易所交易票据）、`Global Depositary Receipt`（全球存托凭证）、`Limited Partnership`（有限合伙）、`Mutual Fund`（共同基金）、`Physical Currency`（实体货币）、`Preferred Stock`（优先股）、`REIT`（房地产信托）、`Right`（权利）、`Structured Product`（结构化产品）、`Trust`（信托）、`Unit`（单位）、`Warrant`（认股权证）

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | object 数组 | 交易所列表，见下表 |
| `status` | string | 响应状态 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `title` | string | 交易所全称 |
| `name` | string | 交易所名称（简称） |
| `code` | string | 市场识别码（MIC，ISO 10383 标准） |
| `country` | string | 交易所所属国家 |
| `timezone` | string | 交易所所在时区 |
| `access` | object | 套餐可用信息（仅 `show_plan=true` 时返回），含 `global`（访问级别）/ `plan`（个人套餐名）/ `plan_business`（企业套餐名） |

## 5. 响应示例

```json
{
    "data": [
        {
            "title": "Argentinian Stock Exchange",
            "name": "BCBA",
            "code": "XBUE",
            "country": "Argentina",
            "timezone": "America/Argentina/Buenos_Aires",
            "access": {
                "global": "Pro",
                "plan": "Pro",
                "plan_business": "Basic"
            }
        }
    ],
    "status": "ok"
}
```
