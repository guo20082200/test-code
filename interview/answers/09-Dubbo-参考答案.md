# Dubbo 参考答案

> 以下为「要点框架 + 关键细节 + 权衡取舍」，注意区分 Dubbo2 与 Dubbo3 的机制差异，面试需结合个人项目展开。原题见 ../09-Dubbo.md

---

## 一、整体架构（必考）

### 1. 分层架构各层职责与分层意义

> **原题**：Service → Proxy → Registry → Cluster → Protocol → Exchange → Transport → Serialize 每层解决什么问题？为什么要分层？

- Service 层：业务接口/实现的领域模型（ServiceConfig、ReferenceConfig），只负责配置收集与生命周期，不参与 RPC 机制本身；
- Proxy 层：生成消费端 Stub 与服务端 Invoker 代理（Javassist 默认），把「接口方法调用」翻译成「Invoker.invoke(Invocation)」，屏蔽调用方式差异；
- Registry 层：服务注册与发现抽象，对上屏蔽 ZK/Nacos/直连等实现差异（RegistryProtocol 统一包装）；
- Cluster 层：把多个 Provider Invoker 伪装成一个 Cluster Invoker，内含 Directory（动态地址列表）、Router（路由过滤）、LoadBalance（选节点）、容错策略；
- Protocol 层：点对点调用语义（export/refer），一种协议一个 Invoker（dubbo/triple/injvm），负责端口与连接管理；
- Exchange 层：请求-响应语义封装（Request/Response + DefaultFuture），把「单向异步传输」升维成「可同步等待的会话」；
- Transport 层：Netty/Mina 抽象（NIO 收发、编解码器挂载、心跳），不关心消息含义；
- Serialize 层：对象与字节流互转（hessian2/protobuf/fastjson2）。
- 为什么要分层：①每层都是独立 SPI 扩展点，可单独替换（换协议不换集群策略、换注册中心不换协议）；②关注点分离——Proxy 屏蔽远程细节、Cluster 屏蔽多节点细节、Exchange 屏蔽异步细节；③各层只依赖抽象 Invoker/Channel，逐层装饰器式组装，调用链即层叠包装。

### 2. 一次 RPC 调用的完整链路

> **原题**：消费端 Proxy→Cluster→Directory→负载均衡→Filter→协议编码→Netty；服务端解码→线程派发→Filter→反射；超时发生在哪几环？内置 Filter 有哪些？

- 消费端链路：`接口代理（Javassist stub）→ InvokerInvocationHandler → MockClusterInvoker → FailoverClusterInvoker → Directory.list()（RegistryDirectory + RouterChain：MockRouter→TagRouter→条件路由）→ LoadBalance.select() → Filter 链（ConsumerContextFilter→FutureFilter→MonitorFilter…）→ DubboInvoker.doInvoke → DubboCodec 编码 → Netty Channel send`；
- 服务端链路：`Netty IO 线程收到字节 → InternalDecodeHandler 解码（DubboCodec）→ Dispatcher 派发到 Dubbo 业务线程池 → Filter 链（EchoFilter→ClassLoaderFilter→GenericFilter→ContextFilter→TimeoutFilter→MonitorFilter→ExceptionFilter…）→ AbstractProxyInvoker.invoke → Javassist 反射调用实现类 → 结果沿原路编码回写`；
- 超时发生在哪几环：①消费端 DefaultFuture.get(timeout) 等响应超时（最常见，超时点在消费端）；②发送阻塞（TCP 发送缓冲满/慢消费者导致 write 排队）；③服务端业务线程池排队被拒或排队耗时；④服务端 TimeoutFilter 只打 warn 日志不中断执行——关键结论：消费端超时后服务端往往仍在执行，靠重试就产生「影子请求」，必须幂等兜底；
- 内置 Filter 举例：EchoFilter（回声测试）、ClassLoaderFilter（SPI 类加载隔离）、GenericFilter（泛化）、ContextFilter（上下文/attachments 注入清理）、TimeoutFilter、MonitorFilter（埋点）、TokenFilter（防绕过注册中心直连的 token 校验）、AccessLogFilter、ExceptionFilter（服务端异常转字符串，防复杂异常对象反序列化失败与类泄漏）、TpsLimitFilter、ExecuteLimitFilter。

### 3. 服务暴露与服务引用流程

> **原题**：export 时本地暴露（injvm）与远程暴露的区别？Spring 集成下 ServiceBean 生命周期钩子在哪？

- 暴露总流程：ServiceConfig.export() → checkAndUpdateSubConfigs（补齐默认值、收集注册中心、生成 URL）→ 按 scope 依次暴露：
  - 本地暴露（injvm）：InjvmProtocol 把「实现类 Invoker」直接放入本地 exporterMap；同 JVM 引用命中 injvm 时走内存调用——不走网络、不序列化（默认关闭引用传递按值拷贝）；
  - 远程暴露：DubboProtocol.openServer（按 ip:port 复用/新建 NettyServer）→ Invoker 包装为 DubboExporter 存 exporterMap（key=serviceGroup/version/port）→ RegistryProtocol 再包一层：先 export 后 register（注册 URL 到注册中心）并订阅 configurators/routers 动态变更；
- 引用流程：ReferenceConfig.get() → init() → 每个注册中心 refer 得到 Invoker → RegistryAwareClusterInvoker 聚合多注册中心 → Cluster 包装（Mock→Failover）→ ProxyFactory 生成接口代理；
- Spring 集成钩子：Dubbo @Service 解析为 ServiceBean（继承 ServiceConfig），2.x 在 `onApplicationEvent(ContextRefreshedEvent)` 中触发 export（容器 refresh 完成后，避免提前暴露时容器未就绪）；3.x 由 DubboBootstrapApplicationListener 监听 ContextRefreshedEvent 启动 DubboBootstrap 统一 export/refer，容器关闭时 destroy 优雅下线；消费端 ReferenceBean 是 FactoryBean，lazy=true 时首次 get() 才发起引用。

### 4. SPI 追问链

> **原题**：JDK SPI 缺陷；Dubbo SPI 按需加载、@SPI/@Adaptive 字节码原理；Wrapper 与 @Activate；写过扩展点吗？

- JDK SPI 缺陷：ServiceLoader 启动即全量实例化所有实现（无法按名按需加载）；无 IOC（不能注入依赖）、无 AOP（不能包装增强）；实例化失败在迭代时才抛异常难定位；无法根据运行时上下文（URL 参数）选择实现。
- Dubbo SPI：配置路径 `META-INF/dubbo/`（及 dubbo/internal），文件名=接口全限定名，内容 `name=实现类全名`；`getExtensionLoader(接口)` 要求接口必须标注 @SPI，`getExtension(name)` 按名懒加载并缓存（Holder 双重检查）。
- @Adaptive 两种形态：
  - 类级：手工实现的选择器（如 AdaptiveExtensionFactory，注入所有 ExtensionFactory 逐一尝试）；
  - 方法级（重点）：运行时用 Javassist 动态生成 Adaptive 子类并编译加载——生成代码逻辑固定为「从方法入参 URL 中取 key（@Adaptive("key") 指定，未指定则按接口名转参量名，如 Protocol→protocol）得到扩展名 → getExtension(extName) → 转发同名方法」。本质是把「静态绑定的实现选择」延迟为「运行时按 URL 参数决策」，如 Protocol$Adaptive 用 `url.getProtocol()` 决定走 dubbo 还是 triple；
- Wrapper（AOP）：getExtension 时反射扫描「构造函数参数为该 SPI 接口类型」的包装类，按 order 层层包裹返回（如 ProtocolFilterWrapper 给 Invoker 织入 Filter 链、ProtocolListenerWrapper 挂监听）——无需改动实现类即可增强；
- @Activate 条件激活：按 group（provider/consumer）+ value（URL 中该参数非空才激活）+ order 排序，批量自动装配到链上（如 ConsumerContextFilter group=consumer 自动生效），入口是 getActivateExtension(url, key, group)；
- 自定义扩展实战：实现 Filter → 建 `src/main/resources/META-INF/dubbo/org.apache.dubbo.rpc.Filter` 文件 → 写 `traceFilter=com.xxx.TraceFilter`；踩坑清单：①key 与内置/他方扩展重名会覆盖或被覆盖（同 key 全局唯一，受类路径加载顺序影响）；②文件名必须精确为接口全限定名、每行 `key=class`（常见错误：写错包名、漏等号）；③扩展实现必须有公共无参构造；④容器/Tomcat 下类加载器委托问题（扩展类找不到时看线程上下文类加载器）；⑤链上位置用 @Activate(order=) 控制。

### 5. Dubbo3 重大演进

> **原题**：应用级 vs 接口级服务发现（数据量为何下降、迁移期双注册）；Triple 协议解决什么、与 gRPC 关系？

- 接口级发现（Dubbo2）：注册粒度=「接口+group+version」，数据量=接口数×实例数；一个应用暴露几十个接口，实例地址被复制几十份，一次实例上下线触发几十个子节点变更+全量订阅方重建 Directory——注册中心存储、推送、消费端 CPU 三重放大；
- 应用级发现（Dubbo3）：注册粒度=「应用名 → 实例列表（IP+元数据端口+metadata）」，数据量=应用数×实例数（与 Spring Cloud 同构，天然跨生态互通）；「接口→应用」映射存在元数据中心（MetadataReport）或由订阅方按应用聚合；实例级接口元数据由实例内 MetadataService 提供（local/remote 两种取法）；
- 迁移期双注册：provider 端 `dubbo.registry.register-mode = interface | instance | all`，all 即双注册；consumer 端通过 migration 规则切流（FORCE_INTERFACE 走老、FORCE_APPLICATION 走新、APPLICATION_FIRST 优先应用级失败回退接口级，3.x 默认），规则由配置中心下发、可按应用/按服务灰度，新老消费者共存平滑；
- Triple 协议：Dubbo3 默认协议，HTTP/2 传输+多序列化（Protobuf/hessian2），解决三个问题：①跨语言互通（标准 HTTP/2，不再依赖私有 16 字节头）；②流式通信（unary / server / client / bidirectional streaming）；③网关与基础设施友好（L7 网关、mesh、监控可直接解析）；
- 与 gRPC 关系：Triple 兼容 gRPC 协议（gRPC 客户端可直接调用 Triple 服务）；差异：Triple 不强绑 Protobuf IDL——可直接用 Java 接口+任意序列化（Dubbo 模式），也支持 gRPC 模式；服务元数据放 HTTP/2 头，注册模型沿用 Dubbo 生态。

---

## 二、通信与协议（原理深度）

### 6. 单一长连接 + NIO 异步设计

> **原题**：为什么单一长连接+异步？什么流量模型下成瓶颈？connections 怎么缓解？

- 设计动机：RPC 请求「小报文、高频次」——单长连接省去每次建连开销（三次握手/证书协商）与连接管理成本；NIO 异步让一个 IO 线程服务多连接、单连接上请求异步并发（id 关联+Future 回调），不因等待响应阻塞；
- 默认行为：consumer 对同一 provider ip:port 默认共享 1 条长连接（connections=0），心跳默认 60s 保活，断线重连；
- 瓶颈流量模型：①大报文/大文件——单连接发送串行入队，大包占满发送缓冲导致后续小请求排队（同连接队头阻塞），且默认 payload 上限 8MB；②慢消费者——服务端响应写不出去，发送缓冲积压、内存上涨，还拖累同连接其他请求；③超高频大吞吐时单连接序列化+socket 吞吐饱和；
- 缓解：consumer 端 `connections=N` 建多条长连接分摊（请求按连接数取模分发，每条独立 Channel 与发送缓冲）；大文件不走 RPC（对象存储/分片/专用传输）；大报文压到 KB 级+压缩；配合 iothreads/收发缓冲调优与压测验证。

### 7. 请求-响应关联：协议头 id + DefaultFuture

> **原题**：一条连接上怎么对应请求响应？DefaultFuture 超时清理与线程泄漏？

- 关联机制：每个 Request 分配全局自增 id（8 字节 long 写入协议头），发送前 `DefaultFuture.newFuture()` 把 `id → DefaultFuture` 放入静态 ConcurrentHashMap FUTURES；
- 响应到达：IO 线程解码出 Response 后按 `response.getId()` 从 FUTURES 取出 future 并 complete——单连接多请求并发靠 id 复用路由，等待线程 `future.get(timeout)` 返回或走 CompletableFuture 回调；
- 超时清理：创建 future 时向 HashedWheelTimer（时间轮）注册超时检查任务，到期将 future 以 TimeoutException 完成、从 FUTURES 移除；晚到的响应查不到 future 直接丢弃并打 warn（防旧响应污染新请求）；
- 断连兜底：channel 关闭时 DefaultFuture.closeChannel 把该 channel 上所有 pending future 以异常完成（否则只能等时间轮超时兜底）；
- 泄漏风险与工程监控：①get 不设超时→调用线程永久挂起（Dubbo 默认必有 timeout）；②服务端处理成功但响应丢失/进程假死→pending future 堆积到超时；③历史版本断连清理不全导致 FUTURES 积压——工程上对 FUTURES.size()（在途请求数）做监控，异常增长即报警。

### 8. 粘包拆包与 16 字节协议头

> **原题**：粘包拆包怎么解决？协议头 16 字节各字段含义？

- TCP 是无边界字节流，Dubbo 用「定长头+变长体」：ExchangeCodec/DubboCodec 先读 16 字节头（字节不足则继续累积等待），读出 body length 后判断缓冲是否够整个 body，够了才截取完整报文解码（Netty ByteToMessageDecoder 累积缓冲语义，等价于 LengthFieldBasedFrame 思路的自定义实现）；
- 16 字节头逐字段：
  - 0~1 字节：魔法数 0xdabb（标识 Dubbo 报文，快速过滤非法流量）；
  - 第 2 字节：标志位——bit7 Req/Res（1 请求/0 响应）、bit6 2way（是否需要响应）、bit5 Event（事件如心跳）、bit0~4 序列化类型 id（hessian2=2、protobuf 等）；
  - 第 3 字节：响应状态码（仅响应报文：20=OK、25=客户端超时、31=服务端超时、40=BAD_REQUEST、50=BAD_RESPONSE、80=SERVER_ERROR 等）；
  - 4~11 字节：请求 id（8 字节 long，请求响应关联键）；
  - 12~15 字节：body 长度（4 字节 int，默认限制 8MB）；
- body：hessian2 序列化的请求（版本、路径、方法名、参数类型、实参、attachments）/响应（结果或异常）。

### 9. 线程模型

> **原题**：Dispatcher 派发策略与默认 all；IO 线程与业务线程池边界、为何不能反；EXHAUSTED 表现与处置？

- Dispatcher SPI（派发策略）：
  - all（默认）：所有消息（请求/响应/连接/断连/心跳）都派发到业务线程池；
  - direct：全部留在 IO 线程执行，不切线程；
  - message：仅请求/响应派线程池，连接事件留 IO 线程；
  - execution：仅「请求」派线程池，响应等事件留 IO 线程（消费端响应回填常用，避免占业务池）；
  - connection：连接/断连事件放线程池，请求排队；
- IO 线程 vs 业务线程边界：IO 线程只做「快且必要」——编解码、协议帧、心跳、连接管理；Filter 链后的业务反射调用必须切业务线程池。不能反的原因：业务逻辑耗时/阻塞若占用 IO 线程，同一 EventLoopGroup 上的所有连接、心跳、编解码全部停摆，表现为连接假死、心跳超时、断连雪崩——IO 线程是全局公共资源，业务线程池是可隔离、可降级、可拒绝的私有资源；
- 服务端业务线程池打满：默认 fixed 200 + SynchronousQueue（queues=0 不排队），打满即拒绝并打印 `Thread pool is EXHAUSTED`，消费端表现为超时/ServerBusy 失败；处置顺序：①限流止血（actives/executes/TpsLimit）；②定位根因——慢调用（慢 SQL/下游阻塞/锁竞争）、GC、突发流量，jstack 看线程栈堆积在哪；③扩容/拆分服务隔离故障；④调大 threads 是最后手段（上下文切换+内存代价），queues 调大只是把拒绝变成延迟恶化，同样慎用。

### 10. 序列化追问链

> **原题**：hessian2 默认地位与安全、protobuf 优势、跨语言选型、序列化变更怎么灰度？

- hessian2：Dubbo2 默认——自描述（携带类名/字段名，两端无需同版本 schema 即可解）、跨语言可读、体积与性能均衡；安全问题：属于反序列化漏洞重灾区家族（任意类构造 gadget），Dubbo 的对策是 SerializeCheck（类白名单/黑名单，3.x 默认严格模式只放行受信包）、禁用 JDK 原生序列化、`dubbo.application.serialize-check-status` 控制严格程度；
- protobuf：紧凑（Varint、字段编号）、编解码快（无 Java 反射元数据开销）、强 schema 适合跨语言与流式；代价是要维护 proto、Java 侧多一层对象转换；
- 跨语言选型：多语言/网关/移动端 → protobuf（Triple）；纯 Java 内网 → hessian2/fastjson2；极致性能且纯 Java → kryo/fst（注意类注册与安全）；禁用原生 JDK 序列化；
- 序列化变更灰度：①协议头低 5 位携带 serialization id，服务端按报文自适应解码——天然支持「服务端多序列化并存」，过渡期双序列化（3.x `prefer-serialization`）；②按消费方逐个切 serialization 参数灰度，观察错误率与兼容检查告警；③切换前用兼容性检查/预发流量回放验证对象兼容；④监控按序列化类型打点，确认旧序列化零流量后再下线。

---

## 三、注册中心与地址发现

### 11. 注册中心挂了影响调用吗

> **原题**：消费端本地缓存服务列表？什么时候才真正失败？对高可用的意义？

- 存量调用不受影响：消费端订阅后把 Provider 列表缓存在内存（RegistryDirectory 的 urlInvokerMap），并持久化到本地文件缓存（`~/.dubbo/dubbo-registry-*.cache`），注册中心宕机后重启的消费端还能从文件恢复列表继续调用；
- 真正失败的场景：①故障期间新实例扩容无法被发现（容量不能弹性增长）；②实例下线/摘除无法推送——不过进程真实挂掉时 TCP 连接断开，消费端能感知断连并重建 Directory，所以纯宕机影响有限，关键是「弹性扩容」这一路彻底断了；
- 高可用意义：注册中心被移出「调用关键路径」，只承担元数据一致性的最终同步——注册中心故障=降级为静态配置而不是全站不可用；配合注册中心集群多副本+多注册中心，把 RTO 控制在分钟级；这是 Dubbo 可用性设计的核心（去中心化依赖）。注意：动态路由/override 规则也依赖注册中心下发，宕机期间规则变更同样失效。

### 12. ZooKeeper vs Nacos 注册中心

> **原题**：两者差异；ZK 临时节点+Watch；session 超时闪断导致的「服务列表抖动」怎么缓解？

- ZK：ZAB 强一致；服务注册=临时节点（生命周期绑定 session），变更通知=Watch（一次性触发需重注册，Dubbo 用 ChildrenCache 包装并全量拉取对比）；强一致代价是可用性——leader 选举期间不可写，但 Dubbo 本来就不依赖注册中心的实时性（见上题），可容忍；
- session 闪断抖动：网络抖动/长 GC 超过 sessionTimeout → ZK 判定会话过期、删除该客户端注册的所有临时节点 → 全网视角该 provider「下线」→ 消费端摘流 → 客户端重连重建会话又注册回来 → 列表来回跳，流量震荡、Directory 频繁重建，极端时瞬时全下线造成可用率毛刺；
- 缓解：①调大 sessionTimeout（Curator 默认 60s）+客户端自动重连与重试策略；②治理抖动源：注册中心客户端所在机器的 GC 停顿、网络抖动专项排查；③Dubbo 侧延迟删除/列表校验（避免一次闪断立刻清空）；④换 Nacos：心跳保活（15s 不健康标记、30s 剔除，可配）+保护阈值（不健康实例占比超阈值时宁可返回全部实例也不摘成 0——牺牲一致性保可用，防雪崩）；⑤大规模场景评估 Nacos AP 模型或 Dubbo3 应用级发现，从根本上降低注册中心写与推送压力。

### 13. 服务上下线的优雅性

> **原题**：优雅停机流程；停机期间流量残留（推送延迟+缓存刷新）怎么兜底？

- 优雅停机标准流程：①收到 SIGTERM / QOS `shutdown` → ②先从注册中心注销（新流量不再分进来）→ ③等待在途请求处理完（shutdown.wait 预算内）→ ④拒新（关闭监听/线程池）、释放资源；消费端镜像操作：停发新请求并等自己在途请求返回再销毁；
- 流量残留根因：注册中心摘除是「异步传播」——ZK watch 触发→消费端重拉→Directory 重建 Invoker，链路耗时秒级到十几秒；期间消费端本地缓存仍把请求发到停机实例；
- 多层兜底：
  - 主动摘除+延迟退出：停机脚本先 QOS `offline`（或调内部接口注销），sleep 覆盖推送延迟（10~30s，压测标定），再 kill；
  - Provider 拒流兜底：停机中收到的新请求返回 Shutdown 类异常，消费端 failover 重试到健康实例（要求该接口可重试/幂等，读接口最合适）；
  - 网关/SLB 层摘流：南北向先从 LB 摘除后端再停应用；K8s 用 readiness 置失败+preStop sleep 顺序（endpoint 摘除→sleep→SIGTERM），terminationGracePeriodSeconds 覆盖最长在途请求；
  - mesh 场景由 sidecar 做 drain；连接层 FIN/RST 断开感知作为最后防线；
- 面试要点：说明「注册中心推送延迟不可消除，只能用多道防线兜住」，并给出你们项目的具体 sleep 与在途请求等待时长依据。

### 14. 大规模集群下的地址推送风暴

> **原题**：几千 provider、几万实例的注册中心压力；Dubbo3 应用级发现+增量推送怎么解决？你们的规模与踩坑？

- 接口级模式放大链：实例上下线→该应用所有接口的注册节点变更→订阅这些接口的所有消费端全量重拉+重建 Directory（解析 URL、建连），复杂度 O(接口数×订阅方数)；大促批量扩容时形成推送风暴压垮 ZK（写抖动、watch 积压、快照膨胀），消费端重建本身也是 CPU 热点；
- 应用级发现：粒度降到应用（O(应用数×订阅方数)），通常降 1~2 个数量级；接口元数据外移到 MetadataReport，注册中心只剩实例列表；
- 增量推送：3.x 对实例列表变更只推增量（delta），消费端本地合并——万级实例时全量单条消息数 MB，增量显著降低网络与解析开销；变更处理从「全量重建」变为「局部合并」；
- 工程实践参考：①注册中心按业务域分集群隔离，避免全局风暴；②扩容分批、错峰注册；③监控注册中心写入 QPS、watch 积压、消费端 Directory 重建耗时；④单集群超过数千 provider/数万实例时优先推进应用级迁移（写迁移双注册方案与切流节奏）。

---

## 四、集群容错与负载均衡（必考）

### 15. 集群容错策略与超时预算

> **原题**：failover/failfast/failsafe/failback/forking/broadcast 场景；failover 写接口重复提交、幂等保障；timeout×retries 乘积与预算收敛？

- 六种策略与场景：
  - failover（默认，retries=2，共最多 3 次）：失败自动切换其他实例，适合读/幂等接口；重试时跳过已失败节点；仅对 RPC 层异常（超时、无可用节点等）重试，业务异常不重试；
  - failfast：只调一次快速失败，适合非幂等写（失败立即抛，避免调用方误判后再补一次）；
  - failsafe：失败忽略只记日志，旁路操作（审计日志、打点上报）；
  - failback：失败先记录、后台定时重发，消息通知类最终一致场景（注意本身会产生重复）；
  - forking：并行调 N 台（forks）取最先成功，实时性优先、成本不敏感的读（QPS 放大 N 倍）；
  - broadcast：逐台全调、异常不中断、最后汇总，刷缓存/全员通知类；
- failover 写接口危险性：超时≠失败——服务端可能已成功执行、响应丢失；重试=重复提交（扣款/库存事故）；工程保障双管齐下：
  - 配置约定：写接口显式 retries=0（团队规约+评审+扫描工具检查），或接口注解声明幂等属性由框架自动装配容错策略；
  - 幂等设计：唯一业务 ID+去重表/数据库唯一键/Redis setnx 防重、乐观锁版本号、状态机合法跃迁校验、资金类用对账兜底；
- 超时乘积与收敛：接口总耗时上限=(retries+1)×单次 timeout；外层超时必须>内层总预算，否则内层重试结果还没返回外层已放弃——白重试还放大流量；收敛方法：①画「超时预算树」：网关 3s → 服务A 2s → 依赖B 500ms×2=1s，逐层递减留余量并评审；②重试预算（retry budget）：全局限制可重试请求比例（如≤10%），防止大面积故障时 failover 把流量放大 3 倍加速雪崩（可在自定义 Filter/Cluster 扩展实现计数熔断）；
- 附加细节：重试与负载均衡联动（重新 select，sticky 除外）；Dubbo 原生重试无退避间隔，可扩展。

### 16. 负载均衡策略与定制

> **原题**：Random/RoundRobin/LeastActive/ConsistentHash（哈希环、虚拟节点、hash.arguments）场景；定制负载均衡（同机房优先/单元化）怎么实现？

- Random（默认）：按权重随机，长期统计趋近权重比；无状态实现最简单；短窗口可能倾斜；绝大多数场景够用；
- RoundRobin：平滑加权轮询（Smooth Weighted RR，Nginx 同款）：当前权重逐步累加、选中后扣减总权重——分布均匀且不连续打同一台；有状态（按服务维护计数）；对慢实例无感知，怕积压；
- LeastActive：活跃数（在途请求数，ActiveLimitFilter/ExecuteLimitFilter 统计）最少者优先，同活跃按权重随机——天然自适应慢实例（慢的积压活跃数升高被绕开），适合耗时差异大的服务；
- ConsistentHash：TreeMap 实现哈希环，默认每实例 160 个虚拟节点（`hash.nodes`），相同参数固定路由到同一实例（粘性会话/本地缓存命中）；`hash.arguments` 参数化 hash key（默认第 0 个参数，可配 "0,1" 取多个参数组合）；节点增减只影响相邻区间（有界漂移），但实例上下线仍会造成部分缓存 miss；注意热参数倾斜与单实例热点；
- 定制实现：优先做 Router 而不是 LoadBalance——Router 管「选地址集合」（过滤/排序），LoadBalance 管集合里「挑一个」：
  - 同机房优先：自定义 Router 按 attachment/环境变量取 zone 标，优先返回同 zone Invoker，同 zone 空时降级全量（防同机房整体故障无服务），或直接用 3.x 内置 zone-aware/mesh 路由；
  - 单元化路由：把「单元标识」作为路由维度，从入口网关染色（tag/attachment）→ 每跳 Router 按 attachment 匹配单元实例 → 数据层同单元绑定，实现封闭单元；
  - 自定义 LoadBalance 则实现 LoadBalance SPI+注册 META-INF，在 select 里先过滤/分组候选列表再套内置策略（如先同机房分组、组内 LeastActive）。

### 17. 灰度发布与 tag 路由全链路灰度

> **原题**：group/version/多注册中心灰度方案；接口级灰度 vs 网关级灰度；tag 路由怎么实现全链路灰度？

- 基础隔离：group（同接口逻辑分组）、version（不兼容版本并存：新服务 2.0 注册，消费端逐个切引用版本，稳定后下线 1.0）；多注册中心用于机房级/体系级并存切流；
- 接口级灰度：在 Dubbo RPC 层内灰度（version/group/tag 控制），粒度=接口，配置繁琐、比例静态，适合少量接口变更；
- 网关级灰度：入口按用户/设备特征（灰度名单、百分比、地域）把流量导入「灰度泳道」应用集群，随后的 RPC 依赖靠标签透传继续走灰度实例——即全链路灰度；
- tag 路由全链路实现链路：
  1. Provider 打标：启动参数 `dubbo.provider.tag=gray`（或环境变量），注册的地址带 tag；
  2. 流量染色：网关把灰度标识写入 HTTP header → 入口 Filter 转 `RpcContext.setAttachment("dubbo.tag","gray")`；
  3. 消费端 TagRouter 过滤：优先同 tag 实例；无同 tag 实例时按规则 `force` 字段决定——force=true 报无可用服务（强隔离），force=false 降级到默认（无 tag）实例（保可用）；
  4. 每一跳服务的 Filter 从 attachment 取出 tag 继续透传 → 全链路泳道闭环；规则由配置中心下发（应用级 tag 路由规则，支持按 key=value 匹配自动打标）；
- 踩坑：tag 透传到异步/线程池场景会丢（见 20 题）；灰度实例容量不足时的降级策略要明确；「灰度隔离」若下游还有 DB/缓存，则必须升级为单元化（数据隔离），仅路由层隔离不够。

### 18. 动态流量治理：条件路由与标签路由

> **原题**：条件路由/标签路由配置格式与下发链路（配置中心→注册中心→Directory 刷新）？黑名单、读写分离、机房隔离怎么配？

- 下发链路：治理控制台（Dubbo Admin）写规则 → 配置中心（3.x：Nacos/Apollo 的路由规则配置；2.x：ZK 节点 `/dubbo/{service}/configurators|routers`）→ 订阅通知推给消费端 → RouterFactory 实例化 Router 加入 RouterChain → Directory.list() 时依次过滤。关键理解：**路由规则在消费端本地执行**，注册中心只是通知通道；
- 条件路由（2.x 字符串规则，左边匹配 consumer、右边过滤 provider；3.x 为应用级 YAML）：
  - 黑名单：禁某台消费方 `host = 10.20.1.3 =>`（右侧为空=不提供任何 provider）；屏蔽某台 provider `=> host != 10.20.1.2`（全员不路由到该实例）；
  - 读写分离：`method = find*,get*,query* => host = 10.20.1.10,10.20.1.11`（读走读集群）、`method != find*,get*,query* => host = 10.20.1.20,10.20.1.21`（写走主集群）；
  - 机房隔离：按实例的 env/zone 参数匹配 `=> env = same-idc`，或直接用 tag 路由/zone-aware（3.x 推荐）；
  - 另一类是 configurator（override 规则）：改的是配置参数（如强制某服务 timeout、`weight=0` 动态下线），与 router（改地址集合）职责区分清楚；
- 标签路由规则（应用级 JSON/YAML）：`force` + `tags:[{name:gray, match:[{key:env,value:gray}]}, {name:default}]`——按请求 attachment/参数自动匹配实例标签；
- 变更安全：规则先在预发验证、支持回滚、控制台操作留痕；错误路由规则可能把服务过滤成无可用地址（Router 链为空），需要灰度下发与紧急恢复机制。

### 19. Mock 与本地伪装

> **原题**：mock=true 降级链路与强制 Mock 的场景？与 Sentinel 熔断降级的分工？

- 配置语义：`mock=true` 时找「接口名+Mock」约定类（XxxServiceMock）；`mock=fail:return null/值` 失败后返回；`mock=fail:throw 异常`；`mock=force:return null` 强制 Mock（不发起远程调用，用于屏蔽故障/演练预案）；也可指向自定义类全名；
- 降级链路：Cluster 层最外层是 MockClusterInvoker.invoke() → ①invocation 携带 force mock 标记 → 直接走 MockInvoker；②正常走 FailoverClusterInvoker → 抛 RpcException 且配置了 fail mock → 转入 MockInvoker 返回降级结果（本地执行 Mock 对象，无网络开销）；
- 适用场景：依赖抖动时对非核心接口返回默认值保护上游、外部收费/三方接口在降级预案中屏蔽、联调期依赖未就绪；
- 与 Sentinel 分工：Sentinel 熔断是「事前/事中控制」——慢调用比例/异常比例/异常数超阈值自动断路、半开探测恢复，外加流控、热点、系统自适应保护，规则动态下发、有监控大盘；Dubbo Mock 是「事后兜底」——失败后的静态返回，无阈值触发与恢复探测。实践组合：Sentinel 前置熔断减少无效流量与线程占用 → 兜底值用 Mock/业务降级逻辑给出有意义结果（如推荐列表降级为热门榜单），两者互补不替代；核心链路的降级值要有业务语义评审。

---

## 五、治理与工程实战

### 20. Filter 扩展实战与上下文传递

> **原题**：用 Filter 做过什么（traceId/attachments/鉴权/埋点/统一异常）？跨线程池/异步调用的上下文丢失怎么解？

- 典型实战（按 filter 双端职责讲）：
  - traceId 透传：consumer 端 Filter 取网关生成的 traceId 写入 attachments，provider 端 Filter 取出放 MDC（日志）+透传给下游，调用出口清理——这是「跨服务日志串联」的底座；
  - 鉴权：provider 端校验 token/签名（内置 TokenFilter 只防绕过注册中心直连，业务鉴权自定义），失败直接抛出不上钻业务；
  - 监控埋点：参考 MonitorFilter 原理（调用前后计时、按 service+method 维度计数），上报 Micrometer/Prometheus（P99、QPS、错误率、按机房标签）；
  - 统一异常：参照 ExceptionFilter——服务端把未声明异常转为携带字符串信息的约定异常返回，避免复杂异常对象跨端反序列化失败与内部类名泄漏；
  - 染色：入口 Filter 把 HTTP header（灰度标、压测标）转为 attachment 供 Router/监控使用；
- 上下文丢失问题与解法：RpcContext 基于 ThreadLocal，三处必丢——①业务自建线程池里发起第二次 RPC（attachments 不跨线程复制）；②CompletableFuture 回调线程已不是发起线程，RpcContext 可能被复用/清空；③参数回调/事件通知线程；
  - 解法：提交任务前显式快照（capture attachments/trace 上下文），任务内先 restore 再发调用——封装统一的 Runnable/Callable 装饰器强制收口；或引入 transmittable-thread-local（TTL）包装线程池自动透传；规范约定 setAttachment 必须发生在「真正发起调用的线程」，用测试用例固化（线程池内二次调用场景回归）。

### 21. RpcContext 隐式传参

> **原题**：attachments 与 setAttachment 的作用域？为什么不能传业务大对象？

- 作用域：`RpcContext.setAttachment` 只对**下一次** RPC 调用生效——消费端写入→序列化进请求 attachments→服务端 ContextFilter 取出放入自己的 RpcContext（getAttachment）；provider 侧要回传数据用响应 attachments（2.x setAttachment；3.x RpcContext.getServerContext），单次调用结束即清理；线程绑定，链式多跳需每跳透传；
- 不能传大对象的原因：①attachments 与方法参数一起整体序列化，大对象直接撑爆报文（默认 payload 8MB 上限）并挤占发送缓冲；②每次调用都附带→网络/序列化 CPU 开销 N 倍放大；③attachments 定位是「控制面元数据」（trace、tag、鉴权、路由染色、压测标），不是「数据面」——传大对象属于职责错用，正确姿势是方法参数、分布式缓存/对象存储+引用传递；④mesh/网关转发场景 attachment 还会被中间层再复制，放大内存压力。

### 22. 泛化调用

> **原题**：什么场景用（网关/测试平台/无接口依赖）？GenericService 实现原理与性能代价？

- 场景：API 网关统一代理 Dubbo 服务（不可能依赖全公司接口 jar）、测试平台/自动化用例动态构造参数调用任意方法、内部调试工具（QOS 的 invoke 命令就是泛化的封装）、跨团队无契约试探调用；
- 用法：ReferenceConfig.setGeneric(true) → 拿 GenericService → `$invoke("方法名", 参数类型数组, 参数值数组)`，POJO 用 Map 表示（带 class 字段标类型）；
- 原理：消费端 GenericImplFilter 拦截 $invoke 并标记泛化调用；服务端 GenericFilter 识别后把 Map 通过 PojoUtils.realize 反射还原成真实 POJO（按参数类型逐字段构造），再进入正常服务端调用链——消费端全程不需要加载接口类与参数类，类型还原发生在服务端；
- 性能代价：①Map↔POJO 双向反射转换，比原生序列化+直接调用慢数倍（网关场景会缓存 Method/字段元数据缓解）；②参数类型校验弱化，错误运行时才暴露；③无编译期检查与 IDE 支持；工程约束：只允许网关/平台层使用，业务代码禁用，并对泛化调用量与耗时单独监控。

### 23. 异步调用演进

> **原题**：RpcContext.future → CompletableFuture 签名、参数回调、事件通知，哪些真用过？

- 演进脉络：
  - Dubbo2 早期：`RpcContext.getContext().asyncCall()` / 配置 async=true 后 getFuture——同步签名下的伪异步（已废弃）；
  - 参数回调：方法最后一个参数是回调接口（`void hello(String msg, Callback cb)`），consumer 传匿名实现，provider 持有反向调用——本质是把 consumer 侧一个代理对象经 attachments 传给服务端回调；约束：依赖连接存活、连接断开回调失效，工程慎用；
  - 事件通知：方法级配置 oninvoke/onreturn/onthrow 指定本地回调方法（同步时机通知，适合埋点/审计，已被 Filter+指标替代）；
  - Dubbo2.7+ 正解：方法签名直接声明返回 `CompletableFuture<T>`——消费端拿到 future 做非阻塞编排（thenCompose 组合多依赖、allOf 聚合）；服务端也可返回 CompletableFuture 开启「服务端异步」——业务线程立刻归还线程池，结果就绪后回调回写响应，对长轮询、慢 IO 场景线程池吞吐提升明显；
- 实践建议：高并发多下游聚合用 CompletableFuture 组合；服务端 async 注意回调线程里 RpcContext 不可用、上下文需提前捕获；参数回调/事件通知仅遗留场景维护。

### 24. 优雅升级追问链

> **原题**：接口不兼容变更怎么平滑？参数增删兼容原则、枚举/嵌套对象坑？接口契约怎么管理？

- 不兼容变更平滑：①原则「只加不改不删」——新增方法或新接口（XxxServiceV2）而非原地改签名；②双版本并存：provider 同时发布 version=1.0/2.0 两个 ServiceConfig，消费端按应用灰度切引用版本；③观察双版本流量与错误率后下线 1.0（先停止写流量再注销）；④配合 tag 路由/条件路由做灰度与回滚预案；⑤过渡期方法内参数分叉要有清理排期；
- hessian2 兼容原则：按字段名（非顺序）序列化——**增字段**双向兼容（旧端忽略新字段，新端对缺字段给默认值）；删字段先停写滚动删除；字段类型变更=不兼容；数值用包装类型避免默认值歧义；
- 高危坑：①枚举：hessian2 按名字序列化，删除/重命名枚举项后对端遇到未知名抛异常——规约「枚举只加不改不删」，未知值端上兜底（或迁移 int+映射表，牺牲可读性）；②嵌套对象：内层类包名/类名重构（IDE rename）=对端反序列化 ClassNotFoundException——**类全名是契约的一部分**，禁止随意改包；③跨序列化迁移（hessian2→protobuf）时字段一一映射验证，日期/BigDecimal 等类型逐个比对；
- 契约管理：①独立 API 包（接口+DTO+常量，不依赖实现），semver 管理、发内部仓库，消费端禁止 copy 类；②javadoc+单测固化行为契约，@Deprecated 标注过渡；③上 Triple 后以 proto IDL 为唯一契约：proto 评审流程+CI 兼容性检查（buf breaking：字段编号不可改、只增不删）+代码生成。

### 25. Dubbo QOS 运维

> **原题**：QOS 端口 22222 的 ls/invoke/offline/metrics 各干什么？线上紧急摘除一个实例怎么做？

- QOS（22222 端口，telnet/HTTP；生产必须内网隔离+鉴权，3.x 默认拒绝匿名修改操作）：
  - ls：列出本应用暴露/引用的服务明细（`ls xxService` 看 provider/consumer 维度）——快速确认服务是否注册成功；
  - invoke：本地发起一次泛化调用验证服务可用性（`invoke XxxService.getMethod({"k":1})`）——线上探活/联调利器；
  - offline：把某服务从注册中心临时注销（进程不停），`online` 恢复——单机摘流/隔离问题实例；
  - metrics：输出内置指标（qps/rt/错误率等），也可接 Prometheus 拉取；
  - 其他：shutdown（触发优雅停机）、version、status/ready/live 健康检查、enableRouter/disableRouter 临时禁用路由；
- 紧急摘除 SOP：①定位问题实例 IP 与影响面；②优先基础设施层摘流：K8s readiness 失败/LB 摘后端（覆盖南北向）；③Dubbo 层：telnet 22222 → `offline <service>`（或控制台下发 disable）→ 注册中心摘除 → 消费端秒级~十秒级感知；④进程假死 QOS 无响应→直接网络层断流（iptables/安全组）或强制杀，依赖消费端断连感知+超时重试到其他实例兜底；⑤摘除后保留现场（jstack/jmap/gc log）再处理；⑥恢复用 online；注意 offline 是服务级临时注销、重启自动回归；
- 边界提醒：注册中心不可用时 offline 不会传播（回到缓存调用状态），必须走网络层手段。

### 26. 监控与排障

> **原题**：自打点 vs Micrometer/metrics；偶发超时排查树；threads/iothreads/connections/actives 怎么定值？

- 打点方案：自定义 Filter+RpcContext 灵活但侵入、维度自己维护；Dubbo3 内置 metrics（Micrometer 门面，application/service/method 维度）+Prometheus/Grafana 是标配。推荐：内置 metrics 覆盖标准指标，Filter 补业务自定义维度（机房 tag、降级计数），注意标签基数控制（method 可接受，禁止参数值作标签）；
- 偶发超时排查树（从消费端报错出发逐层下钻）：
  1. 分类报错：`Waiting server-side response timeout`（服务端没回）/`client-side timeout`（发送层阻塞）/EXHAUSTED/连接拒绝——不同分支不同根因方向；
  2. 超时预算核对：本接口 timeout 与 retries 配置、上游总预算是否覆盖「(retries+1)×timeout」——排除链路乘法放大与配置漂移；
  3. 服务端视角：同接口 RT 分布（P99/P999 与 avg 的离散度）、时间点对齐 GC 日志（Young GC 突刺是偶发超时头号嫌疑人）；
  4. 线程池：业务线程池活跃数/队列、EXHAUSTED 日志；jstack 看线程堆积卡在哪（下游慢的连锁反应）；
  5. 网络层：TCP 重传率（netstat -s）、带宽打满、单连接排队（connections 不足）、K8s conntrack/SNAT 端口耗尽；
  6. 地址陈旧：确认失败实例是否早已下线（注册中心推送延迟/列表陈旧）；
  7. 重试放大：故障窗口 QPS 是否同比放大 retries+1 倍（failover 在故障期的反作用）；
  8. 常见「偶发」实锤：高峰 GC、慢 SQL 突刺、连接池耗尽、Full GC、CPU 限流（CFS throttling）、ConsistentHash 热 key 倾斜；
- 参数定值依据：
  - `dubbo.protocol.threads`（默认 fixed 200）：Little's Law 定容量——所需并发=目标 QPS×平均 RT（如 200 QPS×50ms=10 并发即可承载，200 的默认值对 50ms 接口理论扛 4000 QPS）；最终以压测定，线程过多上下文切换反而推高 RT；下游阻塞优先治理下游而不是加线程；
  - `iothreads`（默认 CPU+1）：只干编解码，跟核数走，一般不动；仅大报文编解码过重时评估；
  - `connections`（默认 0=单连接）：常规接口不需要调；单连接吞吐饱和（发送缓冲排队/序列化打满）的高吞吐或大报文场景设 2~4，压测观察单连接带宽收益；
  - `actives`（消费端每服务每方法最大并发，默认不限）：客户端限流保护自己与服务端，定值=服务端该接口容量÷消费端实例数；与服务端 executes（单方法服务端并发上限）、TpsLimitFilter 组成「消费端自治+服务端兜底」两层限流。

### 27. Dubbo vs Spring Cloud 深度对比

> **原题**：RPC 协议性能差异数量级？服务治理能力各自怎么补齐？混用怎么统一可观测？

- 性能：Dubbo 私有二进制协议（16B 头+紧凑序列化+单连接多路复用）对比 OpenFeign（HTTP/1.1+JSON+连接池）在低 RT 高频接口上常见 1 个数量级的吞吐差距（文本解析、无多路复用、JSON 反射开销）；HTTP/2 系（Triple/gRPC）靠多路复用大幅缩小差距，剩余差距主要在序列化（Protobuf 对 JSON 的数倍）；工程判断：多数业务瓶颈在 DB/下游，协议差异只在超高频低 RT 场景才构成选型决定因素；
- 治理能力对比与互补：
  - Dubbo：路由规则、tag 灰度、负载均衡、集群容错、隐式传参、QOS 内建于框架，Java 生态治理最强；
  - Spring Cloud：契约=HTTP，治理靠组件拼装（Nacos/Eureka 发现、LoadBalancer、Resilience4j/Sentinel 熔断、Gateway、Config），优势是语言无关与生态广度；
  - 互补路径：Dubbo 补熔断限流→接 Sentinel/Resilience4j；SC 补细粒度路由灰度→上 Istio（对 HTTP 原生友好）或迁 Dubbo3（应用级发现对齐 Spring Cloud 模型、Triple 兼容 HTTP 生态）；
- 混用统一可观测：①以 OpenTelemetry/W3C tracecontext 为统一标准，Java agent（OTel/SkyWalking）字节码插桩同时覆盖 Dubbo Filter 与 HTTP 客户端（Feign/RestTemplate），traceId 跨协议串联；②指标统一到 Micrometer/Prometheus（Dubbo 内置 metrics 与 HTTP 指标同后端聚合）；③日志规范统一 MDC traceId；④网关层统一南北向入口，APM 拓扑图呈现 HTTP+RPC 完整调用树，故障定位不再「过协议就断链」。

### 28. 架构题：RPC 框架选型

> **原题**：Dubbo3(Triple)/gRPC/OpenFeign/自研 评估矩阵怎么列？什么信号出现时建议自研？

- 评估矩阵（维度×候选，逐项打分加权）：
  - 语言与团队生态：主力语言（Java 为主→Dubbo3；多语言→gRPC/Triple；HTTP 优先+异构团队→OpenFeign 系）、团队熟悉度、招聘成本；
  - 性能目标：目标 QPS/RT（高频低 RT 内部服务→二进制 RPC；BFF/边缘→HTTP JSON 即可）；
  - 跨语言与流式：是否需要 streaming（Triple/gRPC 支持，Dubbo2 不支持）、移动端/网关接入方式；
  - 服务治理：路由灰度、容错、限流熔断、QOS 的开箱程度（Dubbo 最全；gRPC 需自建或依赖 mesh）；
  - 基建兼容：注册/配置中心现状（Nacos 对两者通吃）、K8s+mesh 路线（gRPC 原生友好）、APM 插桩成熟度；
  - 演进与社区：版本活跃度、API 稳定性、迁移路径（存量 Dubbo2→3 有官方双注册迁移方案）；
  - 运维成本：排障难度、升级复杂度、是否需要专职团队维护；
  - 安全合规：序列化白名单、mTLS、鉴权内建程度；
- 决策示例：全新无存量 → Dubbo3+Triple（治理全、Java 顺手、可平滑演进到 mesh）或多语言优先选 gRPC；Java 服务+大量 Web 混合 → Spring Cloud 起步，Dubbo3 应用级发现可与 SC 互通作为第二阶段；
- 自研信号（只有「自研收益>通用方案成本」时才考虑）：
  - 极端性能/确定性要求：超低延迟交易系统（零拷贝、用户态网络、GC 级定制）、特殊硬件（RDMA/DPDK）——通用框架的序列化与线程模型开销不可接受；
  - 深度平台化诉求：公司把 RPC 当平台产品输出，需要完全掌控演进与差异化，且有专职中间件团队（5+ 人长期维护）；
  - 特殊调度耦合：单元化/异地多活调度需要 RPC 与调度器、数据层深度协同，通用框架扩展点不够用；
  - 反例信号（此时不要自研）：无专职维护者、通用 CRUD 业务、只想「改改开源」——正确路径是开源+扩展点（Filter/Router/Protocol SPI）满足定制，而不是造轮子；自研的真实成本是十年量级的边界 case（重连、背压、优雅停机、协议兼容、灰度、安全…），绝大多数诉求都能用扩展点解决。