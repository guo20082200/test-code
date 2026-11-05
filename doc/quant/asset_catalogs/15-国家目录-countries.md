# Twelve Data — Countries（国家目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `14-市场状态`、`16-工具类型目录`、`17-技术指标目录`

## 1. 接口概述

`countries` 端点返回完整的国家列表，包含 ISO 代码、官方名称、首都、货币等信息。适合本地化、货币转换、地理分析等需要准确国家信息的应用。

- **接口地址**：`https://api.twelvedata.com/countries`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/countries?apikey=demo"
```

## 3. 请求参数

无需任何参数。

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | object 数组 | 国家列表，见下表 |

**`data` 数组元素：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `iso2` | string | ISO 3166 两位国家代码，如 `US` |
| `iso3` | string | ISO 3166 三位国家代码，如 `USA` |
| `numeric` | string | ISO 3166 数字国家代码，如 `840` |
| `name` | string | 国家全称，如 `United States` |
| `official_name` | string | 国家官方名称，如 `United States of America` |
| `capital` | string | 首都，如 `Washington D.C.` |
| `currency` | string | 国家货币，如 `USD` |

## 5. 响应示例

```json
{
    "data": [
        {
            "iso2": "US",
            "iso3": "USA",
            "numeric": "840",
            "name": "United States",
            "official_name": "United States of America",
            "capital": "Washington D.C.",
            "currency": "USD"
        }
    ]
}
```
