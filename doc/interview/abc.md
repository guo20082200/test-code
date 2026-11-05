下面按「第一轮技术组长」和「第二轮技术负责人」两轮来设计，每轮都覆盖 JD 里的核心考察点。题目尽量用「场景 + 追问」的方式，方便根据候选人回答层层深入，判断其是否真到 P6+ / Tech Lead 水平。

---

## 第一轮：技术组长（偏深度技术 + 编码/排查能力，60–90 分钟）

**目标**：验证 Java 核心功底、中间件原理、线上问题排查、并发与性能优化是否扎实，是否真做过复杂系统。

### 一、Java 核心与并发（15–20 分钟）

1. **JVM 内存与 GC**
   - 描述一次你实际排查过的 JVM 问题（OOM / GC 频繁 / STW 过长）。你怎么定位的？用了哪些工具（jstat、jmap、MAT、Arthas）？最后根因和方案是什么？
   - 追问：G1 和 CMS 的区别？什么场景下你会调整堆参数或换收集器？如何判断是内存泄漏还是内存溢出？

2. **并发编程**
   - `ThreadPoolExecutor` 的核心参数、拒绝策略，你们线上线程池是怎么配置和隔离的？有没有遇到过线程池打满？怎么发现和止损？
   - 追问：`synchronized` 和 `ReentrantLock` 的取舍；AQS 原理；`ConcurrentHashMap` 在 JDK8 的并发设计（CAS + synchronized + 扩容协助）。
   - 场景题：一个接口 QPS 很高但依赖的下游很慢，如何在不堆积线程的前提下做限流/降级/异步化？

3. **IO 与集合**
   - BIO/NIO/AIO 区别，Netty 的 Reactor 模型；你们项目里哪里用到了 NIO？
   - `HashMap` 扩容、红黑树转换阈值；`ArrayList` 与 `LinkedList` 的实际选型。

### 二、分布式与微服务（20–25 分钟）

4. **服务治理**
   - 你们用的是 Spring Cloud 还是 Dubbo？讲一下一次 RPC 调用的完整链路（注册发现、负载均衡、序列化、超时、重试）。
   - 追问：Dubbo 的 SPI 机制、服务暴露与引用流程；Spring Cloud 中 Feign 的超时与重试怎么配，重试会带来什么问题（幂等）？

5. **分布式事务**
   - 你们线上用了哪种分布式事务方案（本地消息表 / TCC / Seata AT / Saga / 最终一致）？为什么选它？一致性如何保证？失败补偿怎么做？
   - 场景题：下单扣库存 + 扣优惠券 + 记流水，跨三个服务，如何保证最终一致？出现悬挂/空回滚怎么处理？

6. **高并发与缓存**
   - 缓存穿透/击穿/雪崩分别怎么解决？你们缓存和 DB 的一致性策略是什么（先删缓存还是后删）？延迟双删解决什么问题？
   - 热点 key 怎么发现和处理？Redis 集群模式下大 key、热 key 的治理经验。
   - 追问：Redis 持久化 RDB/AOF 取舍；分布式锁用 Redis 还是 ZK，Redlock 的争议你怎么看？

7. **消息队列**
   - Kafka 为什么吞吐高（顺序写、零拷贝、批量、分区）？如何保证不丢消息、不重复消费？消费积压怎么排查处理？
   - 场景题：用 MQ 做削峰，如何保证消息顺序性和幂等？

### 三、线上问题排查与性能优化（15–20 分钟）

8. 给一个具体场景：**某核心接口 P99 从 200ms 涨到 2s，监控显示 CPU 不高但 RT 高**，你怎么系统性地排查？（线程栈、GC 日志、下游依赖、连接池、锁竞争、慢 SQL……）
9. 你们做过哪些性能优化？举一个量化收益的例子（QPS 提升 / RT 下降 / 成本下降），优化前后的数据。
10. 一次重大线上故障的处理复盘：现象、定位过程、止损手段、根因、后续改进（预案、监控、演练）。

### 四、工程能力与安全（10 分钟）

11. 你们的 CI/CD 流程是怎样的？代码规范、CR、单测覆盖率怎么落地？
12. 常见 Web 安全漏洞（SQL 注入、XSS、CSRF、越权、反序列化）在你们系统里怎么防？

### 五、编码题（可选，20–30 分钟）

13. 手写一个**带过期时间 + 懒删除 + 定期清理的本地缓存**，要求线程安全，说明并发设计。
14. 或：实现一个**固定窗口/滑动窗口限流器**，并讨论分布式场景下如何扩展。

---

## 第二轮：技术负责人（偏架构设计 + 技术判断 + 带团队，60–90 分钟）

**目标**：考察架构设计能力、技术选型判断、复杂项目主导经验、团队协作与推动力，是否具备 Tech Lead 的视野。

### 一、架构设计（25–30 分钟，选 1–2 题白板设计）

1. **设计一个高并发秒杀/抢购系统**：从流量入口、限流、库存扣减、防超卖、幂等、削峰、降级、数据一致性讲起，追问容量估算、热点隔离、DB 压力、Redis 与 DB 的一致性、故障预案。
2. **设计一个分布式链路追踪 + 日志平台**：TraceId 透传、采样策略、存储选型、对业务性能的影响、与监控告警的联动。
3. **老系统重构**：一个单体应用要拆微服务，你怎么划分边界（DDD）、如何保证迁移期双写/灰度、数据一致性、回滚方案？拆分后带来的分布式问题怎么兜底？
4. 追问通用架构题：如何设计一个**高可用、可扩展**的核心业务架构？多机房/异地多活怎么考虑？降级、限流、熔断、隔离的落地策略。

### 二、技术选型与判断（15 分钟）

5. 讲一次你做过的**关键技术选型**（框架/中间件/存储），当时的备选方案、评估维度、最终决策和后续验证。如果重来会怎么选？
6. 面对「业务要快、技术要稳」的冲突，你怎么平衡？举一个你推动技术方案落地的例子，如何说服业务方和上级？
7. 你怎么看微服务拆分的粒度？什么情况下你会反对拆微服务？

### 三、项目主导与团队（15–20 分钟）

8. 介绍一个你**主导**的复杂项目：你的角色、团队规模、周期、难点、你做的关键决策、最终结果（量化）。
9. 作为 Tech Lead，你如何制定代码规范和技术标准并让团队真正执行？遇到资深同事不配合怎么办？
10. 你如何做项目管理与进度协调？需求变更、资源不足、排期冲突时怎么处理？举实例。
11. 你如何培养团队、做技术传承和 CR？你带过的人有没有明显成长案例？

### 四、故障与决策（10–15 分钟）

12. 假设大促前夜核心链路压测发现瓶颈，但业务坚持按期上线，你作为技术负责人怎么决策和推进？
13. 线上重大故障时，你如何组织排查、对外沟通、止损与复盘？复盘如何避免「甩锅」、真正沉淀改进？

### 五、开放与软素质（5–10 分钟）

14. 你最近一年研究的技术方向？看过哪些源码/写过什么轮子？对 AI 辅助研发、云原生等趋势的看法。
15. 你为什么考虑换机会？期望的团队和职责是什么？未来 2–3 年的规划。

---

## 评分参考（建议面试官用）

| 维度 | 权重 | 观察点 |
|---|---|---|
| Java 核心与并发 | 20% | 原理是否讲透，是否有真实排查经验而非背书 |
| 分布式/中间件 | 25% | 选型理由、一致性/幂等/高可用落地细节 |
| 架构设计 | 25% | 边界划分、容量估算、故障预案、权衡取舍 |
| 排查与优化 | 15% | 方法论 + 量化结果 |
| 团队/项目/软素质 | 15% | 主导性、推动力、沟通逻辑 |

**判定要点**：P6+ 候选人应能「讲清原理 + 给出真实项目细节 + 说出权衡和踩坑」；只会背八股、回答停留在概念层、项目描述无量化结果、把团队成果说成个人成果的，需降级或淘汰。

---

# 参考答案

> 面试题多为开放式，以下给出「要点框架 + 关键细节 + 权衡取舍」，实际面试需结合个人项目实例展开。

## 第一轮：技术组长

### 一、Java 核心与并发

**1. JVM 内存与 GC**

排查案例（示例模板）：
- 现象：监控告警 Full GC 频繁 / OOM，接口 RT 抖动。
- 定位过程：`jstat -gcutil <pid> 1000` 观察 Old 区占用与 Full GC 频率 → `jmap -histo:live <pid>` 查看对象直方图，发现某业务 DTO 数量异常 → 必要时 `jmap -dump:live,format=b,file=heap.hprof` + MAT 分析支配树 / Leak Suspects → Arthas `dashboard` / `watch` 验证业务路径。
- 常见根因与方案：未分页的全表查询结果堆积（加分页 + 流式处理）、本地缓存无上限（换 Caffeine 设容量上限和过期）、ThreadLocal 忘记 remove（用完 remove / 池化场景用 TransmittableThreadLocal）、连接/流未关闭（try-with-resources）。

G1 vs CMS：
- CMS：老年代并发「标记-清除」，停顿低但产生碎片，碎片过多触发 Full GC（Serial Old 单线程，停顿长）；并发失败（concurrent mode failure）退化严重；JDK9 废弃、JDK14 移除。
- G1：堆划分为 Region，物理上分代、逻辑上不连续；整体「标记-整理」、Region 间「复制」，天然无碎片；通过 `-XX:MaxGCPauseMillis` 建立可预测停顿模型，优先回收价值高的 Region；SATB 记录并发标记期间的引用变化。
- 选型：大堆（>8G）、低停顿要求 → G1（或超大堆 + 极致低停顿用 ZGC/Shenandoah）；吞吐优先的批处理任务 → Parallel GC；堆小、JDK8 老系统 → CMS/Parallel。

内存泄漏 vs 溢出：
- 溢出（OOM）：堆确实装不下，可能是一次性大对象、堆参数过小，属瞬时问题。
- 泄漏：对象不再使用但 GC 无法回收，特征是 Full GC 后 Old 占用曲线「阶梯式上升不回落」（正常应是锯齿形回落）。验证方法：隔一段时间连续取多个 dump 做 MAT 对比，看增长对象集合；最终也会演变为 OOM。

**2. 并发编程**

ThreadPoolExecutor：
- 7 个参数：corePoolSize、maximumPoolSize、keepAliveTime、unit、workQueue、threadFactory、handler。
- 提交顺序：先填核心线程 → 入队 → 队列满才扩到 max → 再满执行拒绝策略。注意：这与「先扩线程再排队」的直觉相反，队列过大会导致 max 形同虚设。
- 拒绝策略：AbortPolicy（默认，抛异常）、CallerRunsPolicy（调用方执行，天然反压但会阻塞调用线程）、DiscardPolicy、DiscardOldestPolicy；线上一般自定义拒绝策略（记日志 + 告警 + 落库重试）。
- 线上配置：按业务域隔离线程池（核心交易、营销、文件互不影响）；IO 密集型线程数 ≈ 核数 × (1 + 等待时间/计算时间)；推荐「动态线程池」（如美团方案）：参数可通过配置中心热更 + 监控 activeCount/queueSize/rejectCount 告警。
- 打满发现与止损：监控队列长度、活跃线程数、拒绝次数；止损：降级非核心调用、动态调大队列/max、丢弃低优先级任务、扩容实例。

synchronized vs ReentrantLock：
- synchronized：JVM 层面（monitorenter/exit），锁升级（无锁→偏向→轻量级 CAS→重量级 monitor），自动释放，不可中断，非公平。
- ReentrantLock：API 层面基于 AQS，支持公平锁、可中断 lockInterruptibly、tryLock 超时、多个 Condition。
- 取舍：简单临界区用 synchronized（JVM 持续优化、无需手动释放）；需要超时获取、可中断、多条件队列等高级特性才用 Lock。

AQS 原理：
- 核心：volatile int state（同步状态）+ CAS 修改 state + CLH 变体的双向等待队列 + LockSupport park/unpark 阻塞唤醒线程。
- 独占模式（ReentrantLock）与共享模式（Semaphore/CountDownLatch）两种模板：acquire 失败入队 park，release 时唤醒后继节点。

ConcurrentHashMap（JDK8）：
- 放弃 Segment 分段锁，改为 CAS + synchronized 锁单个桶头节点，锁粒度更细。
- 空桶用 CAS 插入，非空桶 synchronized 头节点；链表过长（≥8 且表长 ≥64）树化。
- size 用 baseCount + CounterCell[] 分散计数（LongAdder 思想），避免计数热点。
- 扩容：transfer 支持多线程协助迁移（认领 stride 区间），ForwardingNode 标记已迁移桶，读请求转发到新表。

场景题（下游慢、QPS 高）：
- 限流：入口层（网关/Sentinel）QPS 限流 + 热点参数限流，超出直接快速失败。
- 熔断：Sentinel/Resilience4j 断路器，下游错误率/慢调用超阈值自动熔断，半开探测恢复。
- 降级：熔断后返回兜底数据 / 缓存旧值 / 默认值。
- 异步化：同步链路只做「受理」，用 MQ 削峰异步处理；或 CompletableFuture 并行编排。
- 关键点：用「少量线程 + 异步非阻塞/消息队列」替代「大量线程同步等下游」，避免线程堆积雪崩。

**3. IO 与集合**

- BIO：一连接一线程，阻塞读写，连接多时线程爆炸。NIO：Selector 多路复用 + Channel + Buffer，一个线程管理大量连接，事件驱动。AIO：异步 IO 回调，Linux 底层仍是 epoll 模拟，实际使用少。
- Netty 主从 Reactor：boss 线程组只负责 accept，把连接注册到 worker 线程组的 selector；worker 负责读写事件，pipeline 责任链处理编解码/业务。项目中的应用：自研 RPC 框架通信层、网关、长连接推送服务。
- HashMap：容量 16、负载因子 0.75；链表 ≥8 且数组长度 ≥64 才树化（否则先扩容），红黑树节点 ≤6 退化回链表；JDK8 扩容用 `e.hash & oldCap` 判定高低位拆分，无需重算 hash；并发使用必须包一层或换 ConcurrentHashMap。
- 选型：ArrayList 随机访问 O(1)、尾部增删好，是默认选择；LinkedList 理论头尾插删 O(1) 但节点内存开销大、CPU 缓存不友好，实际做队列/栈首选 ArrayDeque。

### 二、分布式与微服务

**4. 服务治理**

Dubbo RPC 链路：
- 启动时：provider 服务 export（本地暴露 → 协议层启动 Netty server → 注册 ZK/Nacos）；consumer 启动生成代理，订阅注册中心拿到地址列表缓存在内存（RegistryDirectory），地址变更推送更新。
- 调用时：Proxy 代理 → Cluster（负载均衡：随机/轮询/最少活跃/一致性哈希；集群容错：failover 默认重试 2 次、failfast、failsafe）→ Protocol → 序列化（hessian2/protobuf）→ Netty 发送 → provider 反序列化执行。
- 超时：provider 和 consumer 均可配，consumer 侧生效为准；重试要区分幂等性，写接口必须 failfast 或关闭重试。

Dubbo SPI：相比 JDK SPI（全量实例化），按 key 按需实例化；@Adaptive 自适应扩展（运行时按 URL 参数选择实现）；支持 IOC（setter 注入其他扩展）和 AOP（Wrapper 自动包装）。

Feign 超时与重试：配置 `feign.client.config.default.connectTimeout/readTimeout`（注意 Ribbon/LoadBalancer 配置的覆盖关系）；重试默认关闭（Retryer.NEVER_RETRY），若开启默认 Ribbon 的重试 + GET 重试较安全；POST 重试会导致重复提交 → 必须接口幂等（唯一业务键 + 去重表/状态机），稳妥策略：只对查询重试，写接口靠熔断 + 快速失败。

**5. 分布式事务**

选型与理由（示例）：多数电商跨服务场景选「本地消息表 / RocketMQ 事务消息」，追求最终一致 + 高可用；资金类强一致场景才用 TCC 或 Seata AT。理由：MQ 是既有基础设施、异步削峰天然解耦、补偿语义清晰；TCC 开发成本高（每个参与方实现 Try/Confirm/Cancel 三个接口）。

本地消息表模式：业务表和消息表同库、同本地事务写入 → 定时任务/事务消息投递 MQ → 消费方幂等消费 → 失败重试 + 死信告警人工兜底；再加定期对账任务兜底。

场景题（下单扣库存 + 扣券 + 记流水）：
- 主流程：订单服务本地事务创建订单 + 写消息记录；库存/优惠券服务消费消息，各自本地事务执行 + 幂等表防重；流水服务同样消费。
- 一致性保证：消费失败重试 → 死信告警 → 人工/自动补偿（反向消息回滚）；每日对账任务比对三方数据修复差异。
- TCC 两个经典问题：
  - 空回滚：Try 请求因网络未到达，Cancel 先到 → Cancel 检查事务执行记录，无 Try 记录则直接返回成功并记一条「已回滚」标记。
  - 悬挂：Cancel 执行后，迟到的 Try 才到达 → Try 执行前检查是否已有回滚标记，有则拒绝执行。
  - 解决思路统一为：事务控制表记录事务 ID 状态，接口执行前校验。

**6. 高并发与缓存**

三件套：
- 穿透（查不存在的数据，打到 DB）：布隆过滤器前置拦截；空值缓存（短 TTL 如 30s）；入口参数合法性校验。
- 击穿（单个热 key 过期瞬间大量请求打 DB）：互斥锁重建（只放一个请求去 DB，其余等待）；逻辑过期（值里带过期时间，过期后异步刷新，始终返回旧值）；热点数据不设物理过期。
- 雪崩（大量 key 同时过期或 Redis 宕机）：TTL 加随机抖动；多级缓存（本地 + Redis）；Redis 高可用集群；限流降级兜底。

缓存一致性：
- 推荐先更新 DB 再删缓存（Cache Aside），配合过期时间兜底。
- 延迟双删解决「读请求在写请求前读了旧库、写后又把旧值写回缓存」的并发窗口：写前删一次 → 更新 DB → 延迟几百 ms 再删一次。
- 严格强一致代价极高，实践都是最终一致：binlog 订阅（Canal）异步更新缓存 + 延迟消息二次删除 + 对账。

热点 key 与大 key：
- 发现：Redis 监控（如 redis-cli --hotkeys / 监控平台 key 分析）、客户端打点统计、proxy 层统计。
- 热 key 治理：本地缓存（Caffeine）挡住大部分读；key 加随机后缀打散到多个节点（读多场景多副本）；必要时单独隔离到独立集群。
- 大 key 治理：拆分（大 hash 分小 hash）、压缩 value、删除用 UNLINK 异步删避免阻塞；上线前用分析工具扫描预防。

持久化与分布式锁：
- RDB：fork 子进程快照，文件小恢复快，但两次快照间数据可能丢；fork 大内存时有阻塞风险。AOF：追加写，everysec 最多丢 1 秒，文件大恢复慢，需 rewrite。生产推荐 4.0+ 混合持久化（RDB 头 + AOF 增量）。
- 分布式锁：一般场景 Redis 足够——SET key uniqueValue NX PX ttl，释放用 Lua 校验唯一值防误删；Redisson 看门狗自动续期。ZK 临时顺序节点公平锁，强一致但性能和运维成本高。
- Redlock 争议：Martin Kleppmann 指出分布式锁的正确性依赖时钟假设，进程 GC 停顿/时钟漂移下多节点 Redlock 仍可能同时被两个客户端持有，且无 fencing token 无法防止「锁过期后旧客户端继续写」。结论：强正确性要求（如资金防重）用 DB 唯一约束/乐观锁版本号兜底，Redis 锁只做「效率锁」减少并发冲突。

**7. 消息队列**

Kafka 高吞吐：顺序写磁盘（append-only log，接近内存速度）、页缓存（读写都走 page cache）、零拷贝（sendfile 消费数据直传网卡）、批量发送 + 压缩、分区并行 + 消费者组水平扩展。

不丢消息（三端保证）：
- Producer：acks=all + retries>0 + enable.idempotence=true（防 broker 端重复）。
- Broker：replication.factor≥3，min.insync.replicas≥2，unclean.leader.election.enable=false。
- Consumer：关闭自动提交，业务处理完成后手动提交位移。

不重复消费：Kafka 至少一次语义下重复不可避免，消费端幂等——唯一业务 ID 去重表（DB 唯一索引）、Redis setnx 标记、状态机校验（只允许合法状态流转）。

积压排查：消费组 lag 监控 → 看消费者是否宕机、消费是否被失败消息卡住（重试风暴）、下游 DB 是否慢；处理：扩分区 + 扩消费者实例（分区数≥消费者数才有并行收益）、消费逻辑批量化和异步化、临时方案——新起 topic 用搬运程序快速转储再慢速补消费；死信隔离毒丸消息。

场景题（MQ 削峰的顺序性与幂等）：
- 顺序性：按业务键（订单 ID）hash 到固定分区（发送时指定 key），单分区内天然有序；消费端单分区再按 key hash 到固定内存队列 + 单线程执行，避免多线程乱序。全局有序只能单分区，牺牲并行度。
- 幂等：唯一业务 ID + DB 唯一索引去重表，或消费前查 Redis 是否已处理（需处理 Redis 丢数据兜底），状态机校验。

### 三、线上问题排查与性能优化

**8. P99 从 200ms 涨到 2s，CPU 不高但 RT 高**

系统性排查步骤：
1. 先看变更：确认发生时间点，关联发布/配置变更/下游发布（80% 的 RT 劣化来自变更）。
2. 定位耗时归属：全链路 Trace（SkyWalking/Zipkin）看 2s 花在本服务还是下游某依赖。
3. 本服务维度：
   - 线程 dump（jstack / Arthas thread -b 找阻塞源头）：worker 线程是否大量 WAITING/BLOCKED——等连接池、等锁、等下游 IO。
   - GC 日志：Full GC 停顿（CPU 可能不高，因为 STW 期间）。
   - 锁竞争：jstack 反复抓几次看同一把锁的阻塞。
4. 资源池维度：DB/Redis/HTTP 连接池是否打满（获取连接排队时间计入 RT）——这是最常见根因之一。
5. 存储维度：MySQL slowlog、`show processlist`、锁等待（information_schema.innodb_trx / lock_waits）；Redis 慢命令（KEYS、大 key）。
6. 下游维度：下游接口 RT 变化、网络质量（重传率、机房专线抖动）。
7. 容器/宿主机维度：CPU throttling（cgroup 限额导致使用率不高但被限流）、宿主机超卖、IO wait 高。
常见根因清单：连接池打满、慢 SQL、下游抖动、Full GC、锁竞争、CPU throttle。

**9. 性能优化案例（模板）**
- 示例：订单列表接口 RT 800ms → 120ms。手段：逐条 DB 往返改批量（IN 查询 + 批量 insert），N+1 消除；热点数据加多级缓存；无依赖的 3 次下游调用改 CompletableFuture 并行。
- 量化：RT 降 85%，同规格实例 QPS 从 400 提升到 2600，DB 负载下降 70%，机器成本省一半。
- 回答要点：说清「瓶颈定位依据 → 手段 → 前后数据」闭环，且收益是压测/监控实测的。

**10. 故障复盘（模板）**
- 现象与影响面：什么时间、多少用户/交易受影响、资损多少。
- 时间线：告警发现 → 响应 → 止损 → 恢复的分钟级记录。
- 止损动作：回滚版本 / 扩容 / 降级开关 / 流量切换（先止损后定位）。
- 根因：5 Why 追问，最终落到机制而非个人（例：「配置改错」→「为什么没有配置校验和灰度」）。
- 改进项：每项有 owner 和 deadline，可验证——如增加监控告警、变更审批卡点、预案演练排期；下次复盘先过一遍上次改进项完成情况。

### 四、工程能力与安全

**11. CI/CD 与质量落地**
- 流水线：提交触发 → 编译 → 单测 + JaCoCo 覆盖率（增量代码门禁，如 60%）→ SonarQube 静态扫描（阻断级问题 fail）→ 构建镜像 → 自动部署测试环境 → 集成回归。
- CR：主干保护，至少 1~2 人 approve 才能合入；小步提交；CR checklist（幂等、异常、兼容性）。
- 发布：灰度/金丝雀按比例放量，观察核心指标（错误率、RT）自动判断是否继续；一键回滚。
- 规范落地靠工具而非自觉：Checkstyle/SpotBugs + Sonar 门禁卡在 CI 上，人不通过机器通过才算过。

**12. Web 安全防护**
- SQL 注入：预编译参数化，禁用字符串拼接 SQL；ORM 用参数绑定；MyBatis 用 `#{}` 不用 `${}`。
- XSS：输出编码转义（按 HTML/JS/URL 上下文）；CSP 限制脚本来源；富文本白名单过滤（jsoup）。
- CSRF：CSRF Token；Cookie 加 SameSite；修改状态只用 POST/PUT 且校验 Token。
- 越权：水平越权——服务端校验资源归属（当前用户只能查自己的数据），统一在 DAO/Service 层拦截，不信任前端传的用户 ID；垂直越权——统一鉴权框架 + 注解声明权限。
- 反序列化：不使用 Java 原生反序列化处理外部输入；JSON 反序列化开启类型白名单；及时升级 Jackson/Fastjson/Log4j 等已知漏洞组件；WAF/RASP 兜底。

### 五、编码题

**13. 带过期时间的线程安全本地缓存**

```java
public class LocalCache<K, V> {
    private static final int MAX_SIZE = 100_000;

    private static class Entry<V> {
        final V value;
        final long expireAt; // 过期时间戳（毫秒）

        Entry(V value, long ttlMillis) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean expired() {
            return System.currentTimeMillis() >= expireAt;
        }
    }

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "local-cache-cleaner");
                t.setDaemon(true);
                return t;
            });

    public LocalCache() {
        cleaner.scheduleWithFixedDelay(this::cleanExpired, 1, 1, TimeUnit.SECONDS);
    }

    public void put(K key, V value, long ttlMillis) {
        if (map.size() >= MAX_SIZE) {
            cleanExpired(); // 达到上限先清理；更完善可加 LRU 淘汰
        }
        map.put(key, new Entry<>(value, ttlMillis));
    }

    public V get(K key) {
        Entry<V> e = map.get(key);
        if (e == null) return null;
        if (e.expired()) {
            // 懒删除：remove(key, e) 带 CAS 语义，
            // 即使其他线程刚 put 了新值也不会误删
            map.remove(key, e);
            return null;
        }
        return e.value;
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        map.entrySet().removeIf(e -> e.getValue().expireAt <= now);
    }
}
```

并发设计说明：
- ConcurrentHashMap 保证读写线程安全；无全局锁，读操作无锁。
- 懒删除：get 时检查过期并删除，保证调用方拿不到已过期数据；`remove(key, e)` 传 Entry 引用做条件删除，避免与并发 put 产生竞态。
- 定期清理：独立单线程守护线程批量清理，避免冷数据（无人 get）永不释放导致内存泄漏。
- 追问可延伸：容量上限可加 LRU（链表记录访问顺序或直接换 Caffeine）；清理可分批防止单次遍历过长。

**14. 限流器**

固定窗口（有临界突刺问题：窗口交界处瞬间可通过 2 倍流量）：

```java
public class FixedWindowRateLimiter {
    private final long windowMillis;
    private final int limit;
    private long windowStart = 0;
    private int count = 0;

    public FixedWindowRateLimiter(int limit, long windowMillis) {
        this.limit = limit;
        this.windowMillis = windowMillis;
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - windowStart >= windowMillis) {
            windowStart = now;
            count = 0;
        }
        return count++ < limit;
    }
}
```

滑动窗口（按时间戳队列，粒度更平滑）：

```java
public class SlidingWindowRateLimiter {
    private final int limit;
    private final long windowMillis;
    private final Deque<Long> timestamps = new ArrayDeque<>();

    public SlidingWindowRateLimiter(int limit, long windowMillis) {
        this.limit = limit;
        this.windowMillis = windowMillis;
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long boundary = now - windowMillis;
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= boundary) {
            timestamps.pollFirst(); // 移除窗口外旧请求
        }
        if (timestamps.size() < limit) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
```

分布式扩展讨论：
- 单机版用 synchronized/AQS 原子操作即可；集群下必须集中状态。
- Redis + Lua：用脚本把「读计数 + 判断 + 写入」原子化（滑窗可用 ZSet 存时间戳，ZREMRANGEBYSCORE 清理 + ZCARD 判断 + ZADD 记录；或令牌桶用 String 存桶状态）。注意 Lua 保证原子性，避免 check-then-act 竞态。
- 高 QPS 下 Redis 成为瓶颈/单点：用 Sentinel 集群限流（token server 模式）或客户端本地预分配额度（每实例领一部分配额）。

## 第二轮：技术负责人

### 一、架构设计

**1. 秒杀/抢购系统**

分层设计（漏斗式逐层削减流量）：
1. 流量入口：CDN 静态化页面、按钮置灰防重复点击、前端答题/验证码打散请求。
2. 网关层：令牌桶限流 + 单用户/单 IP 维度限流 + 黑名单；无库存直接返回售罄。
3. 库存层：活动开始前把库存预热到 Redis，Lua 脚本原子「校验 + 扣减 + 记录用户购买资格」（防超卖、防重复下单）；库存分桶（100 库存拆 10 个 key）分散热点。
4. 异步下单：扣减成功后发 MQ 削峰，订单服务消费落库（DB 层 `update stock set n=n-1 where id=? and stock>0` 乐观锁兜底防超卖）。
5. 结果获取：前端轮询/推送查询下单结果。
6. 幂等：请求带唯一 token，Redis + DB 唯一索引双重去重。
7. 一致性：Redis 预扣成功但 MQ/DB 失败 → 可靠消息/补偿回补库存；活动结束对账校准。
8. 热点隔离：秒杀独立应用集群 + 独立 Redis 分片，不拖垮主站。
9. 容量估算示例：预估峰值 10w QPS → 网关限流到 Redis 可承受量（单分片约 8-10w，分片后线性扩展）→ 最终 DB 写 QPS 由 MQ 削峰控制在千级。
10. 故障预案：Redis 故障 → 降级为本地内存令牌限流排队；MQ 故障 → 限流直连 DB 按容量放行；所有开关预演过。

**2. 链路追踪 + 日志平台**
- TraceId：入口生成全局唯一 ID（雪花/UUID + 时间前缀便于检索），通过 RPC header、HTTP header、MQ 消息属性透传；线程池切换用 TransmittableThreadLocal 保证跨线程透传。
- 采样：头部采样（固定 1%~10%，入口决定全局采样标记，避免断链）+ 尾部采样（错误、慢请求 100% 保留），平衡成本与排障价值。
- 存储：Trace 用 Elasticsearch（按时间分索引 + traceId 查询）或 ClickHouse（列存压缩比高、扫描快、成本低）；日志 ELK（Filebeat → Kafka 缓冲 → Logstash → ES）。
- 性能影响：SDK 全异步——内存队列 + 批量刷盘（如每 500ms 或 512 条触发），队列满丢弃保护业务；上报独立线程，不阻塞业务线程。
- 联动：慢 trace/错误 trace 聚合成指标 → 告警规则；告警点击可下钻到对应 trace 和日志，形成「指标 → 链路 → 日志」三层排障闭环。

**3. 单体拆微服务**
- 边界划分：DDD 限界上下文——按业务能力（商品、交易、库存、营销、用户）划分，结合团队结构（康威定律）；先梳理数据依赖，领域模型和表耦合最深的不要硬拆。
- 迁移策略：绞杀者模式——网关按路由规则把新功能流量切到新服务，老功能逐步剥离；优先拆「读多写少、依赖简单」的模块练手。
- 数据迁移：binlog 同步双写（旧库为准）→ 新服务灰度读新库 → 校验通过切写 → 观察期后下线旧写。全程对账任务校验两边数据。
- 灰度：按用户白名单/比例切流，可回滚（网关一键切回旧链路）。
- 分布式问题兜底：跨服务一致性用本地消息表/事务消息；引入统一网关鉴权、全链路 Trace、接口幂等规范；准备服务降级预案。

**4. 通用高可用架构 + 多机房**
- 基础：无状态服务水平扩展；多级缓存（本地 + Redis + CDN）；异步化削峰；读写分离 → 分库分表；依赖治理（弱依赖可降级）。
- 多机房：同城双活（机房间同步/半同步复制，延迟毫秒级，应用双活）→ 异地多活（单元化 set 化：按用户维度分片路由到单元，单元内闭环；数据层跨机房异步复制 + 冲突处理；故障时切单元）。异地多活成本极高，先做同城双活 + 异地灾备（冷备 → 热备 → 多活渐进演进）。
- 稳定性四板斧落地：限流（网关 + 应用双层）、熔断（下游故障隔离防雪崩）、降级（非核心功能开关、兜底数据）、隔离（线程池隔离、集群隔离、核心/非核心流量隔离）。

### 二、技术选型与判断

**5. 关键选型（回答模板）**
- 背景：什么业务、什么约束（规模、团队、时间）。
- 备选：列 2~3 个候选（如 MQ 选型：Kafka vs RocketMQ vs RabbitMQ）。
- 评估维度：功能匹配度（事务消息/延迟消息/顺序）、性能、运维成本与团队熟悉度、社区活跃度与生态、公司既有技术栈复用。
- 决策与验证：给出决策 + 上线前 POC 压测数据验证 + 灰度计划。
- 复盘：如果重来——说出当时认知不足的地方（体现成长），如「低估了运维复杂度，应该先搭好监控再上量」。

**6. 业务快 vs 技术稳**
- 分层处理：核心交易链路坚持质量标准（预案、灰度、压测必做）；探索性业务允许快（feature flag、简化流程、技术债登记后续偿还）。
- 推动案例模板：用数据说服——估算故障成本（资损/资损概率/品牌影响）vs 提前投入成本，量化对比；方案上先小范围试点（一个非核心场景）拿到效果再推广，降低业务方顾虑；向上管理——给上级选择题而不是判断题（A 方案快但风险 X，B 方案慢 2 周但可控）。

**7. 微服务拆分粒度**
- 原则：按业务能力和团队规模拆，服务数量 ≤ 团队能运维的极限；初期宁可粗粒度（模块化单体 + 清晰模块边界），边界清晰后再拆。
- 会反对拆的情况：团队规模小（<10 人后端）、业务边界还看不清、没有分布式基础设施能力（Trace/监控/事务方案）、数据库耦合拆不开、拆分动机只是「简历驱动」。
- 观点：微服务解决的是「组织协作与独立演进」问题，不是性能问题；先有模块边界，再谈拆分。

### 三、项目主导与团队

**8. 主导复杂项目（STAR 模板）**
- Situation/Task：项目背景、规模（如交易链路重构、团队 12 人、6 个月）。
- Action：自己的关键决策（架构方案选型、里程碑拆分、风险预案），哪些是自己拍板的、哪些是推动团队共识的。
- Result：量化结果（性能数据、稳定性数据、业务指标），如实区分个人贡献与团队贡献（面试官最反感把团队成果说成个人）。

**9. 规范落地与资深同事**
- 规范工具化：Checkstyle/Sonar/CI 门禁让机器把关，人只关注设计和逻辑；规范文档 + 新人导师制。
- CR 文化：小 PR、CR 模板、轮值制度，CR 结果纳入积分但不 KPI 化（避免形式主义）。
- 资深不配合：先一对一沟通了解真实诉求（可能是方案确实有问题，虚心采纳）；是习惯问题则用事故事例/数据说明成本；再不行引入技术委员会机制形成团队共识，让规则约束所有人；同时给对方一个能发挥价值的位置（如让他主导一个技术专项）换取认同。

**10. 项目管理与进度协调**
- 排期：团队自己估，留 20% buffer；需求分级，MVP 先行。
- 需求变更：变更影响评估（工期/风险）透明给业务方，由其决策排期代价；锁定期后变更走下一迭代。
- 资源不足/冲突：砍范围或分期而非全员加班；风险早暴露，让上级和业务方尽早参与取舍，而不是最后一天爆雷。举实例（模板：一次大促项目需求中途加量，通过砍非核心功能保住核心时间点）。

**11. 团队培养与传承**
- 机制：新人导师制（1 个月带教目标）、双周技术分享、CR 即辅导（讲 why 不只讲 what）、轮岗接触不同模块、给高潜同事独立负责模块/带小项目的练手机会。
- 成长案例（模板）：某中级同学从只能做局部功能 → 通过主导一次服务拆分 + 技术分享 → 一年后独立 lead 一个 3 人小组，晋升成功。

### 四、故障与决策

**12. 大促前发现瓶颈但业务坚持上线**
1. 先量化风险：压测容量 vs 预估峰值流量，差距多少、最坏后果（资损估算），不模糊表态。
2. 想办法扩容/优化：临时扩容、性能优化、缓存预热，能不能把差距补上。
3. 给业务方可选项而不是单选题：A 延期；B 上线但砍非核心功能保核心链路；C 上线但配合限流预案（超容量自动限流排队，牺牲部分用户体验保全量可用）。
4. 底线：核心链路必须能扛住预估流量且有止损预案，做不到就坚持不上线并升级到更高级别决策；用数据说话，避免「我觉得不行」。

**13. 重大故障组织与复盘**
- 应急响应：先止损后定位（回滚 > 扩容 > 降级 > 切流）；明确分工——总指挥（协调+决策）、排查组、对外沟通专人（避免多口径）、业务同步专人；每 10~15 分钟同步一次进展。
- 对外沟通：统一话术模板、定期通报，不轻易定性原因。
- 复盘（blameless 无指责文化）：只对事不对人，聚焦「为什么流程/机制没有拦住」；产出时间线 + 根因链 + 改进项清单（可验证、有 owner、有 deadline）；改进项进入跟踪表直到关闭；大故障改进项优先级最高；定期故障演练验证预案有效性。

### 五、开放与软素质

**14. 技术方向与趋势**
- 如实回答自己深入的方向（源码/轮子经历），关键是体现「深度学习方法论」而非罗列名词。
- AI 辅助研发：提效在代码生成/CR/单测，核心链路仍需人工把关；团队落地建议（如 AI 辅助 CR 工具、内部知识库问答）。
- 云原生：K8s 成事实标准，Serverless 适合弹性波峰场景，Service Mesh 渐进采用；关注它对交付效率和成本的实际收益，不追新。

**15. 换机会与规划**
- 离职动机：正向表述（寻求更大挑战/平台/技术空间），不抱怨前司。
- 期望：技术氛围 + 业务空间 + 职责边界清晰。
- 规划：近期（1-2 年）做深某领域并拿到业务结果，中期（3 年）带更大团队/负责更大系统，与应聘岗位的发展路径对齐。

---

## 附：回答技巧提醒

1. 每个原理题回答结构：是什么 → 核心机制 → 适用场景/对比 → 踩过的坑。
2. 项目题必须带数字（QPS、RT、机器数、团队规模、收益百分比）。
3. 被追问「为什么」时至少答到两层：方案层 → 原理层 → 权衡层。
4. 不知道的问题：诚实说没深入过，但展示推理过程（类比已知知识推断），比硬编强得多。

