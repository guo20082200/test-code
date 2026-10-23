# Spring Cloud 参考答案

> 以下为「要点框架 + 关键细节 + 权衡取舍」，面试需结合个人项目组件组合展开。原题见 ../08-SpringCloud.md

---

## 一、体系认知与版本演进（开场必问）

### 1. Netflix 停更后的补位与组件选型全景图

> 原题要点：Netflix 大面积停更后，官方与 Alibaba 各自怎么补位？画出项目组件全景图并说明理由。

- **停更背景**：Eureka 2.x 停止开发、Hystrix 进入维护模式、Ribbon/Zuul 冻结、Zuul 2 因内部原因迟迟不开源，Netflix 宣布将组件转入维护态并转向内部方案（Concurrency Limits 等）。
- **Spring Cloud 官方补位**：
  - 注册/发现抽象：`Spring Cloud Commons`（`@LoadBalanced`、DiscoveryClient 接口）+ 接第三方实现；
  - 熔断抽象：`Spring Cloud CircuitBreaker`（Resilience4j/Sentinel 适配）；
  - 网关：`Spring Cloud Gateway`（WebFlux 替代 Zuul 1 的同步阻塞 servlet 模型）；
  - 负载均衡：`Spring Cloud LoadBalancer` 替代 Ribbon（懒加载、响应式、无静态全量列表）；
  - 配置：`Spring Cloud Config` + Bus（实践中常被 Nacos/Apollo 直接替代）。
- **Spring Cloud Alibaba 补位**：Nacos（注册+配置二合一）、Sentinel（限流熔断，自带 Dashboard 与规则持久化）、Seata（分布式事务）、RocketMQ、Dubbo（作为 RPC 接入）。
- **参考全景图（示例口径，面试换成本项目）**：
  - 接入层：Nginx/K8s Ingress → **Spring Cloud Gateway**；
  - 注册+配置：**Nacos 2.x**（一个组件同时解决两件事，运维减半）；
  - RPC：**OpenFeign + LoadBalancer**（团队 REST 习惯）或 Dubbo（内网高频低延迟场景）；
  - 熔断限流：**Sentinel**（规则推 Nacos 持久化）；
  - 链路：**OpenTelemetry + SkyWalking**；日志：ELK/Loki；监控：Prometheus + Grafana；
  - 消息：RocketMQ/Kafka；任务：XXL-Job。
- **选型理由话术**：不全家桶照搬，按「团队规模、运维能力、SLA 要求」取舍——小团队用 Nacos 一站式；对生态兼容性敏感的用官方抽象（CircuitBreaker/DiscoveryClient）打底，便于后续换实现。

### 2. 版本兼容关系与 Spring Boot 2 → 3 升级踩坑

> 原题要点：SC 与 SCA 版本兼容关系怎么维护？Boot 2→3 升级踩过哪些坑？

- **兼容关系本质**：Spring Cloud 版本 train（Hoxton/2021.x/2022.x/2023.x）锁定 **Spring Boot minor 版本区间**；Spring Cloud Alibaba 通过**独立版本号映射表**（如 SCA 2021.0.x ↔ SC 2021.0.x ↔ Boot 2.6.x）对齐。维护方式：看官方 wiki 版本说明，**以「先定 Boot，再定 SC，最后定 SCA」的顺序锁版本**，全公司 parent POM/BOM 统一管理，禁止各服务自行引版本。
- **Boot 2 → 3 主要坑**：
  - **javax → jakarta**：所有 `javax.*`（servlet、validation、persistence）改 `jakarta.*`；依赖老组件（如老版本 Swagger/Springfox、某些 SDK）直接不可用，需换 springdoc、等新版本；
  - **JDK 基线**：Boot 3 要求 Java 17，语法/字节码、垃圾收集器默认值变化；
  - **组件废弃与替换**：Ribbon → LoadBalancer、Hystrix → CircuitBreaker/Sentinel（官方 starter 对 Boot 3 无适配）、Sleuth → Micrometer Tracing + Bridge（OTel 语义）；
  - **Spring Security 5→6**：`WebSecurityConfigurerAdapter` 移除、Lambda DSL 强制；
  - **SCA 对应版本**：必须升到 2022.x/2023.x 系列，老 Nacos client（1.x）与 Boot 3 的 grpc/protobuf 依赖冲突；
  - **第三方 native 依赖**：JNI、老 cglib/asm 反射在新 JDK 下告警或失败。
- **升级策略**：先建「版本对齐表 + 升级基线分支」，按「公共组件先升 → 抽 1 个非核心服务试点 → 全量滚动」，升级期 Boot 2/3 服务共存时注意注册中心与序列化协议互通。

### 3. Spring Cloud（REST 生态）vs Dubbo（RPC 二进制）路线选择

> 原题要点：两者体系差异、场景选择、SCA 里 Dubbo 与 OpenFeign 混用经历。

- **体系差异**：
  - Spring Cloud：HTTP/REST + JSON，生态全（网关、配置、链路围绕 HTTP 语义），调试友好，但序列化体积大、性能与吞吐低于二进制协议；
  - Dubbo：TCP + 二进制（hessian2/protobuf），**Triple/gRPC 支持流式**，长连接复用、序列化高效，单次调用开销低（RT 差 1~3ms 量级），但生态要配 Dubbo 自家的治理面板。
- **选型**：
  - 面向外/异构/前端联动、团队 REST 文化 → Spring Cloud 路线；
  - 内网高频、低延迟、大流量（如交易核心链路、QPS 上万）→ Dubbo/Triple；
  - 混合现实：**边界对外 REST，内部核心 RPC 走 Dubbo**。
- **混用经历（SCA 口径）**：SCA 下两者共享 **Nacos 注册中心**——Dubbo 服务与 Feign 服务在同一注册表内但走不同协议；用 Dubbo 提供**内部高性能接口**，用 OpenFeign 暴露**给网关/上游 Spring 服务的 HTTP 接口**；坑：接口命名/分组冲突、两个客户端各自的重试与超时要分别治理，避免「同一个方法两条通道配置不一致」。
---

## 二、注册中心（必考）

### 4. Eureka 原理追问链

> 原题要点：AP 自我保护的代价、三级缓存与 30s 一致性、2.x 烂尾、感知延迟叠加。

- **AP 设计与自我保护**：
  - 机制：15 分钟内心跳比例低于 **85%** 时，认为「可能是网络分区而不是服务全挂」，**停止剔除过期实例**，同时新注册仍写入、读取不受影响；
  - 解决的问题：网络抖动/机房分区时**防止把活着的实例全部剔除**导致服务雪崩（宁要脏数据，不要没数据）；
  - 代价：**真实下线的死实例也留在注册表里**，消费者可能调到僵尸节点——必须配合客户端快速失败 + 重试 + 熔断兜底；同时只读缓存照常返回旧数据，感知更慢。
- **三级缓存**：
  - `readOnlyCacheMap`（只读缓存，ConcurrentHashMap，客户端 GET 默认走它，**30s 定时**从二级刷新）；
  - `readWriteCacheMap`（Guava Cache，即 renderRegistry，**写注册/下线时实时失效**对应 key，180s 过期）；
  - 客户端本地注册表缓存（`DiscoveryClient` 30s 增量/全量拉取 + ribbon 自己的 ServerList 缓存再 30s）。
  - 为什么：注册中心读多写少（读:写 常上百比一），**用多级缓存扛读放大**；代价是**最长 30+30+30 ≈ 90s（极端再加 Ribbon 缓存）的数据不一致窗口**。可配 `eureka.server.use-read-only-response-cache=false` 关一级缓存缩短延迟。
- **2.x 烂尾原因**：Netflix 转向内部新方案，2.x 与 1.x API 不兼容、社区维护动力消失，官方直接宣布只维护 1.x（后被 Spring Cloud Netflix 继续护住）——即 Eureka 事实上**功能冻结**。
- **感知延迟是固有的**：Eureka 无服务端推送（只有客户端定时拉 + 心跳），AP 模型优先可用性，**秒级~分钟级感知是设计取舍**；要缩短只能关只读缓存、调短拉取间隔，治标不治本，需要更快感知就换 Nacos/ZooKeeper 类推送模型。

### 5. Nacos 原理追问链

> 原题要点：Distro vs Raft、临时 vs 持久实例、UDP→gRPC 推送、双重健康检查误判、优雅下线推空保护。

- **AP/CP 两模式与实例类型**：
  - **临时实例（ephemeral，默认）→ AP/Distro**：客户端注册、心跳维持（1.x）/连接维持（2.x），数据存内存，节点间用 **Distro 协议异步复制**（每个节点负责一部分数据的写，全量对账），分区时各自可用——适合微服务实例（可重启、可丢）；
  - **持久实例 → CP/JRaft**：服务端持久化（Derby/MySQL）+ Raft 多数派写入，不健康**只标记不下线**——适合 DNS-F/配置类、数据库、不能丢的服务。
  - 怎么选：微服务用临时；要求「即使注册中心分区实例也不能消失」的（如网关回源列表）用持久。
- **UDP → gRPC 推送**：
  - 1.x：订阅推送走 **UDP**——NAT/防火墙下丢包、客户端不可达时反复重试，延迟秒级、还常误判掉线；
  - 2.x：注册/订阅/推送全部走 **gRPC 长连接**（复用一条 TCP 多路复用），推送从「尽力而为的 UDP」变成**可靠有序的连接内消息**，且健康检查从「定时心跳」升级为「连接活性」，断连即感知；因此**变更感知从秒级降到毫秒级**（服务端推送 + 客户端即时应用）。
- **双重健康检查误判**：心跳超时 ≠ 实例挂（可能 GC 停顿、网络抖动、负载高导致心跳线程饿死）；2.x 长连接断开 ≠ 进程死（可能只是网络闪断重连）。误判防护：标记不健康给宽限期、客户端重连恢复、服务端对比多个信号（心跳 + 连接 + 主动探测）。
- **优雅下线与推空保护**：
  - 下线顺序：**先从注册中心主动注销（HTTP DELETE / instance）→ 等消费者缓存刷新/收到推送（sleep 数秒）→ kill 进程（先 SIGTERM 走 shutdown hook：停流量入口、清理线程池）**；
  - 推空保护：消费者/网关订阅回调**收到空实例列表时拒绝应用**（保留上次快照并告警），防止 Nacos 异常把整服务「推空」造成全量故障；Nacos 2.x 客户端本身也有本地快照容灾。

### 6. 注册中心对比选型

> 原题要点：Eureka/Nacos/Consul/ZooKeeper 的 CAP、健康检查、推送时效、运维成本对比；今天新项目选谁。

| 维度 | Eureka | Nacos 2.x | Consul | ZooKeeper |
|---|---|---|---|---|
| CAP | AP | AP+CP 双模 | AP（默认）/CP 可选 | CP |
| 健康检查 | 客户端心跳 | 心跳+连接+探测 | Agent/多协议 | 会话 KeepAlive |
| 变更感知 | 秒~分钟（拉） | **毫秒（gRPC 推送）** | 秒级（长轮询/blocking query） | 秒内（watch） |
| 配置中心 | 无 | 内置 | KV 可当配置用 | 无（znode 可凑合） |
| 运维成本 | 低（但停更） | 低（开箱即用，中文文档） | 中（Agent 每机部署） | 高（JVM、脑裂、ZAB 运维门槛） |
| 生态 | 老系统 | 阿里系/国内主流 | HashiCorp 系/K8s 生态 | Hadoop/Dubbo 老生态 |

- **今天新项目**：默认选 **Nacos 2.x**——注册+配置二合一、双模 CAP、毫秒推送、运维简单、社区活跃（国内语境）；K8s 深度用户可以直接不引注册中心，用 **K8s Service/DNS** 做发现；海外/HashiCorp 生态或需要多数据中心（Consul 的强项，WAN federation）才考虑 Consul。

### 7. 场景题：发布期间消费者调到已下线实例

> 原题要点：怎么系统性解决——优雅停机、主动下线、网关侧过滤、灰度池隔离。

- **根因**：注册中心感知延迟（多级缓存+拉取）> 发布窗口，消费者缓存里还有死实例。
- **系统性方案（分层叠加）**：
  1. **优雅停机**：接 `SIGTERM` → 先摘流量（从注册中心注销 / 网关摘除 / K8s preStop）→ 等待传播（> 最大缓存延迟，如 10~30s）→ 排水（处理完存量请求）→ 关闭容器；K8s 里 `preStop sleep + terminationGracePeriodSeconds` 配套；
  2. **主动下线**：发布脚本调用 Nacos OpenAPI 显式注销，别只靠 kill；滚动发布时一台台来，控制并发摘流比例；
  3. **消费端兜底**：LoadBalancer 配重试 `retryable` 且**避开同实例**；客户端对 `Connection refused` 类错误快速摘除本地实例（Ribbon 的 `ServerListUpdater` + 状态判定）；
  4. **网关侧过滤**：网关路由层做实例健康聚合 + 主动探测（health check passivity/activity），不健康实例不参与负载；
  5. **灰度池隔离**：新旧版本分组（meta/label 打标），发布期只把流量切到新组，旧组下线前确认零流量。
- **验收标准**：演练验证——下线一台，抓全链路日志确认 0 个 `Connection refused` 到达业务层。

---

## 三、配置中心

### 8. Nacos 配置中心长轮询（29.5s）机制

> 原题要点：完整时序；为什么是长轮询而不是长连接或纯推送；灰度发布与多环境隔离设计。

- **时序（客户端 ↔ 服务端）**：
  1. 客户端启动拉全量配置落本地快照（cache-data）；
  2. 客户端发起 **`/listener` 长轮询请求**，携带关注的 `dataId+group+tenant` 及其 **MD5**；
  3. 服务端 hold 住请求，**默认 29.5s**（实际 30s 减去 0.5s 余量）内轮询比较 MD5；期间配置变了 → **立即返回变更的 dataId 列表**（注意：只返回 key，不返回值）；没变 → 返回空，客户端马上再发下一轮；
  4. 客户端收到变更 key → **再发一次 GET 拉取新值** → 回调监听器刷新。
- **为什么长轮询**：
  - 纯定时轮询：延迟高（最长一个周期）+ 无效请求多；
  - 纯推送（服务端长连接推）：服务端要维护所有客户端连接与推送可靠性，1.x 时代 HTTP 下做不到（没长连接），且推送丢消息要补偿机制；
  - 长轮询 = **推拉结合**：变更「准实时」（服务端 MD5 比对是 1~10s 粒度的定时任务 + 唤醒，准毫秒级感知），无变更时每 30s 一个轻量请求，实现简单、天然穿透 NAT/防火墙；29.5s 是为了**避开网关/代理 30s 空闲超时断连**。
- **灰度发布**：Nacos 配置「Beta 发布」按 **IP 列表**推送灰度值，验证后再全量；配置历史留痕可回滚。
- **多环境/多租户隔离设计**：
  - 三元组：**namespace（环境隔离：dev/test/staging/prod）→ group（业务域/项目线）→ dataId（服务名-环境.profile 或 服务名+用途）**；
  - 约定：namespace 按环境物理隔离（权限可分人），group 按大团队/业务线，dataId 命名规范 `应用名[-场景].yaml`；权限粒度做到 namespace，敏感 namespace 单独账号。

### 9. 配置优先级、@RefreshScope 原理、刷新失败兜底

> 原题要点：优先级；动态刷新的 Bean 重建机制；刷新失败/部分失败兜底。

- **优先级（Nacos 体系，高→低）**：`dataId 明确带 profile` > `dataId 默认（不带 profile）` > 扩展 `extension-configs`（按下标）> 共享 `shared-configs`；Spring 侧 `bootstrap.yml` 加载的远程配置优先于本地（`spring.cloud.config.override-none` 等可调）；同一 key 以**最后加载的覆盖先加载的**为准，排查时看 `Environment` 的 propertySource 顺序。
- **@RefreshScope 原理**：
  - 标注类被注册为 **`RefreshScope`（GenericScope）管理的「懒加载代理 Bean」**——容器里放的是 CGLIB 代理，方法调用时按 name 到 scope 缓存里取真实实例；
  - 配置变更（`EnvironmentChangeEvent`）→ `RefreshScope.refreshAll()/refresh(name)`：**把 scope 缓存里的目标 Bean 销毁**（触发 `@PreDestroy`），**下次第一次调用时用新 Environment 重新创建**；
  - 所以热刷新生效条件：**Bean 必须是代理且通过代理调用**（自调用/未加注解的 Bean 不刷新）；`@ConfigurationProperties` 走的是另一条路（rebind，不需要 RefreshScope）。
- **刷新失败/部分失败兜底**：
  - 失败场景：Bean 重建抛异常（新配置不合法）、部分实例网络分区没收到事件；
  - 兜底：监听器代码要 try-catch + 保底用旧值；配置校验前置（发布前 Dry-run/预检脚本）；部分实例未刷新 → 提供**管理端点手动触发 `/actuator/refresh`** 或延迟 N 秒重试；关键配置刷新后**健康检查上报**，配置平台轮询实例刷新状态；必要时滚动重启兜底（配置刷新不等价于重启，接受不了就重启，别硬撑）。

### 10. Nacos vs Apollo、敏感配置加密

> 原题要点：审批/权限/审计/灰度维度选型；数据库密码加密（Jasypt/KMS）。

- **对比**：
  - **Apollo**：企业级治理强——**发布审批流、细粒度权限（应用/环境/namespace 三级）、完整审计日志、发布状态追踪、多环境多集群模型成熟、灰度按 IP**；组件多（portal/admin/service）运维重，客户端只做配置；
  - **Nacos**：轻量、注册配置一体、SDK/文档国内生态好；治理能力相对弱（权限到 namespace、1.x 无审批流，2.x 部分增强），审计要自己补。
- **选型**：**配置变更要过合规审计（金融/大厂流程）→ Apollo**；中小团队、要注册+配置一体、运维人力少 → **Nacos**；折中：Nacos + 自建发布审批/操作审计平台（企业内部网关代理写接口）。
- **敏感配置加密**：
  - **Jasypt**：应用本地解密——配置里存 `ENC(xxx)`，盐/主密钥通过环境变量或启动参数注入（**绝不进配置中心明文**）；简单但有局限：密钥分发管理、轮换要自己管；
  - **KMS（云厂商/自建 Vault）**：配置中心只存密文 + keyId，应用启动时调 KMS 解密或直接由 KMS 动态下发，支持**审计、轮换、按应用授权**；金融/强合规场景必选；
  - 附加：配置中心开 HTTPS+鉴权、数据库密码定期轮换、应用侧连接池密码热更配合 RefreshScope。

### 11. 场景题：配置中心全挂了怎么保证服务还能起、还能跑

> 原题要点：本地快照/cache-data/启动容灾参数；是否演练过。

- **Nacos 容灾机制**：
  - **本地快照（failover 文件）**：客户端每次成功拉取都落盘快照（`~/nacos/config/...` 目录），服务端不可达时**用快照兜底**；另有**主动 failover 文件目录**，运维手工放置可强制覆盖远端；
  - 启动期：拉不到配置时客户端**阻塞重试但有超时降级**（可用 `spring.cloud.nacos.config.fail-fast=false` 及超时参数调），用快照先起进程；
  - 运行期：已加载配置在内存里，**配置中心挂了不影响运行中的服务**（只影响后续变更）。
- **架构级兜底**：
  - Nacos 服务端三节点集群 + MySQL 主备，前置 SLB，客户端配置多个 server-addr；
  - 关键配置「双写」：极少数保命参数（DB 地址、开关）同时打进 **镜像/启动参数/K8s ConfigMap**；
  - 网关/服务 discovery 用本地快照同理（Nacos 客户端有 server list 快照与实例快照）。
- **演练口径（面试加分）**：做过——预发环境 kill 整个 Nacos 集群，验证：存量服务零影响、新扩容实例能靠快照启动、恢复后自动重连并同步期间变更（注意验证**宕机期间的配置变更在恢复后能被感知**，这是长轮询模型恢复的关键点）；演练发现的问题举例：fail-fast 配错导致新实例起不来 → 修正启动参数。
---

## 四、RPC 与负载均衡

### 12. OpenFeign 原理追问链

> 原题要点：接口→HTTP 的链路；客户端替换与连接池；超时覆盖关系；重试叠加导致写请求重复的事故与规避。

- **调用链路**：
  1. `@EnableFeignClients` 扫描 `@FeignClient` 接口 → `FeignClientsRegistrar` 注册 **`FeignClientFactoryBean`**；
  2. 容器里注入的是 **JDK 动态代理**（`FeignClientFactoryBean.getObject()` → `Feign.Builder.target()`），接口每个方法绑定一个 **`SynchronousMethodHandler`**；
  3. 调用时：解析 `@PathVariable/@RequestBody` → **Encoder** 编码 → 组装 `Request`（模板化 URL）→ **Client** 发 HTTP（默认 `Client.Default` = JDK HttpURLConnection）→ **Decoder** 反序列化 → 非 2xx 走 **ErrorDecoder**。
- **客户端替换与连接池**：
  - 默认 JDK 客户端**没有连接池**，**每次请求新建 TCP 连接**——高 QPS 下 TIME_WAIT 堆积、端口耗尽、RT 抖动，这就是「连接池不配会发生什么」的标准答案；
  - 替换：引 `feign-okhttp` / `feign-hc5`（Apache HttpClient5），配置最大连接数/每路由连接数/keep-alive；连接池指标要接监控（活跃/等待/空闲）。
- **超时覆盖关系**（优先级高 → 低）：
  - Feign：`@FeignClient(configuration=...)` 方法级/接口级 > `feign.client.config.<服务名>.connectTimeout/readTimeout` > 全局默认（connect 10s / read **60s**，偏大要主动收）；
  - 有 Ribbon 的老版本：**`ribbon.ReadTimeout` 等显式配置优先于 Feign 配置**（经典坑：Feign 配了 3s 实际还是 ribbon 的 60s）；
  - LoadBalancer 时代：SC LoadBalancer 本身不管超时，Feign 超时即底层 HTTP 客户端超时；
  - **全链路阶梯**：客户端/网关超时 必须 **>** 下游 Feign 读超时（内小外大），否则上游先断下游还在空跑；外面包了 Hystrix/Sentinel 时还要保证 **熔断超时 > Feign read timeout**，否则熔断先触发、真实错误被 fallback 掩盖。
- **重试叠加事故与规避**：
  - Feign 默认 `Retryer.NEVER_RETRY`；一旦配了 `Retryer`（如 `PeriodicRetryer`）再叠加 LoadBalancer 的 `RetryLoadBalancerInterceptor`（`spring.cloud.loadbalancer.retry.enabled=true` 时换实例重试），一次失败最坏被放大为 **LB 重试次数 × Retryer 重试次数** 次真实请求；
  - **写接口（下单/扣款）被重复执行**就是这么来的经典事故；
  - 规避：**写接口一律禁重试**（`Retryer.NEVER_RETRY` + LB retry 关闭或按 client 设 `retryable=false`）；必须重试先保证**幂等**（唯一请求 ID + 服务端去重表/分布式锁）；只对幂等 GET、可重试异常（连接类异常、502/503）重试，**读超时不盲目重试**（下游可能已执行成功）；次数收敛（≤2）+ 退避间隔。

### 13. Ribbon vs Spring Cloud LoadBalancer

> 原题要点：轮询实现的 CAS 改进、`@LoadBalanced` 原理；自定义负载均衡策略。

- **差异**：
  - Ribbon：Netflix 老组件，**启动拉全量服务列表、默认饿加载、定时刷新**，轮询 `incrementAndGet % count`（对溢出/并发的处理粗糙），扩展靠全局 IRule，与 Eureka 强耦合；
  - **SC LoadBalancer**：官方新实现，响应式（`ReactorServiceInstanceLoadBalancer`）、**懒加载**（首次调用才拉实例）、基于 `ServiceInstanceListSupplier` 可组合（缓存/发现/健康过滤）；
  - 轮询改进点：新实现用 `AtomicInteger` + **`pos & Integer.MAX_VALUE` 位运算防负数越界**（老 Ribbon 靠 abs/取模，溢出时有抖动），且每次请求创建 `AtomicInteger` 还是共享实例的语义更清晰，避免 CAS 自旋重试浪费。
- **`@LoadBalanced` 原理**：本质是 **`@Qualifier` 的具名限定符**——给 RestTemplate/WebClient Bean 打标记；`LoadBalancerAutoConfiguration` 用 `@LoadBalanced` 收集这些 Bean，为它们包上 `LoadBalancerInterceptor`，拦截请求时**把 URI 里的服务名替换为 LoadBalancerClient 选出的实例地址**。
- **自定义策略**：实现 `ReactorServiceInstanceLoadBalancer`（如按 zone 亲和、权重、灰度标签过滤后轮询），在 `@LoadBalanced` 对应的子上下文里注册为 `@Bean` 生效（Ribbon 时代是 IRule）。做过的话举例：同机房优先 + 剩余连接数加权。

### 14. 服务调用的超时与降级全局策略

> 原题要点：超时怎么定（P99 打底 + 留 buffer）；线程池隔离 vs 信号量隔离在 Feign 层怎么落地。

- **超时定值方法**：
  - 拉下游接口 **P99（甚至 P999）基线**，超时 = P99 × 1.5~2 或 P99 + 固定 buffer；再按**入口 RT 预算**反推层层分解（总预算 - 本地耗时 = 下游配额）；
  - 区分 connect（1~2s 足够，建连慢=网络问题）与 read（按业务）；全局默认值收敛（如 read 3~5s），**慢接口单独覆写**，别让 60s 默认值裸奔；
  - 超时沿调用链**层层递减**：客户端 < 网关 < 服务端，避免上游先断。
- **隔离方式**：
  - **线程池隔离**：每类下游独立线程池，慢下游只耗尽自己的池；代价：线程切换开销、内存占用、池配置爆炸（几十个依赖时不可维护）；
  - **信号量隔离**：计数器限并发，**无线程切换、开销小**，但**当前线程会被下游同步阻塞占用**（必须配好超时才安全）；
  - **Feign 落地口径**：Feign 同步调用本身就是「调用线程等响应」，绝大多数场景**信号量/并发数限流（Sentinel 线程数流控）+ 熔断 + 超时收敛**即可；只有**极慢/不可控下游**或异步边界才需要独立线程池；Hystrix 那套「一依赖一池」已不推荐。

### 15. 场景题：Feign 接口偶发 Read timed out 排查树

> 原题要点：网络 → 连接池 → GC → 下游慢 → 重试叠加放大，逐层确认。

- **第 0 层 现象归类**：抓超时样本——时间分布（是否集中在整点/发布/GC 高峰）、目标实例分布（单实例 or 全部）、接口分布；先看调用方/下游的 RT、QPS、GC、连接池四组监控。
- **1. 网络层**：跨机房/跨 AZ？`ping/mtr` 看丢包与延迟；K8s 内看 conntrack 表是否打满（丢包）、CNI、DNS 解析耗时（卡在解析是 connect 超时不是 read）；必要时 tcpdump 对齐「请求发出 → 响应到达」时间轴。
- **2. 连接池层**：配池了吗（默认 JDK 无池）？池打满后排队——排队时间计入总耗时表现为偶发超时；看池活跃/等待指标；**服务端先关闭 keep-alive 连接**（服务端 keepalive_timeout < 客户端 idle）→ 半开连接导致偶发 `NoHttpResponseException`/重发，配客户端连接空闲回收时间。
- **3. 调用方 GC 层**：请求已发出，线程随后 STW，恢复后响应其实已到但计时已超——**最常被忽略的一层**；GC 日志（`-Xlog:gc*`）停顿时间点与超时时间轴对齐即可实锤。
- **4. 下游真的慢**：下游自身 GC、慢 SQL、锁竞争、线程池满、突发流量；看下游 P99 与该时段画像；若下游 RT 本来就贴着超时值 → 回去重定超时（P99+buffer）。
- **5. 重试叠加放大**：超时触发重试 → 下游雪上加霜 → 更多超时（重试风暴）；特征：下游 QPS ≈ 上游 QPS × (1+重试次数)；处理：关掉读超时重试、加退避、熔断止血。
- **收尾模板**：定位根因 → 修复（超时收敛/加池/修慢 SQL/扩容）→ 防护（熔断+限流+隔离）→ 复盘沉淀预案。

---

## 五、熔断、限流、降级（必考）

### 16. Hystrix 为什么被淘汰；Sentinel 与 Resilience4j 对比

> 原题要点：线程池开销、维护停滞；规则中心/注解支持/热更新对比。

- **Hystrix 淘汰原因**：
  - **每依赖一个线程池**（默认 10 线程），切换开销 + 内存占用大，几十个依赖时线程池爆炸；信号量隔离又功能受限；
  - Netflix 2018 年起**进入维护模式**（只修 P0 问题），无新特性，基于 RxJava 1.x 的 API 老旧，社区迁移；
  - 官方继任：Resilience4j；国内生产级方案：Sentinel。
- **对比**：
  - **Sentinel**：自带 **Dashboard + 规则中心**（配 Nacos/Apollo 持久化），规则**热更新秒级推送**；`@SentinelResource` 注解；功能全（流控/熔断/热点/系统自适应/集群流控）；阿里双 11 生产验证；
  - **Resilience4j**：轻量库（无控制台，指标靠 Micrometer 自建看板），函数式 API + Spring 注解（`@CircuitBreaker/@RateLimiter/@Retry`），K8s/Boot 原生友好；规则改配置要重启或自建动态绑定；
  - 选型口径：要**可视化运营 + 运维改规则 + 大促保障** → Sentinel；要轻量内聚、基础设施已 K8s 化 → Resilience4j。

### 17. Sentinel 原理追问链

> 原题要点：LeapArray；三种流控策略；熔断三策略与半开恢复；热点参数限流；token server 集群限流；blockHandler vs fallback。

- **滑动窗口 LeapArray**：
  - 时间轴切成 N 个 **`WindowWrap` 桶**（如 1s 统计 = 2 个 500ms 桶），每桶一组 `AtomicLong`（pass/block/exception/RT 等 MetricBucket）；
  - 定位当前桶：`(timeMillis / windowLength) % sampleCount`；桶过期时**用锁 + 原子替换（CAS）复用/重置**，**读路径无锁、写竞争最小化**；
  - 统计值 = 窗口内各桶求和 ÷ 窗口时长；这就是秒级滑动窗口的实现基础。
- **QPS 流控三策略**：
  - **直接**：资源自身超阈值就拒——常规限流；
  - **关联**：**关联资源**超阈值时限制自身——典型：写操作（下单）超限时把读操作（查询）限掉，保护共享的 DB/缓存；
  - **链路**：只统计**指定入口链路**的调用——同一资源被多入口共享、只想限某条链路（坑：要关 context 统一收敛 `web-context-unify=false` 才生效）。
  - 流控效果：快速失败 / **Warm Up**（冷启动斜坡抬阈值，防秒杀瞬间打满连接池缓存）/ **排队等待**（漏桶匀速，削峰）。
- **熔断三策略与半开恢复**：
  - **慢调用比例**：RT > 阈值计为慢调用，窗口内慢调用比例超阈值且请求数达标 → 熔断（下游变慢首选）；
  - **异常比例 / 异常数**：异常占比或绝对数超阈值 → 熔断；
  - 恢复：熔断 `recoverTimeout` 毫秒后进入 **HALF-OPEN**——放**一个探测请求**，成功则关闭熔断、失败则继续熔断（再等一个周期）；比 Hystrix 固定 5s 休眠更可控。
- **热点参数限流**：按**参数索引**取 arg，对**每个参数值独立计数**（内部是可扩容哈希桶 + 滑动窗口结构，LRU 淘汰冷值），支持**参数例外项**（特定商品 ID/用户 ID 单独阈值）；典型：秒杀按商品 ID 限流。
- **集群限流 Token Server**：
  - 各节点作为 **token client** 向内嵌/独立部署的 **token server 申请令牌**（支持批量预取），server 按「全局阈值 ÷ 实例数（或按需分配）」发放，**保证全局总量精确**；
  - 解决的问题：单机限流 × N 台在阈值小、流量不均时偏差大（有的实例超发、有的没用满）；代价：server 单点 → client 要有 **本地限流 fallback**，server 主备部署。
- **`@SentinelResource` 的 blockHandler vs fallback**：
  - **`blockHandler`**：接 **`BlockException`**（限流/熔断/系统保护**规则触发**），语义是「被拒绝」；
  - **`fallback`**：接**业务异常**（`Throwable`），语义是「执行出错」；
  - 两者都配则各管各的；签名要求与原方法一致 + 追加对应异常参数；返回类型必须一致；未定义时走 `defaultFallback` 或直接抛出。

### 18. 降级开关体系设计

> 原题要点：预案分级、谁有权限开、怎么演练验证、怎么自动恢复。

- **预案分级**：
  - L1 只读降级（返回缓存旧数据/默认值）→ L2 功能裁剪（关非核心链路：积分、推荐、日志降采样）→ L3 核心保护（限流加严、熔断阈值收紧、只保留下单支付主链）→ L4 熔断保护（拒绝写、只读模式）；
  - 每级写清**触发条件**（下游 RT/错误率/容量水位）与**恢复条件**，做成预案手册。
- **开关设计**：配置中心托管（Nacos/Apollo），命名规范（`预案.业务.开关名` + 值域），全量开关有**台账**；原则：**有代码兜底路径才允许加开关**（开了必须有行为变化）。
- **权限**：配置平台 RBAC——值班（oncall）可开**预案内**开关，非常规开关走变更审批；所有开合操作**审计 + IM 群广播**；大促按预案预授权值班长权限。
- **演练验证**：
  - 例行：故障演练平台（ChaosBlade 等）周期性打挂下游，验证开关生效、兜底返回正确、告警冒烟；
  - 大促前：全链路预案推演 + 上线准入（新接口必须配降级才能上）。
- **自动恢复**：熔断靠半开自愈；业务开关配「**延时自动关闭 + 恢复探测**」——下游健康检查连续 N 次正常才回切，回切斜坡放量防抖；不确定的报警转人工。原则：**能自动的先自动，不确定的报警等人**。

### 19. 场景题：核心接口被下游抖动拖垮的完整防护

> 原题要点：超时收敛 → 熔断 → 降级兜底 → 线程隔离 → 预案开关。

- **故障模型**：核心接口 A 同步依赖 B（风控/库存），B 抖动 → A 的调用线程被 read timeout 挂住 → 线程池耗尽 → **A 上所有接口不可用（雪崩扩散）**。
- **防护五步（由内到外）**：
  1. **超时收敛**：A→B read timeout 从默认 60s 收敛到 P99×2（如 800ms）、connect 1s，调用线程快速释放——**防雪崩第一道闸**；
  2. **熔断**：A→B 配 Sentinel 慢调用比例熔断（如 RT>500ms 占比>50%）+ 异常比例熔断兜底，半开探测恢复；
  3. **降级兜底**：熔断/限流触发返回**有业务语义的兜底**（默认风控走低额度、库存读缓存旧值、推荐返回热门榜），`blockHandler/fallback` 分离，兜底逻辑本身要轻（不做重 IO，别把兜底写成新的慢点）；
  4. **隔离**：A→B 并发线程数流控（如 50），超出的直接走降级——**把故障限制在 B 的配额内**，不占 A 的整体容量；
  5. **预案开关**：B 的全局降级总开关放配置中心，极端情况一键全降级；配套降级量告警与恢复回切斜坡。
- **辅助**：入口限流保护 A 自身容量；B 恢复后半开探测 + 斜坡回切；事后复盘把该链路纳入例行演练。
- **表达要点**：「超时是最重要的兜底」「隔离的目的不是修 B，是保证 B 挂时 A 还活着」「降级是有损可用，不是报错」。
---

## 六、网关

### 20. Spring Cloud Gateway 原理追问链

> 原题要点：WebFlux/Netty 反应式模型为什么扛高并发、什么时候反而更糟；Route/Predicate/Filter 与两类 Filter；动态路由；自定义 Filter 实践。

- **反应式模型**：
  - 基于 **Netty（NIO event loop，少量线程服务海量连接）+ WebFlux（Reactor）**：请求处理不绑定线程，IO 等待期间 event loop 可服务其他连接——**大量连接/高并发纯转发场景单机吞吐远高于线程池模型（Tomcat 200 线程上限）**；
  - **反而更糟的场景**：Filter/Handler 里写了**阻塞调用**（同步 JDBC、阻塞 RPC、`Thread.sleep`、同步 Redis 客户端）——**卡死 event loop**，吞吐骤降甚至整个网关停摆；原则：网关链路全异步非阻塞，必须阻塞就丢到独立调度器（`publishOn(boundedElastic)`），或别在网关做重逻辑。
- **三件套与 Filter 体系**：
  - **Route**（目标 `lb://服务名` + 断言 + 过滤器集合）、**Predicate**（Path/Header/Method/Query/Weight 等匹配条件）、**Filter**（改写请求/响应）；
  - **GatewayFilter**（路由级/默认过滤器）与 **GlobalFilter**（全局过滤器）：执行时统一按 **order 排序合并成一条链**（内置的 LoadBalancer、NettyRoutingFilter 都是 GlobalFilter，按 order 插入其中）；**坑：自定义 Filter 要显式定义 order（`getOrder()`/`@Order`），默认 order=0 会插到链中间，出现「转发先于鉴权」类事故**。
- **动态路由**：
  - 实现思路：路由表存 **Nacos 配置（dataId=网关路由 JSON）** → 监听配置变更 → 写入 `RouteDefinitionWriter` → 发布 **`RefreshRoutesEvent`** → `RouteLocator`（CachingRouteLocator）重建缓存路由表；
  - 也可基于 DiscoveryClient 自动生成按服务名的路由（`lb://`），叠加 Weight 断言做比例灰度。
- **自定义 Filter 实践（面试按项目讲）**：
  - **鉴权**：GlobalFilter 校验 token/签名，失败直接 401 短路，解析出的用户上下文透传给下游 header；
  - **限流**：KeyResolver（IP/用户/接口维度）+ RequestRateLimiter；
  - **灰度染色**：命中灰度规则 → 打标 `X-Gray-Tag` → 路由到灰度路由/实例；
  - **日志追踪**：生成 requestId、注入 traceId、异步 access log。

### 21. 网关限流与网关层熔断分工

> 原题要点：RequestRateLimiter + Redis Lua / Sentinel 网关流控；网关熔断与应用层熔断怎么分工。

- **内置限流**：`RequestRateLimiter` 过滤器 + **`RedisRateLimiter`**（令牌桶算法，**Redis + Lua 脚本保证分布式原子**，支持补充速率与突发容量 burstCapacity）+ `KeyResolver` 定义维度（IP/用户/接口 path）；Redis 不可用时 fail-open 还是 fail-closed 按业务定（一般保命限流 fail-closed，体验类 fail-open）。
- **Sentinel 网关流控**：`sentinel-spring-cloud-gateway-adapter`，按 route/API 分组限流，规则热更，可接集群流控。
- **熔断分工**：
  - **网关层**：管「**服务整体不可用**」的粗粒度逃生——某服务全实例超时/失败时对整个路由熔断，直接返回兜底响应，保护网关自身与用户体验；
  - **应用层（Sentinel+Feign）**：管「**接口/实例级抖动**」的细粒度治理，能做业务语义降级；
  - 阈值要有梯度：应用层先熔断（细），网关层最后兜底（粗），避免网关先熔断导致应用层熔断永远收不到足够数据。

### 22. 灰度发布在网关层落地与链路染色

> 原题要点：按 header/用户/比例路由到灰度泳道；tag 全链路透传怎么保证不串泳道。

- **网关层落地**：
  - **打标**：实例分组——注册中心 metadata（`version=gray`）或 K8s label；
  - **路由**：Gateway 按 **Header 断言**（`Header=X-Gray-Tag, canary`）/Cookie/用户白名单/**Weight 断言**（比例放量）匹配到灰度路由；LoadBalancer 层按 metadata 过滤实例（同标优先，无同标回落基线）；
  - **全链路泳道**：入口染色后，每一跳服务都**优先调用同泳道实例**，泳道缺实例时回落基线——由统一的 LoadBalancer 拦截器按 tag 过滤实现，业务代码无感。
- **链路染色不串泳道的关键**：
  - **入口统一染色**：只有网关/登录入口决定 tag，后续服务**只透传不修改**，防中间层改标；
  - **全链路透传**：HTTP header（`X-Gray-Tag`）+ RPC attachment + **MQ 消息属性** + 定时任务上下文显式带标，统一封装在 Feign 拦截器/MQ 拦截器，禁止业务手工传；
  - **异步不丢标**：跨线程用装饰器/TTL 传递上下文——异步任务丢标回落基线是「串泳道」的典型来源；
  - **数据隔离**：灰度要写新表/新字段时用影子表或行级打标（gray 字段），查询带标过滤，防止灰度数据污染基线。

### 23. 网关自身高可用与容量瓶颈信号

> 原题要点：无状态多实例 + 上层 LB；连接数/TLS/带宽瓶颈信号与应对。

- **高可用**：网关**无状态**（路由在 Nacos、会话在 Redis）→ 多实例 + 上层 Nginx/LB（或 K8s Service+HPA）→ 滚动发布无感；核心与非核心流量**独立网关集群**分组隔离。
- **瓶颈信号**：
  - **连接数**：active connections 逼近上限、accept queue 溢出、fd 耗尽；
  - **TLS**：握手 CPU 高（RSA/ECDHE 计算贵），CPU 打满但业务线程闲——HTTPS 高并发下 TLS 先到瓶颈；
  - **带宽**：网卡流量打满——大文件/大响应转发场景最明显。
- **应对**：
  - 连接池前置 + keep-alive/HTTP2 多路复用减少握手与建连；**TLS 卸载到 LB/CDN**（网关内网明文转发）或用 ECDSA 证书/session ticket 降低握手成本；
  - **大文件直传不走网关**（返回对象存储签名 URL）；
  - 水平扩容 + HPA；按流量拆分核心/非核心网关集群。

---

## 七、可观测性与分布式链路

### 24. TraceId 透传机制与跨线程池丢 TraceId

> 原题要点：Sleuth/Micrometer Tracing 的透传原理；跨线程丢 TraceId 的经典问题怎么解（装饰器/TtlMDC）。

- **透传机制**：
  - 入口（网关/边缘服务）生成或提取 **traceId（整链路不变）+ spanId（每跳更新）+ parentSpanId**，通过 W3C `traceparent` / B3（`X-B3-TraceId`）等 header 传播；
  - 运行时写入 **MDC**（key=traceId），日志 pattern 输出；跨服务调用由 Feign/RestTemplate/WebClient 的客户端拦截器自动注入 header；Sleuth 用 Brave 实现，Boot 3 后升级为 **Micrometer Tracing**（可桥接 OTel，兼容 Zipkin/OTLP 双出口）。
- **跨线程丢 TraceId 的原因**：MDC 基于 **ThreadLocal**；`@Async`/自建线程池/`CompletableFuture` 默认不继承调用方上下文。
- **解法**：
  - **装饰器**：Sleuth 自动包装 `Executor/TaskScheduler` Bean（`ExecutorBeanPostProcessor`）；手写等价实现是 `TaskDecorator`——提交任务时捕获 MDC 快照、子线程执行前回放、finally 清理；
  - **TtlMDC（TransmittableThreadLocal）**：`transmittable-thread-local` + `TtlExecutors` 包装线程池，**执行时捕获/回放**上下文，解决线程池**复用线程**场景下装饰器（提交时快照）覆盖不到的问题；
  - MQ 链路同理：traceId 放消息属性，消费端还原后继续透传。

### 25. SkyWalking / Zipkin / Jaeger 选型；SkyWalking 字节码增强原理

> 原题要点：探针侵入性、存储、UI、告警对比；agent → Instrumentation → 插件原理与性能影响。

- **对比**：
  - **Zipkin**：轻量、Spring 生态原生（Sleuth 默认对接）；UI 简单、无内置拓扑/告警；适合自建轻栈；
  - **Jaeger**：CNCF 项目、云原生/K8s 生态好、OTel 原生导出；存储依赖 Cassandra/ES，UI 一般；
  - **SkyWalking**：**Java agent 字节码增强零侵入**，自带**服务拓扑图 + 指标 + 告警 + 完善 UI**，中文社区强，存储支持 ES/BanyanDB/H2；国内自建首选。
- **字节码增强原理**：
  - **JVM 启动 `-javaagent` → agent premain** → 通过 **`java.lang.instrument.Instrumentation`** 注册 **ByteBuddy** 的 ClassFileTransformer；
  - 类加载时按**插件的 Instrumentation 定义**匹配目标类（JDBC Driver、OkHttp、Spring MVC、RocketMQ client 等），**织入切面字节码**：方法前后创建 span、提取/注入上下文、记录 tag；
  - 插件体系：每个中间件一个插件（可按需启用/禁用），**未匹配的类零开销**；
  - 数据经 gRPC **异步批量**上报 OAP 集群，按采样率落 ES/BanyanDB。
- **性能影响**：织入本身纳秒级；开销主要在 **span 创建、上下文管理与上报**——用**采样率**（核心链路 50%~100%、边缘 1%~10%）、异步批量、丢弃策略控制；GC 压力略增；注意 JDK 大版本升级时验证 agent 兼容性（JDK17+ 模块化/字节码版本限制）。

### 26. 全链路灰度、全链路压测的流量染色架构

> 原题要点：影子库/影子 topic 的实现思路；为什么全链路压测不能简单用预发环境模拟。

- **核心思想——一次染色、全链路生效**：
  - **入口染色**：网关按规则（压测标识 header、灰度用户名单、放量比例）给请求打标；
  - **全链路透传**：HTTP header / RPC attachment / **MQ 消息属性**统一透传（框架层封装，业务无感）；
  - **路由隔离**：服务发现过滤——同泳道实例优先，无则回落基线（灰度）；压测流量路由到**影子链路**；
  - **数据隔离**：**影子表**（同 schema 独立表，生产库内隔离）或行级打标；**影子 topic**（`topic_shadow`）；缓存 key 加影子前缀；**压测写操作一律进影子存储**，真实数据零污染；
  - **配套**：压测流量打全局唯一水印可一键清理回收；影子链路独立限流、日志降采样；一键熔断压测流量的总开关。
- **为什么不能用预发环境模拟全链路压测**：
  - **容量不同**：预发机器规格/数量远小于生产，压出来的水位线、瓶颈点完全不真实（瓶颈位置都不对）；
  - **数据形态不同**：测试数据量级、分布（热点 key、倾斜、索引选择性）与生产差异大——慢 SQL、缓存命中率全部失真；
  - **依赖不全**：第三方、老系统、跨部门依赖往往只有生产全量在跑；
  - **网络/部署差异**：机房拓扑、限流阈值、配置基线不一致，压测结论不可信；
  - 结论：全链路压测必须**在生产用染色流量 + 影子隔离**做，前提是隔离可验证、流量可回收、事故可一键止损。
---

## 八、场景与架构题

### 27. 从 0 设计一套微服务基础设施（5 个服务的最小可用清单）

> 原题要点：网关/注册配置/RPC/熔断/监控/发布规范——最小可用清单，说明不过度设计的理由。

- **最小可用清单（5 服务规模）**：
  - **接入层**：Nginx/SLB + Spring Cloud Gateway（1~2 实例，先不上复杂鉴权链路）；
  - **注册+配置**：**Nacos 2.x 三节点集群**（一个组件同时解决注册与配置，不值得为 5 个服务拆两套系统）；
  - **RPC**：OpenFeign + SC LoadBalancer + OkHttp 连接池（团队熟悉、生态顺、文档全）；
  - **容错**：Sentinel（限流+熔断+Dashboard，规则持久化到 Nacos）；
  - **可观测**：统一日志（Loki/ES）+ Prometheus/Grafana + SkyWalking（一套覆盖指标/日志/链路的 80% 排障需求）；
  - **发布规范**：统一 parent POM/BOM 锁版本；CI/CD 流水线（构建→镜像→滚动发布）；配置中心管理配置禁硬编码；健康检查/优雅停机标准化接入；多环境用 namespace 隔离；
  - **消息/调度**：一套 MQ（RocketMQ/Kafka）+ 轻量调度（XXL-Job 或先 cron）。
- **明确不做的（防过度设计）**：
  - **不上 Seata**：先同库事务 + 消息最终一致，分布式事务等业务复杂度真到了再上；
  - **不上 Service Mesh**：5 个服务引入 Istio 的运维成本远大于收益；
  - **不做多泳道灰度、自建 PaaS**：实例先打 metadata 留好扩展点，规模到了再启用；
  - 组件尽量「一专多能」（Nacos 注册+配置、SkyWalking 拓扑+指标）。
- **演进留白**：每项都选社区主流且有平滑升级路径（Nacos→大规模再评估、Feign→需要时换 Dubbo），到几十个服务规模再引入全链路灰度/多环境治理。

### 28. 服务依赖故障演练：故意挂掉一个下游，预期行为是什么

> 原题要点：预期系统行为 + 演练 checklist（熔断触发、降级兜底、告警冒烟、恢复探测）。

- **预期行为**：下游挂掉后，**上游快速熔断**（不是一直等超时）、**调用方返回降级兜底**（有损可用而非报错）、**自身资源不恶化**（线程/连接不堆积）、**告警及时触达**、下游恢复后**自动探测回切**——一句话：故障被「隔离 + 有损消化 + 可观测 + 可自愈」。
- **演练 checklist**：
  - **准备**：环境与爆炸半径（单实例/单接口，避开高峰）、注入方式（kill / 延迟注入 / 异常注入，**慢故障必须测**）、回滚开关、基线指标留档；
  - [ ] **熔断触发**：错误率/慢调用比例达阈值后熔断打开（Sentinel Dashboard/日志可见），上游无大量请求悬挂；
  - [ ] **降级兜底**：返回有业务语义的兜底结果（默认值/缓存旧值），用户侧无 5xx 扩散；
  - [ ] **资源不恶化**：调用方线程池/Tomcat 线程/连接池稳定，RT 无放大；
  - [ ] **重试不放大**：下游失败期上游重试未形成风暴（QPS 倍数 ≤ 配置预期）；
  - [ ] **告警冒烟**：熔断/错误率告警在 SLA 内（如 1min）触达值班群，信息可定位；
  - [ ] **链路可观测**：TraceId 可检索到故障跳数，拓扑图正确显示依赖异常；
  - [ ] **恢复探测**：下游恢复 → 熔断半开 → 探测成功 → 自动关闭 → 流量回切（斜坡），告警自动收敛。
- **演练后**：输出报告（发现的问题 → 改进项 → 责任人/期限）；把「预期行为」固化成断言脚本接入例行演练；预案文档更新。

### 29. 微服务拆分后接口 RT 比单体高 20~50ms 的原因与优化

> 原题要点：网络、序列化、鉴权链路、多跳；本地缓存元数据、批量接口、并行编排、去冗余跳数。

- **原因拆解**：
  - **网络**：每跳一次 RTT（内网 0.5~2ms 起步，跨 AZ 更高）+ DNS/建连（没配连接池时更贵）；
  - **序列化**：HTTP+JSON 编解码 CPU 开销、报文体积大；单体是方法调用零开销；
  - **鉴权链路**：鉴权在网关、A 服务、B 服务**重复做**——同一份用户/权限查询放大 N 次；
  - **多跳串行**：原来一个事务内的逻辑拆成 A→B→C 串行调用，RT 直接累加；再叠加注册感知、熔断探测、重试的额外耗时；
  - 还有：负载不均的尾延迟、冷启动连接池/缓存未预热。
- **优化手段**：
  - **本地缓存元数据**：配置/字典/权限点近端缓存（Caffeine + 短 TTL 或版本号失效），砍高频小请求；
  - **批量接口**：BFF/聚合层提供 batch 接口，一次带 N 个 id 替代 N 次调用；
  - **并行编排**：无依赖的下游调用并行化（CompletableFuture 全链路超时收敛），总 RT = 最慢支路而非累加；
  - **去冗余跳数**：网关直接路由到真正处理的服务；砍掉纯转发/粘合层；上游直调不层层中转；
  - **鉴权下沉**：网关统一鉴权一次，用户上下文签名透传，下游本地验签不远程查；
  - **传输优化**：内网大流量换二进制协议（protobuf/Dubbo）、HTTP/2 连接复用、响应字段按需裁剪；
  - **缓存与预热**：热点结果缓存、实例启动预热连接池与热点数据。
- **认知兜底**：微服务多出的 RT 换来**独立部署、可用性隔离、团队并行**——目标是把无谓开销砍到 5ms 级，而不是回退到单体。

### 30. 反直觉题：什么项目你会建议不上 Spring Cloud 全家桶

> 原题要点：单体+模块化、直接 K8s Service + 内部 LB 的路线；两条路线的迁移成本对比。

- **建议不上全家桶的场景**：
  - **业务简单/团队小**：3~5 人、少量服务甚至单服务——注册中心、配置中心、治理控制台的运维成本 > 收益；
  - **已深度 K8s 化**：K8s **Service/DNS = 服务发现 + kube-proxy = 负载均衡 + ConfigMap/Secret = 配置 + 探针 = 健康检查与自愈 + Ingress = 网关**——基础设施面已被 K8s 覆盖，再引 Eureka/Nacos 是重复建设；应用层只需补 K8s 不管的：**熔断限流（Resilience4j/Sentinel）、动态配置刷新、分布式链路（OTel）**；
  - **强合规/交付型软件**：要离线交付到客户环境，依赖越少越好部署；
  - **流量模型简单**：同步 CRUD、无明显治理需求——上全家桶属于简历驱动开发。
- **替代路线**：
  - **路线 A：模块化单体**：Maven 多模块按业务域划边界 + 模块间只走显式接口（包可见性/ArchUnit 约束）——需要拆服务时按模块剥出去，成本最低；
  - **路线 B：K8s Native 微服务**：发现/负载均衡交给 K8s，应用只做「HTTP + 熔断 + 可观测」三板斧。
- **迁移成本对比**：
  - **Spring Cloud → K8s 路线**：剥离注册中心（Feign 地址改 Service DNS）、配置迁 ConfigMap、治理组件替换——存量系统改造中等，且越晚越贵；
  - **模块化单体 → 微服务（Cloud 路线）**：单体阶段沉淀的清晰模块接口就是未来 RPC 接口，先拆边界再引基础设施，演进平滑；
  - **微服务 → 单体**：几乎不可逆，成本最高。
- **答题要点**：选型看「**业务复杂度 × 团队规模 × 已有平台**」；能说清「K8s 解决了什么、还剩什么要应用层做」（发现✅/LB✅/熔断❌/配置动态刷新❌/链路❌）是资深信号——**全家桶是手段不是信仰**。

---

## 附：答题口径提醒

- 每道原理题按「**是什么 → 为什么这么设计 → 代价/边界 → 我们项目怎么用、踩过什么坑**」四段展开；
- 场景题先给**排查/设计框架**再填细节，体现方法论而非背题；
- 所有「踩坑故事」要落到**具体配置项或日志现象**（29.5s、85%、read timeout 60s、TIME_WAIT、Read timed out 等），经得起追问。