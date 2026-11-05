package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.dto.twelvedata.PriceResponse;
import com.goaerion.testcode.http.dto.twelvedata.QuoteResponse;
import com.goaerion.testcode.http.dto.twelvedata.TimeSeriesCrossResponse;
import com.goaerion.testcode.http.dto.twelvedata.TimeSeriesResponse;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
public class MarketDataController {

        private final HttpServiceFactory httpServiceFactory;

        /**
         * Twelve Data 时间序列接口地址（见 application.properties 的 twelvedata.send-url）
         */
        @Value("${twelvedata.send-url}")
        private String twelvedataSendUrl;

        /**
         * Twelve Data 交叉汇率时间序列接口地址（见 application.properties 的 twelvedata.cross-send-url）
         */
        @Value("${twelvedata.cross-send-url}")
        private String twelvedataCrossSendUrl;

        /**
         * Twelve Data 实时报价接口地址（见 application.properties 的 twelvedata.quote-send-url）
         */
        @Value("${twelvedata.quote-send-url}")
        private String twelvedataQuoteSendUrl;

        /**
         * Twelve Data 最新价接口地址（见 application.properties 的 twelvedata.price-send-url）
         */
        @Value("${twelvedata.price-send-url}")
        private String twelvedataPriceSendUrl;

        /**
         * Twelve Data API key（见 application.properties 的 twelvedata.apikey）
         */
        @Value("${twelvedata.apikey}")
        private String twelvedataApiKey;

        public MarketDataController(HttpServiceFactory httpServiceFactory) {
                this.httpServiceFactory = httpServiceFactory;
        }


        /**
         * 透传调用 Twelve Data 时间序列接口，获取标的 OHLC 历史行情
         * <p>接口文档见 doc/TwelveData-time_series接口文档.md
         *
         * @param symbol     标的代码，如 AAPL、EUR/USD、ETH/BTC
         * @param interval   时间间隔：1min/5min/15min/30min/45min/1h/2h/4h/8h/1day/1week/1month
         * @param outputsize 数据点数量（1~5000，默认 30），不传则使用 Twelve Data 默认值
         */
        @GetMapping("/timeSeries")
        public TimeSeriesResponse timeSeries(
                        @RequestParam(defaultValue = "AAPL") String symbol,
                        @RequestParam(defaultValue = "1min") String interval,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数（GET，无请求体）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("interval", interval);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. Twelve Data 鉴权走 query 参数，无需额外请求头，传空 Map 即可
                Map<String, String> headers = Map.of();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(twelvedataSendUrl, params, headers, TimeSeriesResponse.class);
        }

        /**
         * 透传调用 Twelve Data 交叉汇率时间序列接口，获取 base/quote 组合的 OHLC 历史行情
         * <p>接口文档见 doc/TwelveData-time_series_cross接口文档.md
         *
         * @param base       基础货币（Base）代码，如 JPY
         * @param quote      报价货币（Quote）代码，如 BTC
         * @param interval   时间间隔：1min/5min/15min/30min/45min/1h/2h/4h/8h/1day/1week/1month
         * @param outputsize 数据点数量（1~5000，默认 30），不传则使用 Twelve Data 默认值
         */
        @GetMapping("/timeSeriesCross")
        public TimeSeriesCrossResponse timeSeriesCross(
                        @RequestParam String base,
                        @RequestParam String quote,
                        @RequestParam(defaultValue = "1min") String interval,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数（GET，无请求体）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("base", base);
                params.put("quote", quote);
                params.put("interval", interval);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. Twelve Data 鉴权走 query 参数，无需额外请求头，传空 Map 即可
                Map<String, String> headers = Map.of();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(twelvedataCrossSendUrl, params, headers, TimeSeriesCrossResponse.class);
        }

        /**
         * 透传调用 Twelve Data 实时报价接口，获取标的的最新行情快照（最新价、涨跌幅、52 周指标等）
         * <p>接口文档见 doc/TwelveData-quote接口文档.md
         *
         * @param symbol   标的代码，如 AAPL、EUR/USD、ETH/BTC
         * @param interval 报价对应的 K 线周期：1min/5min/15min/30min/45min/1h/2h/4h/8h/1day/1week/1month
         * @param eod      为 true 时返回已收盘交易日的数据
         */
        @GetMapping("/quote")
        public QuoteResponse quote(
                        @RequestParam(defaultValue = "AAPL") String symbol,
                        @RequestParam(defaultValue = "1day") String interval,
                        @RequestParam(required = false) Boolean eod) {
                // 1. 写发送http请求的查询参数（GET，无请求体）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("interval", interval);
                params.put("eod", eod);
                params.put("apikey", twelvedataApiKey);

                // 2. Twelve Data 鉴权走 query 参数，无需额外请求头，传空 Map 即可
                Map<String, String> headers = Map.of();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(twelvedataQuoteSendUrl, params, headers, QuoteResponse.class);
        }

        /**
         * 透传调用 Twelve Data 最新价接口，获取标的的最新成交价（轻量接口，仅返回一个价格字段）
         * <p>接口文档见 doc/TwelveData-price接口文档.md
         *
         * @param symbol 标的代码，如 AAPL、EUR/USD、ETH/BTC
         * @param dp     小数位数（0~11），不传则由 Twelve Data 自动确定
         */
        @GetMapping("/price")
        public PriceResponse price(
                        @RequestParam(defaultValue = "AAPL") String symbol,
                        @RequestParam(required = false) Integer dp) {
                // 1. 写发送http请求的查询参数（GET，无请求体）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("dp", dp);
                params.put("apikey", twelvedataApiKey);

                // 2. Twelve Data 鉴权走 query 参数，无需额外请求头，传空 Map 即可
                Map<String, String> headers = Map.of();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(twelvedataPriceSendUrl, params, headers, PriceResponse.class);
        }
}
