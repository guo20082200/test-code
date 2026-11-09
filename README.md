# test-code

一个基于 **Spring Boot 4.1.1 / Java 17** 的实验性工程，主要包含三部分内容：

1. **HTTP 客户端模块**：以"工厂 + 策略"模式封装 JDK HttpClient、RestTemplate、RestClient 三种实现，支持 TraceId 链路追踪、分级超时与 SSL 配置；
2. **TwelveData 行情数据对接**：封装时间序列、实时报价、资产目录等 20+ REST 接口，并通过 MyBatis-Plus + PostgreSQL 落库；
3. **Apache Commons Numbers 实验**：针对高精度算术、角度、精度等能力的单元测试验证。

## 技术栈

| 类别 | 选型 | 说明 |
| --- | --- | --- |
| 语言 / 运行时 | Java 17 | |
| 核心框架 | Spring Boot 4.1.1 | spring-boot-starter-webmvc / webclient |
| ORM | MyBatis-Plus 3.5.16 | 使用 `mybatis-plus-spring-boot4-starter` |
| 数据库 | PostgreSQL | 驱动随 Boot 版本管理 |
| 数学计算 | Apache Commons Numbers 1.3 | core / angle / arrays / complex / fraction 等 11 个模块 |
| 工具 | Lombok | |
| 测试 | JUnit 5 + Mockito（subclass mock maker） | Surefire 已配置 `-Djdk.attach.allowAttachSelf=true` |

## 项目结构

```text
src/main/java/com/goaerion/testcode
└── http
    ├── config          # HttpClient 装配、TraceId / 拦截器配置
    │   ├── HttpClientInterceptor.java
    │   ├── HttpClientProvider.java
    │   └── TraceIdInterceptor.java
    ├── controller      # HelloController / MarketDataController / AssetCatalogsController
    ├── dto
    │   ├── twelvedata      # Price / Quote / TimeSeries / TimeSeriesCross 响应
    │   ├── assetcatalogs   # 股票、外汇、加密货币、ETF、基金、商品、债券等目录
    │   ├── assistant       # 助手相关 DTO
    │   └── centertask      # 中心任务相关 DTO
    └── service
        ├── HttpService.java            # 统一 HTTP 出口接口
        ├── HttpServiceFactory.java     # 工厂 + 策略：按 bean 名称选择实现
        └── impl
            ├── JdkHttpServiceImpl.java
            ├── RestTemplateHttpServiceImpl.java
            └── RestClientHttpServiceImpl.java
```

```text
src/test/java
├── com/goaerion/numberscore   # Commons Numbers：算术、精度、范数等测试
├── com/goaerion/testcode/http # 控制器集成测试（目录 / 报价 / 时间序列）
└── DfeTest.java
```

## 核心设计

### HTTP 客户端工厂（工厂 + 策略）

- 所有 `HttpService` 实现由 Spring 注入为 `Map<String, HttpService>`（key = bean 名称）；
- 通过配置项 `http.service.impl` 指定默认实现，可选值：
  - `jdkHttpServiceImpl`
  - `restTemplateHttpServiceImpl`
  - `restClientHttpServiceImpl`（**默认**）
- 统一支持连接/读取分级超时（`http.timeout.connect` / `http.timeout.read`，支持 `10s`、`500ms` 格式）；
- `http.ssl.trust-all=true` 可跳过自签名证书校验（仅限内网/测试环境）。

### TwelveData 行情对接

- 接口地址集中配置在 `application-twelvedata.yml`，由 `application.yml` 通过 `spring.config.import` 引入；
- 覆盖：`time_series`、`time_series/cross`、`quote`、`price`，以及股票/外汇/加密货币/ETF/基金/商品/债券等资产目录与交易所、市场状态、技术指标等辅助接口；
- API Key 通过环境变量 `TWELVEDATA_APIKEY` 注入（**请勿将真实 Key 提交到仓库**）。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+（或直接使用自带 Wrapper `./mvnw`）
- PostgreSQL（可选，行情落库时需要）

### 配置

通过环境变量注入敏感信息：

```bash
export PG_HOST=localhost
export PG_PORT=5432
export PG_DB=quant
export PG_USER=postgres
export PG_PASSWORD=postgres
export TWELVEDATA_APIKEY=your_api_key
```

### 构建 & 运行

```bash
./mvnw clean package
./mvnw spring-boot:run
```

### 测试

```bash
./mvnw test
```

> Mockito 使用 subclass mock maker（见 `src/test/resources/mockito-extensions`），
> Surefire 已通过 `-Djdk.attach.allowAttachSelf=true` 允许 agent 自附加，兼容 JDK 21+。

## 配置文件说明

| 文件 | 作用 |
| --- | --- |
| `application.yml` | 主配置：数据源、MyBatis-Plus、HTTP 工厂与超时/SSL |
| `application-twelvedata.yml` | TwelveData 全部接口地址与 API Key |
| `application-other.yml` | 其他扩展场景配置 |

## doc 目录

| 子目录 | 内容 |
| --- | --- |
| `doc/quant/` | TwelveData 全量接口文档、资产行情获取与存储技术方案、技术指标说明（趋势/动量/量能/波动率/价格变换） |
| `doc/database/` | MySQL / PostgreSQL 核心知识、事务与锁、高可用与优化、两者对比 |
| `doc/interview/` | 系统设计、分布式高可用、中间件、Java 并发、Spring Cloud、Dubbo、限流熔断等面试题及参考答案 |
| `doc/data-interview/` | 大数据体系（Hadoop / Hive / Spark / Flink / Kafka / 数据湖）面试知识 |