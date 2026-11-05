# Twelve Data — Technical Indicators（技术指标目录）接口文档

> 所属类别：资产目录（Asset Catalogs）。目录下其他文档：`01-股票目录` ~ `16-工具类型目录`

## 1. 接口概述

`technical_indicators` 端点返回 Twelve Data 支持的**全部技术指标**的元信息字典（以指标代码为键的 Map），包含每个指标的名称、描述、分类、输出值、输入参数及绘图配色建议。适合需要动态集成技术分析工具（如自建 K 线图、指标配置面板）的应用，无需手工维护各指标的参数定义。

- **接口地址**：`https://api.twelvedata.com/technical_indicators`
- **API 积分消耗**：每次请求 `1` 点

## 2. 请求示例

```bash
curl "https://api.twelvedata.com/technical_indicators?apikey=demo"
```

## 3. 请求参数

无需任何参数。

## 4. 响应字段

响应中 `data` 是一个 **Map（键为指标代码，如 `macd`、`rsi`；值为该指标的元信息对象）**。

**指标元信息对象：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enable` | boolean | 该指标是否已通过测试、审核并推荐使用 |
| `full_name` | string | 指标全称，如 `Moving Average Convergence Divergence` |
| `description` | string | 指标简短描述 |
| `type` | string | 指标所属分类（组），如 `Momentum Indicators`（动量指标） |
| `overlay` | boolean | 该指标是否应叠加绘制在价格 K 线上方（true）/ 单独绘制在副图（false） |
| `output_values` | object | 输出值定义 Map（键为输出参数名，如 `macd`、`macd_signal`、`macd_hist`、`ad`、`adxr`、`aroonosc` 等） |
| `parameters` | object | 输入参数定义 Map（键为参数名，如 `series_type`、`fast_period`、`slow_period`、`time_period`、`signal_period` 等） |
| `tinting` | object | 配色渲染定义，见下表 |

**`output_values` 中每个输出参数定义：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `default_color` | string | 建议显示颜色（十六进制色值） |
| `display` | string | 输出值的渲染方式（如 `line`） |
| `min_range` | integer | 输出值的下界（如有） |
| `max_range` | integer | 输出值的上界（如有） |

**`parameters` 中每个输入参数定义：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `default` | integer | 参数默认值 |
| `max_range` | integer | 保证正确计算的上界（如有） |
| `min_range` | integer | 保证正确计算的下界（如有） |
| `range` | string 数组 | 参数的可用取值列表（如 `open`/`high`/`low`/`close`） |
| `type` | string | 参数类型：`string` / `int` / `float` / `array` |

**`tinting` 配色渲染定义：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `display` | string | 渲染方式（如 `fill` 填充） |
| `color` | string | 十六进制颜色码 |
| `transparency` | number | 透明度（0 ~ 1 的浮点数） |
| `lower_bound` | string | 配色下界，可为数字或输出参数名 |
| `upper_bound` | string | 配色上界，可为数字或输出参数名 |

## 5. 响应示例

```json
{
    "data": {
        "macd": {
            "enable": true,
            "full_name": "Moving Average Convergence Divergence",
            "description": "Moving Average Convergence Divergence(MACD) is ...",
            "type": "Momentum Indicators",
            "overlay": false,
            "output_values": {
                "parameter_name": {
                    "default_color": "#FF0000",
                    "display": "line",
                    "min_range": 0,
                    "max_range": 5
                }
            },
            "parameters": {
                "parameter_name": {
                    "default": 12,
                    "max_range": 1,
                    "min_range": 1,
                    "range": [
                        "open",
                        "high",
                        "low",
                        "close"
                    ],
                    "type": "int"
                }
            },
            "tinting": {
                "display": "fill",
                "color": "#FF0000",
                "transparency": 0.5,
                "lower_bound": "0",
                "upper_bound": "macd"
            }
        }
    },
    "status": "ok"
}
```

> 注：示例中 `parameter_name` 为文档的占位写法，实际响应中该键为具体的输出/输入参数名（如 `macd`、`fast_period`）。
