package com.goaerion.testcode.http.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.goaerion.testcode.http.dto.twelvedata.IndicatorResponse;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Twelve Data 技术指标透传接口
 * <p>102 个技术指标端点（doc/quant/technical-indicators/ 目录，文件名与 API 路由一致）共用同一套
 * 请求结构与响应结构，因此这里通过"指标名路径变量 + 查询参数全量透传"的方式统一暴露，
 * 各指标的专有参数（如 macd 的 fast_period/slow_period/signal_period、bbands 的 ma_type/sd、
 * stoch 的 slow_k/slow_d、add 的 series_type_1/series_type_2 等）按文档直接作为查询参数传入即可。
 * <p>当前仅透传返回远端数据，不做数据库保存。指标名以白名单校验，避免无效请求消耗 API credits。
 */
@RestController
@RequestMapping("/test")
public class TechnicalIndicatorController {

        /** 01 重叠研究 Overlap Studies（21 个） */
        private static final Set<String> OVERLAP_STUDIES = Set.of(
                        "bbands", "dema", "ema", "ht_trendline", "ichimoku", "kama", "keltner", "ma", "mama",
                        "mcginley_dynamic", "midpoint", "midprice", "pivot_points_hl", "sar", "sarext", "sma",
                        "t3ma", "tema", "trima", "vwap", "wma");

        /** 02 动量指标 Momentum Indicators（34 个） */
        private static final Set<String> MOMENTUM_INDICATORS = Set.of(
                        "adx", "adxr", "apo", "aroon", "aroonosc", "bop", "cci", "cmo", "coppock", "crsi", "dpo",
                        "dx", "kst", "macd", "macd_slope", "macdext", "mfi", "minus_di", "minus_dm", "mom",
                        "percent_b", "plus_di", "plus_dm", "ppo", "roc", "rocp", "rocr", "rocr100", "rsi",
                        "stoch", "stochf", "stochrsi", "ultosc", "willr");

        /** 03 成交量指标 Volume Indicators（4 个） */
        private static final Set<String> VOLUME_INDICATORS = Set.of(
                        "ad", "adosc", "obv", "rvol");

        /** 04 波动率指标 Volatility Indicators（5 个） */
        private static final Set<String> VOLATILITY_INDICATORS = Set.of(
                        "atr", "natr", "supertrend", "supertrend_heikinashicandles", "trange");

        /** 05 价格变换 Price Transform（18 个） */
        private static final Set<String> PRICE_TRANSFORM = Set.of(
                        "add", "avg", "avgprice", "ceil", "div", "exp", "floor", "heikinashicandles", "hlc3",
                        "ln", "log10", "medprice", "mult", "sqrt", "sub", "sum", "typprice", "wclprice");

        /** 06 周期指标 Cycle Indicators（5 个） */
        private static final Set<String> CYCLE_INDICATORS = Set.of(
                        "ht_dcperiod", "ht_dcphase", "ht_phasor", "ht_sine", "ht_trendmode");

        /** 07 统计函数 Statistic Functions（15 个） */
        private static final Set<String> STATISTIC_FUNCTIONS = Set.of(
                        "beta", "correl", "linearreg", "linearregangle", "linearregintercept", "linearregslope",
                        "max", "maxindex", "min", "minindex", "minmax", "minmaxindex", "stddev", "tsf", "var");

        /** 全部 102 个技术指标白名单（与 doc/quant/technical-indicators/ 目录一一对应） */
        private static final Set<String> SUPPORTED_INDICATORS;

        static {
                java.util.Set<String> all = new java.util.HashSet<>();
                all.addAll(OVERLAP_STUDIES);
                all.addAll(MOMENTUM_INDICATORS);
                all.addAll(VOLUME_INDICATORS);
                all.addAll(VOLATILITY_INDICATORS);
                all.addAll(PRICE_TRANSFORM);
                all.addAll(CYCLE_INDICATORS);
                all.addAll(STATISTIC_FUNCTIONS);
                SUPPORTED_INDICATORS = java.util.Collections.unmodifiableSet(all);
        }

        private final HttpServiceFactory httpServiceFactory;

        /**
         * Twelve Data 技术指标 Base URL（见 application-twelvedata.yml 的 twelvedata.indicator-base-url）
         */
        @Value("${twelvedata.indicator-base-url}")
        private String twelvedataIndicatorBaseUrl;

        /**
         * Twelve Data API key（见 application-twelvedata.yml 的 twelvedata.apikey）
         */
        @Value("${twelvedata.apikey}")
        private String twelvedataApiKey;

        public TechnicalIndicatorController(HttpServiceFactory httpServiceFactory) {
                this.httpServiceFactory = httpServiceFactory;
        }

        /**
         * 透传调用 Twelve Data 任意技术指标接口
         * <p>指标名即 API 路由（见 doc/quant/technical-indicators/ 下的文档文件名），
         * 除 apiKey 外的所有查询参数原样透传给 Twelve Data，专有参数按各指标文档传参。
         *
         * <p>通用参数示例：symbol（标的代码）、interval（时间粒度，必填）、time_period（计算周期）、
         * series_type（价格类型 close/open/high/low/volume）、outputsize（数据点数）、
         * exchange / mic_code / country、start_date / end_date、order、dp、include_ohlc 等。
         *
         * <p>示例：
         * <pre>
         * /test/indicator/ema?symbol=AAPL&interval=1day&timePeriod=21
         * /test/indicator/macd?symbol=AAPL&interval=1day&fast_period=12&slow_period=26&signal_period=9
         * /test/indicator/bbands?symbol=AAPL&interval=1day&ma_type=SMA&sd=2&time_period=20
         * /test/indicator/stoch?symbol=AAPL&interval=1day&slow_k=5&slow_d=3
         * /test/indicator/add?symbol=AAPL&interval=1day&series_type_1=open&series_type_2=close
         * </pre>
         *
         * @param indicator   指标名（即 API 路由），如 sma、rsi、macd、bbands、supertrend
         * @param queryParams 全部查询参数，原样透传给 Twelve Data（apiKey 由服务端覆盖）
         * @return 指标时间序列（values 中字段名随指标不同），出错时返回 code/message/status
         */
        @GetMapping("/indicator/{indicator}")
        public IndicatorResponse indicator(
                        @PathVariable String indicator,
                        @RequestParam Map<String, String> queryParams) {
                // 0. 白名单校验，避免无效请求消耗 API credits
                if (!SUPPORTED_INDICATORS.contains(indicator)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "不支持的技术指标: " + indicator + "，可用指标共 " + SUPPORTED_INDICATORS.size()
                                                        + " 个，见 doc/quant/technical-indicators/ 目录");
                }

                // 1. 查询参数全量透传（GET，无请求体），apiKey 以服务端配置覆盖
                Map<String, Object> params = new LinkedHashMap<>(queryParams);
                params.put("apikey", twelvedataApiKey);

                // 2. Twelve Data 鉴权走 query 参数，无需额外请求头，传空 Map 即可
                Map<String, String> headers = Map.of();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(twelvedataIndicatorBaseUrl + "/" + indicator, params, headers,
                                                IndicatorResponse.class);
        }

        /**
         * 查询当前支持的全部技术指标名单（102 个，按 7 大类返回）
         */
        @GetMapping("/indicators")
        public Map<String, Object> indicators() {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("total", SUPPORTED_INDICATORS.size());
                result.put("01-overlap-studies 重叠研究", OVERLAP_STUDIES);
                result.put("02-momentum-indicators 动量指标", MOMENTUM_INDICATORS);
                result.put("03-volume-indicators 成交量指标", VOLUME_INDICATORS);
                result.put("04-volatility-indicators 波动率指标", VOLATILITY_INDICATORS);
                result.put("05-price-transform 价格变换", PRICE_TRANSFORM);
                result.put("06-cycle-indicators 周期指标", CYCLE_INDICATORS);
                result.put("07-statistic-functions 统计函数", STATISTIC_FUNCTIONS);
                return result;
        }
}