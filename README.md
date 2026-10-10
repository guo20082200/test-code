# test-code 工程说明文档

## 1. 工程简介

test-code 是一个基于 **Spring Boot 4.0.8 + Java 17 + Maven** 的后端测试工程，核心目标是对 **HTTP 客户端调用能力** 进行统一封装与验证：

- 提供一套通用的 `HttpService` 接口，支持 GET / POST / PUT / DELETE 四种请求方式；
- 提供三种 HTTP 客户端实现（JDK HttpClient / RestTemplate / RestClient），可通过配置随时切换；
- 提供统一的请求拦截器机制（如链路追踪 ID 透传）、SSL 证书策略与超时分级配置；
- 附带两个真实的业务联调示例：调用「C 大脑智能助手」和「任务中心新增任务」接口。

## 2. 技术栈

| 分类 | 技术 | 说明 |
| --- | --- | --- |
| 基础框架 | Spring Boot 4.0.8 | 父 POM：`spring-boot-starter-parent` |
| JDK 版本 | Java 17 | `maven-compiler-plugin` 编译 |
| Web | spring-boot-starter-webmvc | 提供 RestController 能力 |
| HTTP 客户端 | spring-boot-starter-webclient | 提供 RestClient / WebClient 相关支持 |
| JSON | Jackson 3（`tools.jackson.*`） | 请求/响应体序列化与反序列化 |
| 简化工具 | Lombok | `@Slf4j`、`@Builder` 等，编译期注解处理 |
| 构建工具 | Maven（含 `mvnw` Wrapper） | 无需本地安装 Maven 即可构建 |

## 3. 工程结构

```
test-code
├── pom.xml                          # Maven 构建配置
├── mvnw / mvnw.cmd                  # Maven Wrapper 脚本
├── HELP.md                          # Spring Initializr 生成的参考链接
├── doc/                             # 联调辅助资料
│   ├── HelloController.http         # IDEA HTTP Client 请求示例
│   ├── 任务中心新增请求体.json        # 任务中心接口请求体样例
│   └── 任务中心新增响应体.json        # 任务中心接口响应体样例
├── src/main/java/com/goaerion/testcode
│   ├── TestCodeApplication.java     # Spring Boot 启动类
│   └── http
│       ├── config                   # HTTP 客户端基础设施
│       │   ├── HttpClientProvider.java     # 共享 JDK HttpClient 单例（SSL/超时配置）
│       │   ├── HttpClientInterceptor.java  # 全局拦截器 SPI（函数式接口）
│       │   └── TraceIdInterceptor.java     # X-Trace-Id 链路追踪拦截器实现
│       ├── controller
│       │   └── HelloController.java        # 测试入口（/test/hello、/test/centerTask）
│       ├── dto
│       │   ├── assistant            # 智能助手接口相关 DTO
│       │   └── centertask           # 任务中心接口相关 DTO
│       └── service
│           ├── HttpService.java            # 通用 HTTP 服务接口（含 buildUrl 默认方法）
│           ├── HttpServiceFactory.java     # 工厂 + 策略：按配置选择实现
│           ├── CenterTaskService.java      # 任务中心请求体构建接口
│           └── impl
│               ├── JdkHttpServiceImpl.java          # JDK HttpClient 实现
│               ├── RestTemplateHttpServiceImpl.java # RestTemplate 实现
│               ├── RestClientHttpServiceImpl.java   # RestClient 实现（默认）
│               └── CenterTaskServiceImpl.java       # 任务请求体/按钮结构组装
└── src/test/java
    └── TestCodeApplicationTests.java       # 默认上下文加载测试
```

## 4. 核心设计

### 4.1 HttpService 抽象（策略模式）

[HttpService.java](src/main/java/com/goaerion/testcode/http/service/HttpService.java) 定义了统一的 HTTP 发送契约：

- `<T> T get(url, params, headers, responseType)`：参数经 URL 编码后拼接到 query string；
- `<T> T post / put(url, body, headers, responseType)`：请求体统一序列化为 JSON；
- `<T> T delete(url, headers, responseType)`；
- `buildUrl(...)`：default 方法，统一实现 GET 参数拼接与 URL 编码复用。

http / https 均支持，由 URL 中的 scheme 自动识别，业务代码无需针对协议特殊处理。

### 4.2 三种实现与工厂切换

三种实现均注册为 Spring Bean，底层共享同一份 `HttpClientProvider` 配置：

| 实现类 | Bean 名称 | 底层 |
| --- | --- | --- |
| [JdkHttpServiceImpl](src/main/java/com/goaerion/testcode/http/service/impl/JdkHttpServiceImpl.java) | `jdkHttpServiceImpl` | 原生 `java.net.http.HttpClient` |
| [RestTemplateHttpServiceImpl](src/main/java/com/goaerion/testcode/http/service/impl/RestTemplateHttpServiceImpl.java) | `restTemplateHttpServiceImpl` | RestTemplate + JdkClientHttpRequestFactory |
| [RestClientHttpServiceImpl](src/main/java/com/goaerion/testcode/http/service/impl/RestClientHttpServiceImpl.java) | `restClientHttpServiceImpl` | RestClient + JdkClientHttpRequestFactory（**默认**） |

[HttpServiceFactory](src/main/java/com/goaerion/testcode/http/service/HttpServiceFactory.java) 利用 Spring 注入 `Map<String, HttpService>`（key 为 bean 名称），按 `http.service.impl` 配置选择实现，未配置时默认使用 `restClientHttpServiceImpl`：

```properties
http.service.impl=jdkHttpServiceImpl
```

### 4.3 统一配置（HttpClientProvider）

[HttpClientProvider](src/main/java/com/goaerion/testcode/http/config/HttpClientProvider.java) 是共享的 JDK HttpClient 单例（线程安全），统一管理：

- **SSL 策略**：`http.ssl.trust-all=true` 时跳过 https 证书校验（仅限内网/测试环境）；
- **连接超时**：`http.timeout.connect`（默认 10s），JDK HttpClient 全局生效；
- **读超时**：`http.timeout.read`（默认 30s），供各实现按请求应用。

### 4.4 全局拦截器机制

[HttpClientInterceptor](src/main/java/com/goaerion/testcode/http/config/HttpClientInterceptor.java) 是函数式 SPI，在请求发送前回调，可读取 method / url 并向请求头追加内容。实现接口并声明为 Spring Bean 即自动被三个实现类应用。

内置实现 [TraceIdInterceptor](src/main/java/com/goaerion/testcode/http/config/TraceIdInterceptor.java)：为每个请求追加 `X-Trace-Id` 请求头（已存在时沿用上游值），便于跨服务串联日志。

### 4.5 日志与安全

- 三个实现类均输出请求/响应日志，日志前缀分别为 `[JDK-HttpClient]` / `[RestTemplate]` / `[RestClient]`；
- 日志中的 `Authorization` 请求头自动脱敏为 `***`；
- 发送失败统一抛出 `IllegalStateException`，`InterruptedException` 场景会恢复线程中断标记。

## 5. 接口说明

启动应用后（默认端口 8080），提供两个 GET 测试接口，均要求调用方携带以下请求头：

| 请求头 | 说明 |
| --- | --- |
| `currentuserid` | 当前用户工号 |
| `environment` | 环境标识 |
| `Authorization` | 鉴权令牌（Bearer Token） |

### 5.1 GET /test/hello

透传调用智能助手接口（`assistant.send-url`）：固定发送一条「今天上海的天气如何」的联网测试请求（advanced-mode-agent 模式、指定专家本体），返回 `AssistantResponse`。

### 5.2 GET /test/centerTask

透传调用任务中心新增任务接口（`center-task.send-url`）：由 `CenterTaskService` 组装任务请求体（含标题、标记、标签、可见字段、PC/移动端/内网共用的按钮配置等），返回 `CenterTaskResponse`。

请求示例见 [doc/HelloController.http](doc/HelloController.http)。

## 6. 配置说明

配置文件：[application.properties](src/main/resources/application.properties)

```properties
# 智能助手接口地址
assistant.send-url=http://192.168.6.114/cbrain-gateway/comac-ai/comacai/assistant/sendToAssistant

# 任务中心新增任务接口地址
center-task.send-url=http://192.168.6.20/cbrain-gateway/cbrain-task-server/cbrain-task/task/save

# HttpService 实现选择（jdkHttpServiceImpl / restTemplateHttpServiceImpl / restClientHttpServiceImpl）
#http.service.impl=restClientHttpServiceImpl

# https 自签名证书时置为 true 跳过证书校验（仅限内网/测试环境，正式环境必须为 false）
http.ssl.trust-all=false

# HttpClient 超时分级配置
http.timeout.connect=10s
http.timeout.read=30s
```

## 7. 构建与运行

```bash
# 编译打包（跳过测试可加 -DskipTests）
./mvnw clean package

# 运行
./mvnw spring-boot:run

# 运行测试
./mvnw test
```

Windows 环境使用 `mvnw.cmd` 代替 `./mvnw`。启动后访问 `http://localhost:8080/test/hello` 或 `/test/centerTask` 进行验证（需携带第 5 节所述请求头）。
