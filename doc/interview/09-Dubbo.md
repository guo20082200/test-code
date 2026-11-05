# Dubbo 面试题

> 适用岗位：中高级 Java 后端 / RPC 框架方向。考察建议：先确认使用版本（Dubbo2 还是 Dubbo3、是否上 Triple 协议、应用级服务发现），版本不同原理差异巨大；再沿追问链下探。

---

## 一、整体架构（必考）

1. Dubbo 的分层架构讲一遍（Service → Proxy → Registry → Cluster → Protocol → Exchange → Transport → Serialize），每层解决什么问题？为什么要分层？
2. 一次 RPC 调用的完整链路追问链（必考）：
   - 消费端：代理生成 → Invoker 链 → Cluster Invoker（容错）→ Directory（服务目录）→ 负载均衡 → Filter 链 → 协议编码 → Netty 发送；
   - 服务端：Netty 解码 → 线程派发（IO 线程 vs 业务线程池）→ Filter 链 → 反射调用实现类；
   - 每一环都可能出问题：超时发生在哪几环？Filter 链里你能举出哪些内置 Filter（Echo、Timeout、Context、Token、AccessLog……）？
3. 服务暴露与服务引用的启动流程：`export` 时本地暴露（injvm）与远程暴露的区别？Spring 集成下 ServiceBean 的生命周期钩子在哪？
4. SPI 追问链（必考）：
   - JDK SPI 的缺陷（全量实例化、无法按需、无 IOC/AOP）；
   - Dubbo SPI 的 `getExtension(name)` 按需加载、`@SPI` 默认实现、自适应扩展 `@Adaptive` 的字节码生成原理；
   - Wrapper（AOP）与 `@Activate` 条件激活分别在什么场景生效；
   - 你写过 Dubbo 扩展点吗（自定义 Filter/LoadBalance/Protocol），META-INF 文件怎么配，踩过什么坑（key 冲突、类加载）？
5. Dubbo3 的重大演进：
   - 应用级服务发现 vs 接口级服务发现：为什么注册中心数据量从「接口数 × 实例数」降到「应用数 × 实例数」？迁移期双注册怎么平滑？
   - Triple 协议：基于 HTTP/2 + Protobuf，解决什么问题（跨语言、流式、网关友好）？与 gRPC 的关系与差异？

## 二、通信与协议（原理深度）

6. Dubbo 协议为什么设计成「单一长连接 + NIO 异步」？什么流量模型下长连接会成瓶颈（大文件传输、慢消费者），怎么缓解（多连接 `connections`）？
7. 请求-响应怎么在一条连接上对应起来（协议头的 id 字段 + DefaultFuture 异步回调）？`DefaultFuture` 的超时清理与线程泄漏问题？
8. 粘包拆包怎么解决（协议头的长度字段 + 解码器）？Dubbo 协议头 16 字节各字段含义？
9. 线程模型追问链：
   - `Dispatcher` 派发策略（all/direct/message/execution/connection）默认 all 的含义；
   - IO 线程（Netty）与业务线程池的边界：哪些动作必须在 IO 线程做（编码），哪些必须切到业务线程（业务逻辑）？为什么不能反过来？
   - 服务端业务线程池打满的表现（`Thread pool is EXHAUSTED`）与处置。
10. 序列化追问链：hessian2 的默认地位与安全问题、protobuf 的性能优势、跨语言场景怎么选？序列化方式变更怎么灰度（序列化兼容检查、双序列化过渡）？

## 三、注册中心与地址发现

11. 注册中心挂了会影响调用吗（消费端本地缓存服务列表）？什么时候才会真正失败（新实例上下线感知不到）？这个特性对高可用的意义？
12. ZooKeeper / Nacos 作为注册中心的差异：ZK 的临时节点 + Watch 机制、session 超时闪断导致的「服务列表抖动」问题怎么缓解？
13. 服务上下线的优雅性追问链：
    - 优雅停机流程（QOS 命令/kill 信号 → 从注册中心注销 → 已收请求处理完 → 拒新）；
    - 停机期间的流量残留（注册中心推送延迟 + 消费端缓存刷新）怎么兜底（主动通知、下线前置 sleep、网关/SLB 层摘流）？
14. 大规模集群下的地址推送风暴：几千 provider、几万实例的注册中心压力，Dubbo3 应用级发现 + 增量推送怎么解决？你们注册中心的实例规模是多少，出过什么问题？

## 四、集群容错与负载均衡（必考）

15. 集群容错策略追问链：
    - failover（默认，retries=2）/ failfast / failsafe / failback / forking / broadcast 各自的场景；
    - failover 对写接口的危险性（重试导致重复提交）——超时重试的幂等要求怎么在工程上保证（配置约定 + 幂等设计）？
    - 超时时间与重试次数的乘积效应：外层超时必须大于内层（timeout = retries+1 次 × 单次 timeout），链路越深怎么收敛超时预算？
16. 负载均衡策略：Random（默认，按权重）、RoundRobin（平滑加权）、LeastActive、ConsistentHash（用的什么哈希环、虚拟节点数、参数化 hash.arguments）各自的适用场景？你们定制过负载均衡（如同机房优先、单元化路由）吗，怎么实现的？
17. 服务分组（group）、版本（version）、多注册中心的灰度发布方案：接口级灰度 vs 网关级灰度，Dubbo 的 tag 路由怎么实现全链路灰度？
18. 动态流量治理：条件路由规则与标签路由的配置格式与下发链路（配置中心 → 注册中心 → Directory 刷新）？黑名单、读写分离、机房隔离分别怎么配？
19. Mock 与本地伪装：`mock=true` 的降级链路（失败后调用 Mock 实现）与强制 Mock，适合什么场景？与 Sentinel 熔断降级的分工？

## 五、治理与工程实战

20. Dubbo 的 Filter 扩展实战：你用 Filter 做过什么（traceId 透传、附件 attachments 传递、鉴权、监控埋点、统一异常）？跨线程池/异步调用的上下文传递丢失问题怎么解（RpcContext → 异步场景的 Attachments 丢失坑）？
21. 隐式传参：`RpcContext` 的 attachments 与 `setAttachment` 的作用域（单次调用）？为什么不能用它传业务大对象？
22. 泛化调用什么场景用（网关、测试平台、无接口依赖的动态调用）？GenericService 的实现原理与性能代价？
23. 泛化之外：异步调用演进（RpcContext.future → CompletableFuture 签名）、参数回调、事件通知，哪些你真用过？
24. 优雅升级追问链：
    - 接口不兼容变更怎么平滑（新增接口 + version 递增 + 流量切换）？
    - 参数增删的兼容性原则（hessian2 对字段增删的容忍度）？枚举、嵌套对象变更的坑；
    - 你们怎么管理「接口契约」：javadoc、单独的 API 包、IDL（Triple/Protobuf）？
25. Dubbo QOS 运维端口（22222）：ls、invoke、offline、metrics 各干什么？线上紧急摘除一个实例你会怎么做？
26. 监控与排障：
    - `RpcContext` + Filter 自打点 vs Micrometer/Dubbo metrics；
    - 调用偶发超时的排查树：超时预算 → GC 停顿 → 线程池满 → 网络（TCP 重传/连接池）→ 注册中心列表陈旧 → 重试放大；
    - `dubbo.protocol.threads`、`iothreads`、`connections`、`actives`（消费端并发控制）这些参数你是怎么定值的？
27. Dubbo vs Spring Cloud 深度对比追问：RPC 协议性能差异数量级？服务治理能力（路由/熔断/降级）各自怎么补齐？混用场景怎么统一可观测？
28. 架构题：让你为一家新公司设计 RPC 框架选型——Dubbo3（Triple）/ gRPC / Spring Cloud OpenFeign / 自研，评估矩阵怎么列？什么信号出现时你会建议自研？

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 整体架构与 SPI | 25% | 分层职责讲得清、SPI 机制到字节码层、有自定义扩展经历 |
| 通信与线程模型 | 20% | 长连接异步模型、Future 关联、IO/业务线程边界 |
| 注册发现与治理 | 20% | 应用级服务发现、优雅停机流量残留、路由灰度实战 |
| 容错与负载均衡 | 20% | 幂等与重试预算、策略选型理由、超时收敛 |
| 实战与选型 | 15% | 参数定值依据、排障案例、版本演进跟进度 |

**判定要点**：
- **中级**：会用注解搭服务、知道 failover 和注册中心缓存，追问协议头/线程模型就断。
- **高级**：SPI 到源码层、超时重试幂等有真实事故、优雅停机流量残留有方案、参数定值有依据。
- **资深**：跟进 Dubbo3 演进（应用级发现/Triple）、做过流量治理（灰度路由/单元化）、能做 RPC 框架级选型决策。
