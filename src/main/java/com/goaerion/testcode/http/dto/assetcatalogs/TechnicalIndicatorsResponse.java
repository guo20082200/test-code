package com.goaerion.testcode.http.dto.assetcatalogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Twelve Data 技术指标目录（technical_indicators）响应体
 * <p>注意：data 是以指标代码（如 macd、rsi）为键的 Map，值是该指标的元信息
 * <p>接口文档见 doc/asset_catalogs/17-技术指标目录-technical_indicators.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TechnicalIndicatorsResponse(
        /** 指标元信息 Map（键：指标代码；值：指标元信息） */
        Map<String, IndicatorInfo> data,
        /** 响应状态，如 ok */
        String status) {

    /**
     * 指标元信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IndicatorInfo(
            /** 是否已通过测试、审核并推荐使用 */
            Boolean enable,
            /** 指标全称 */
            @JsonProperty("full_name") String fullName,
            /** 指标简短描述 */
            String description,
            /** 指标所属分类，如 Momentum Indicators（动量指标） */
            String type,
            /** 是否应叠加绘制在价格 K 线上方 */
            Boolean overlay,
            /** 输出值定义 Map（键：输出参数名，如 macd_signal） */
            @JsonProperty("output_values") Map<String, OutputValueDef> outputValues,
            /** 输入参数定义 Map（键：参数名，如 fast_period） */
            Map<String, InputParamDef> parameters,
            /** 配色渲染定义 */
            TintingDef tinting) {
    }

    /**
     * 输出参数定义
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutputValueDef(
            /** 建议显示颜色（十六进制色值） */
            @JsonProperty("default_color") String defaultColor,
            /** 渲染方式，如 line */
            String display,
            /** 输出值下界（如有） */
            @JsonProperty("min_range") Integer minRange,
            /** 输出值上界（如有） */
            @JsonProperty("max_range") Integer maxRange) {
    }

    /**
     * 输入参数定义
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InputParamDef(
            /** 参数默认值 */
            Integer defaultValue,
            /** 保证正确计算的上界（如有） */
            @JsonProperty("max_range") Integer maxRange,
            /** 保证正确计算的下界（如有） */
            @JsonProperty("min_range") Integer minRange,
            /** 参数可用取值列表（如 open/high/low/close） */
            List<String> range,
            /** 参数类型：string / int / float / array */
            String type) {
    }

    /**
     * 配色渲染定义
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TintingDef(
            /** 渲染方式，如 fill */
            String display,
            /** 十六进制颜色码 */
            String color,
            /** 透明度（0~1 浮点数） */
            Double transparency,
            /** 配色下界（数字或输出参数名） */
            @JsonProperty("lower_bound") String lowerBound,
            /** 配色上界（数字或输出参数名） */
            @JsonProperty("upper_bound") String upperBound) {
    }
}
