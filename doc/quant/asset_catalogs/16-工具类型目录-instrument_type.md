# Twelve Data — Instrument Type（工具类型目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `15-国家目录`、`17-技术指标目录`

## 1. 接口概述

`instrument_type` 端点返回 Twelve Data 支持的**全部金融工具类型**列表（如股票、ETF、加密货币等），可用于识别和分类金融工具，也可作为其他接口 `type` 过滤参数的合法取值来源（如 [01-股票目录](01-股票目录-stocks.md) 的 `type` 参数、[11-交易所目录](11-交易所目录-exchanges.md) 的 `type` 参数）。

- **接口地址**：`https://api.twelvedata.com/instrument_type`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/instrument_type?apikey=demo"
```

## 3. 请求参数

无需任何参数。

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `result` | string 数组 | 工具类型名称列表（注意：`result` 直接就是数组，与 funds/bonds 的 `result.count + result.list` 包装结构不同） |
| `status` | string | 响应状态 |

## 5. 响应示例

```json
{
    "result": [
        "Agricultural Product",
        "American Depositary Receipt",
        "Bond",
        "Bond Fund",
        "Closed-end Fund",
        "Common Stock",
        "Depositary Receipt",
        "Digital Currency",
        "Energy Resource",
        "ETF",
        "Exchange-Traded Note",
        "Global Depositary Receipt",
        "Index",
        "Industrial Metal",
        "Limited Partnership",
        "Livestock",
        "Mutual Fund",
        "Physical Currency",
        "Precious Metal",
        "Preferred Stock",
        "REIT",
        "Right",
        "Structured Product",
        "Trust",
        "Unit",
        "Warrant"
    ],
    "status": "ok"
}
```
