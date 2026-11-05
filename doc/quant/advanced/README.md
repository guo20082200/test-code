# Twelve Data — Advanced（高级）接口文档

> 来源：https://twelvedata.com/docs/advanced
>
> 面向高级用户的高吞吐与管理端点：提交并监控批量作业以异步拉取大数据集，以编程方式跟踪 API 用量与配额，以及其他面向开发者的自动化与规模化数据工作流工具。

## 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `https://api.twelvedata.com` |
| 鉴权 | 所有请求需携带 `apikey`（batch 接口通过 `Authorization` 请求头传入） |
| 接口总数 | 2 个 |

## 接口目录

| # | 文档 | 接口 | 方式 | Credits | 说明 |
|---|---|---|---|---|---|
| 01 | [Batches](01-batches.md) | `/batch` | `POST`（JSON） | 按批内各端点累加 | 多标的、多数据类型批量请求 |
| 02 | [API usage](02-api-usage.md) | `/api_usage` | `GET` | 1 / request | 当前 API 用量与配额统计 |

## 典型工作流

1. **配额监控**：定时调用 `/api_usage`（1 credit），当 `current_usage` 接近 `plan_limit` 或 `daily_usage` 接近 `plan_daily_limit` 时触发告警或降频
2. **批量拉取**：定时任务中用 `/batch` 一次 POST 打包多个标的、多种数据类型（时间序列 + 汇率 + 换算等）的请求，减少请求管理开销
3. **成本提示**：`/batch` 不节省 credits（总消耗等于逐个请求之和），节省的是请求次数与并发调度成本；并发数受套餐限制
