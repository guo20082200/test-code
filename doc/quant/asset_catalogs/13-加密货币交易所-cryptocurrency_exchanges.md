# Twelve Data — Cryptocurrency Exchanges（加密货币交易所）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `12-交易所交易时间表`、`14-市场状态`、`15-国家目录`、`16-工具类型目录`、`17-技术指标目录`

## 1. 接口概述

`cryptocurrency_exchanges` 端点返回**每日更新**的全部可用加密货币交易所列表，每条记录仅含交易所名称。是最简单的目录接口之一。

- **接口地址**：`https://api.twelvedata.com/cryptocurrency_exchanges`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/cryptocurrency_exchanges?apikey=demo"
```

## 3. 请求参数

所有参数均可选：

| 参数 | 类型 | 说明 | 可选值 | 默认值 |
| --- | --- | --- | --- | --- |
| `format` | string | 响应数据格式 | `JSON`、`CSV` | `JSON` |
| `delimiter` | string | CSV 响应数据的分隔符 | — | `;` |

## 4. 响应字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | object 数组 | 加密货币交易所列表，每个元素仅一个字段：`name`（交易所名称，如 `Binance`） |
| `status` | string | 响应状态 |

## 5. 响应示例

```json
{
    "data": [
        {
            "name": "Binance"
        },
        {
            "name": "Coinbase Pro"
        },
        {
            "name": "Kraken"
        },
        {
            "name": "OKX"
        }
    ],
    "status": "ok"
}
```
