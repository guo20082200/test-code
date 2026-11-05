# Batches — 批量请求接口

> 官方文档：https://twelvedata.com/docs/advanced

## 概述

`/batch` 接口允许同时请求多个金融工具、时间间隔和数据类型的数据。适用于在单次操作中高效收集多样化金融数据，减少逐个请求的需要。单条请求出错不影响其他请求的处理，每个错误独立上报，便于排查。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `POST /batch` |
| 请求方式 | **仅支持 JSON `POST`**（本组唯一非 GET 接口） |
| API credits | 按批内各端点实际消耗**累加**（等于逐个请求之和，不节省 credits） |
| 并发限制 | 并发请求数受订阅套餐限制 |

## 计费与配额规则

- credits 按批内被请求的端点各自计费，总消耗等于批内单条请求之和
- 若请求的数据量超出可用 credits，配额耗尽前会异步返回部分数据
- 批内一条或多条请求出错（如无效标的、不支持的间隔）**不影响其他请求的成功处理**；错误在响应中按条独立上报，可定位并修正具体问题而不影响整批

## 请求参数

无查询参数。所有内容通过 JSON 请求体传递，并需设置请求头：

| 请求头 | 值 |
|---|---|
| `Content-Type` | `application/json` |
| `Authorization` | `apikey <你的 API 密钥>` |

### 请求体结构

key-value 形式：

- **key**：唯一请求 ID（自定义，如 `req_1`、`price_aapl`）
- **value**：对象，含 `url` 字段，值为目标端点及其查询参数（含各自的 `apikey`）

## 请求示例

```bash
curl --location 'https://api.twelvedata.com/batch' \
--header 'Content-Type: application/json' \
--header 'Authorization: apikey demo' \
--data @- << EOF
{
    "req_1": {
        "url": "/time_series?symbol=AAPL&interval=1min&apikey=demo&outputsize=2"
    },
    "req_2": {
        "url": "/exchange_rate?symbol=USD/JPY&apikey=demo"
    },
    "req_3": {
        "url": "/currency_conversion?symbol=USD/JPY&amount=122&apikey=demo"
    }
}
EOF
```

## 响应示例

```json
{
    "code": 200,
    "status": "success",
    "data": {
        "req_1": {
            "response": {
                "meta": {
                    "currency": "USD",
                    "exchange": "NASDAQ",
                    "exchange_timezone": "America/New_York",
                    "interval": "1min",
                    "mic_code": "XNGS",
                    "symbol": "AAPL",
                    "type": "Common Stock"
                },
                "status": "ok",
                "values": [
                    {
                        "close": "248.6",
                        "datetime": "2025-02-21 12:51:00",
                        "high": "248.6",
                        "low": "248.4",
                        "open": "248.5",
                        "volume": "22290"
                    },
                    {
                        "close": "248.52",
                        "datetime": "2025-02-21 12:50:00",
                        "high": "248.59",
                        "low": "248.43",
                        "open": "248.52",
                        "volume": "64085"
                    }
                ]
            },
            "status": "success"
        },
        "req_2": {
            "response": {
                "rate": 149.25999,
                "symbol": "USD/JPY",
                "timestamp": 1740160260
            },
            "status": "success"
        },
        "req_3": {
            "response": {
                "amount": 18209.71933,
                "rate": 149.25999,
                "symbol": "USD/JPY",
                "timestamp": 1740160260
            },
            "status": "success"
        }
    }
}
```

## 响应结构说明

响应与请求体同构（key-value）：

| 字段 | 说明 |
|---|---|
| `code` | HTTP 状态码（如 `200`） |
| `status` | 整批处理状态（`success`） |
| `data.<请求ID>.response` | 该请求的正常响应体（与单独调用该端点的响应完全一致） |
| `data.<请求ID>.status` | 该条请求的状态（如 `success`；出错的条目在此处上报错误信息，不影响其他条目） |
