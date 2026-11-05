# API Usage — 用量查询接口

> 官方文档：https://twelvedata.com/docs/advanced

## 概述

`/api_usage` 接口提供当前 API 用量的详细统计，包括已发起的请求数、剩余请求数以及用量限额的重置时间。适用于监控和管理 API 消耗，确保不超出分配的额度。

## 基本信息

| 项目 | 说明 |
|---|---|
| 路由 | `GET /api_usage` |
| API credits | `1` / request |
| 完整 URL | `https://api.twelvedata.com/api_usage` |

## 请求参数

| 参数 | 类型 | 必填 | 说明 | 示例 |
|---|---|---|---|---|
| `format` | string | 否 | 输出格式。可选值：`JSON`、`CSV` | `JSON`（默认） |
| `delimiter` | string | 否 | 下载 CSV 文件时使用的分隔符 | `;`（默认） |
| `timezone` | string | 否 | 输出时间戳的时区。可选值：`UTC`，或 IANA 时区库名称（如 `America/New_York`、`Asia/Singapore`，**区分大小写**） | `UTC`（默认） |
| `apikey` | string | ✱ 是 | API 密钥 | `your_api_key` |

## 请求示例

```
GET https://api.twelvedata.com/api_usage?apikey=your_api_key
```

## 响应示例

```json
{
    "timestamp": "2025-05-07 11:10:12",
    "current_usage": 4003,
    "plan_limit": 20000,
    "daily_usage": 12500,
    "plan_daily_limit": 100000,
    "plan_category": "enterprise"
}
```

## 响应字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `timestamp` | string | 统计时间点 |
| `current_usage` | integer | 当前周期已消耗 credits |
| `plan_limit` | integer | 套餐总量上限 |
| `daily_usage` | integer | 当日已消耗 credits |
| `plan_daily_limit` | integer | 套餐每日上限 |
| `plan_category` | string | 套餐类别（如 `enterprise`、`grow`、`pro`） |

## 使用场景

- **配额监控告警**：定时调用（仅 1 credit/次），当 `current_usage / plan_limit` 或 `daily_usage / plan_daily_limit` 超过阈值（如 80%）时触发告警或降频
- **多服务配额隔离**：在多个应用共用同一 API Key 时，用本接口实现调度层面的自适应限流
