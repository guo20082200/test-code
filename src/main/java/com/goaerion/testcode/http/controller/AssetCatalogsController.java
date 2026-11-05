package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.dto.assetcatalogs.AssetCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.BondsCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CommoditiesCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CountriesResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CrossListingsResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CryptocurrencyExchangesResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.CryptocurrenciesCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.EarliestTimestampResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.ExchangeScheduleResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.ExchangesResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.ForexPairsCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.FundCatalogResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.InstrumentTypeResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.MarketState;
import com.goaerion.testcode.http.dto.assetcatalogs.SymbolSearchResponse;
import com.goaerion.testcode.http.dto.assetcatalogs.TechnicalIndicatorsResponse;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Twelve Data 资产目录透传接口（对应 doc/asset_catalogs 下的接口文档 01~17）
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class AssetCatalogsController {

        private final HttpServiceFactory httpServiceFactory;

        /** Twelve Data API key（见 application.properties 的 twelvedata.apikey） */
        @Value("${twelvedata.apikey}")
        private String twelvedataApiKey;

        @Value("${twelvedata.stocks-send-url}")
        private String stocksSendUrl;

        @Value("${twelvedata.forex-pairs-send-url}")
        private String forexPairsSendUrl;

        @Value("${twelvedata.cryptocurrencies-send-url}")
        private String cryptocurrenciesSendUrl;

        @Value("${twelvedata.etfs-send-url}")
        private String etfsSendUrl;

        @Value("${twelvedata.funds-send-url}")
        private String fundsSendUrl;

        @Value("${twelvedata.commodities-send-url}")
        private String commoditiesSendUrl;

        @Value("${twelvedata.bonds-send-url}")
        private String bondsSendUrl;

        @Value("${twelvedata.symbol-search-send-url}")
        private String symbolSearchSendUrl;

        @Value("${twelvedata.cross-listings-send-url}")
        private String crossListingsSendUrl;

        @Value("${twelvedata.earliest-timestamp-send-url}")
        private String earliestTimestampSendUrl;

        @Value("${twelvedata.exchanges-send-url}")
        private String exchangesSendUrl;

        @Value("${twelvedata.exchange-schedule-send-url}")
        private String exchangeScheduleSendUrl;

        @Value("${twelvedata.cryptocurrency-exchanges-send-url}")
        private String cryptocurrencyExchangesSendUrl;

        @Value("${twelvedata.market-state-send-url}")
        private String marketStateSendUrl;

        @Value("${twelvedata.countries-send-url}")
        private String countriesSendUrl;

        @Value("${twelvedata.instrument-type-send-url}")
        private String instrumentTypeSendUrl;

        @Value("${twelvedata.technical-indicators-send-url}")
        private String technicalIndicatorsSendUrl;

        public AssetCatalogsController(HttpServiceFactory httpServiceFactory) {
                this.httpServiceFactory = httpServiceFactory;
        }

        /**
         * 透传调用股票目录接口，获取全部可用股票代码列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/01-股票目录-stocks.md
         */
        @GetMapping("/stocks")
        public AssetCatalogResponse stocks(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String country,
                        @RequestParam(required = false) String type,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数（GET，无请求体，null 值参数拼接 URL 时会被忽略）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("exchange", exchange);
                params.put("country", country);
                params.put("type", type);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. Twelve Data 鉴权走 query 参数，无需额外请求头，传空 Map 即可
                Map<String, String> headers = Map.of();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(stocksSendUrl, params, headers, AssetCatalogResponse.class);
        }

        /**
         * 透传调用外汇交易对目录接口，获取全部可用货币对列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/02-外汇交易对-forex_pairs.md
         */
        @GetMapping("/forexPairs")
        public ForexPairsCatalogResponse forexPairs(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String currencyBase,
                        @RequestParam(required = false) String currencyQuote,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("currency_base", currencyBase);
                params.put("currency_quote", currencyQuote);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(forexPairsSendUrl, params, headers, ForexPairsCatalogResponse.class);
        }

        /**
         * 透传调用加密货币交易对目录接口，获取全部可用交易对列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/03-加密货币交易对-cryptocurrencies.md
         */
        @GetMapping("/cryptocurrencies")
        public CryptocurrenciesCatalogResponse cryptocurrencies(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String currencyBase,
                        @RequestParam(required = false) String currencyQuote,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("exchange", exchange);
                params.put("currency_base", currencyBase);
                params.put("currency_quote", currencyQuote);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(cryptocurrenciesSendUrl, params, headers, CryptocurrenciesCatalogResponse.class);
        }

        /**
         * 透传调用 ETF 目录接口，获取全部可用 ETF 列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/04-ETF目录-etfs.md
         */
        @GetMapping("/etfs")
        public AssetCatalogResponse etfs(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String country,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("exchange", exchange);
                params.put("country", country);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果（etfs 与 stocks 响应结构相同，共用 DTO）
                return httpServiceFactory.getHttpService()
                                .get(etfsSendUrl, params, headers, AssetCatalogResponse.class);
        }

        /**
         * 透传调用基金目录接口，获取全部可用基金列表（每日更新）
         * <p>注意：响应数据包裹在 result 对象中（result.count + result.list）
         * <p>接口文档见 doc/asset_catalogs/05-基金目录-funds.md
         */
        @GetMapping("/funds")
        public FundCatalogResponse funds(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String country,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("exchange", exchange);
                params.put("country", country);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(fundsSendUrl, params, headers, FundCatalogResponse.class);
        }

        /**
         * 透传调用大宗商品目录接口，获取全部可用商品交易对列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/06-大宗商品目录-commodities.md
         */
        @GetMapping("/commodities")
        public CommoditiesCatalogResponse commodities(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("category", category);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(commoditiesSendUrl, params, headers, CommoditiesCatalogResponse.class);
        }

        /**
         * 透传调用债券目录接口，获取全部可用债券列表（每日更新）
         * <p>注意：响应数据包裹在 result 对象中（result.count + result.list）
         * <p>接口文档见 doc/asset_catalogs/07-债券目录-bonds.md
         */
        @GetMapping("/bonds")
        public BondsCatalogResponse bonds(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String country,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer outputsize) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("exchange", exchange);
                params.put("country", country);
                params.put("page", page);
                params.put("outputsize", outputsize);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(bondsSendUrl, params, headers, BondsCatalogResponse.class);
        }

        /**
         * 透传调用代码搜索接口，按名称或代码模糊搜索金融工具（结果按相关度排序）
         * <p>接口文档见 doc/asset_catalogs/08-代码搜索-symbol_search.md
         */
        @GetMapping("/symbolSearch")
        public SymbolSearchResponse symbolSearch(
                        @RequestParam String symbol,
                        @RequestParam(required = false) Integer outputsize,
                        @RequestParam(required = false) Boolean showPlan) {
                // 1. 写发送http请求的查询参数（symbol 必填）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("outputsize", outputsize);
                params.put("show_plan", showPlan);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(symbolSearchSendUrl, params, headers, SymbolSearchResponse.class);
        }

        /**
         * 透传调用交叉上市接口，获取指定证券在多个交易所的上市代码列表（每日更新）
         * <p>注意：需 Grow/Venture 及以上套餐（40 积分/次），免费套餐返回错误；
         * 响应数据包裹在 result 对象中（result.count + result.list）
         * <p>接口文档见 doc/asset_catalogs/09-交叉上市-cross_listings.md
         */
        @GetMapping("/crossListings")
        public CrossListingsResponse crossListings(
                        @RequestParam String symbol,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String micCode,
                        @RequestParam(required = false) String country) {
                // 1. 写发送http请求的查询参数（symbol 必填，exchange/mic_code/country 用于消歧）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("exchange", exchange);
                params.put("mic_code", micCode);
                params.put("country", country);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(crossListingsSendUrl, params, headers, CrossListingsResponse.class);
        }

        /**
         * 透传调用最早时间戳接口，获取指定标在指定间隔下最早可用的行情数据时间
         * <p>注意：symbol/figi/isin/cusip 四选一（至少一个）；timezone 仅对日内间隔生效
         * <p>接口文档见 doc/asset_catalogs/10-最早时间戳-earliest_timestamp.md
         */
        @GetMapping("/earliestTimestamp")
        public EarliestTimestampResponse earliestTimestamp(
                        @RequestParam(required = false) String symbol,
                        @RequestParam(required = false) String figi,
                        @RequestParam(required = false) String isin,
                        @RequestParam(required = false) String cusip,
                        @RequestParam String interval,
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String micCode,
                        @RequestParam(required = false) String timezone) {
                // 1. 写发送http请求的查询参数（interval 必填）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("symbol", symbol);
                params.put("figi", figi);
                params.put("isin", isin);
                params.put("cusip", cusip);
                params.put("interval", interval);
                params.put("exchange", exchange);
                params.put("mic_code", micCode);
                params.put("timezone", timezone);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(earliestTimestampSendUrl, params, headers, EarliestTimestampResponse.class);
        }

        /**
         * 透传调用交易所目录接口，获取全部可用股票交易所列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/11-交易所目录-exchanges.md
         */
        @GetMapping("/exchanges")
        public ExchangesResponse exchanges(
                        @RequestParam(required = false) String type,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String code,
                        @RequestParam(required = false) String country,
                        @RequestParam(required = false) Boolean showPlan) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("type", type);
                params.put("name", name);
                params.put("code", code);
                params.put("country", country);
                params.put("show_plan", showPlan);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(exchangesSendUrl, params, headers, ExchangesResponse.class);
        }

        /**
         * 透传调用交易所交易时间表接口，获取各交易所的交易时段与营业日信息
         * <p>注意：需 Ultra/Enterprise 及以上套餐（100 积分/次），免费套餐返回错误
         * <p>接口文档见 doc/asset_catalogs/12-交易所交易时间表-exchange_schedule.md
         */
        @GetMapping("/exchangeSchedule")
        public ExchangeScheduleResponse exchangeSchedule(
                        @RequestParam(required = false) String date,
                        @RequestParam(required = false) String micName,
                        @RequestParam(required = false) String micCode,
                        @RequestParam(required = false) String country) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("date", date);
                params.put("mic_name", micName);
                params.put("mic_code", micCode);
                params.put("country", country);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(exchangeScheduleSendUrl, params, headers, ExchangeScheduleResponse.class);
        }

        /**
         * 透传调用加密货币交易所目录接口，获取全部可用加密货币交易所列表（每日更新）
         * <p>接口文档见 doc/asset_catalogs/13-加密货币交易所-cryptocurrency_exchanges.md
         */
        @GetMapping("/cryptocurrencyExchanges")
        public CryptocurrencyExchangesResponse cryptocurrencyExchanges() {
                // 1. 写发送http请求的查询参数（该接口无业务参数，仅传鉴权）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(cryptocurrencyExchangesSendUrl, params, headers, CryptocurrencyExchangesResponse.class);
        }

        /**
         * 透传调用市场状态接口，获取全部股票交易所的实时开闭市状态
         * <p>注意：响应为顶层数组（无 data/status 包装层），故使用 MarketState[].class 反序列化
         * <p>接口文档见 doc/asset_catalogs/14-市场状态-market_state.md
         */
        @GetMapping("/marketState")
        public List<MarketState> marketState(
                        @RequestParam(required = false) String exchange,
                        @RequestParam(required = false) String code,
                        @RequestParam(required = false) String country) {
                // 1. 写发送http请求的查询参数
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("exchange", exchange);
                params.put("code", code);
                params.put("country", country);
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果（顶层数组响应，用数组类型反序列化后转 List）
                MarketState[] result = httpServiceFactory.getHttpService()
                                .get(marketStateSendUrl, params, headers, MarketState[].class);
                return Arrays.asList(result);
        }

        /**
         * 透传调用国家目录接口，获取包含 ISO 代码、首都、货币的完整国家列表
         * <p>接口文档见 doc/asset_catalogs/15-国家目录-countries.md
         */
        @GetMapping("/countries")
        public CountriesResponse countries() {
                // 1. 写发送http请求的查询参数（该接口无业务参数，仅传鉴权）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(countriesSendUrl, params, headers, CountriesResponse.class);
        }

        /**
         * 透传调用工具类型目录接口，获取全部金融工具类型列表
         * <p>注意：result 字段直接是字符串数组
         * <p>接口文档见 doc/asset_catalogs/16-工具类型目录-instrument_type.md
         */
        @GetMapping("/instrumentType")
        public InstrumentTypeResponse instrumentType() {
                // 1. 写发送http请求的查询参数（该接口无业务参数，仅传鉴权）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(instrumentTypeSendUrl, params, headers, InstrumentTypeResponse.class);
        }

        /**
         * 透传调用技术指标目录接口，获取全部技术指标的元信息字典
         * <p>注意：data 是以指标代码为键的 Map
         * <p>接口文档见 doc/asset_catalogs/17-技术指标目录-technical_indicators.md
         */
        @GetMapping("/technicalIndicators")
        public TechnicalIndicatorsResponse technicalIndicators() {
                // 1. 写发送http请求的查询参数（该接口无业务参数，仅传鉴权）
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("apikey", twelvedataApiKey);

                // 2. 无需额外请求头
                Map<String, String> headers = Map.of();

                // 3. 发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .get(technicalIndicatorsSendUrl, params, headers, TechnicalIndicatorsResponse.class);
        }
}
