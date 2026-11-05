# Flink 与实时计算面试题

> 适用岗位：实时/流式大数据开发。Flink 的水位线、状态、Checkpoint、Exactly-once 是四大金刚，缺一个都算不扎实。

---

## 一、运行时架构（必考）

1. Flink 架构追问链：
   - JobManager（Dispatcher / JobMaster / ResourceManager 职责拆分）与 TaskManager；slot 的本质（资源隔离单元）与 slot 共享（同 slot 内不同算子子任务）；
   - Task slot 数与并行度（parallelism）的关系；一个作业的并行度怎么定（数据量 → 吞吐 → 资源反推）；
   - TaskManager 之间数据交换（网络缓冲区、背压的传递机制）。
2. ExecutionGraph 演进（StreamGraph → JobGraph → ExecutionGraph）各自在什么层级、优化在哪层做（operator chain：什么条件能 chain、chaining 关闭的影响）？
3. 反压（backpressure）追问链（必考）：
   - 基于 credit 的流控原理；反压在 Flink WebUI 上怎么观察（backpressure 指标、inPoolUsage/outPoolUsage）；
   - 反压的常见根因：下游算子慢（join 大状态、rich function 里的外部 IO、序列化开销）、数据倾斜的某个 subtask；
   - 排查树：WebUI flame graph（火焰图采样）→ 定位慢算子 → 外部依赖/倾斜/序列化三方向。
4. Flink vs Spark Streaming 的模型差异：真流 vs 微批在延迟、语义、状态上的体现；什么时候你们会仍然用 Spark 跑"准实时"（分钟级 T+1 补位）？

## 二、时间语义与窗口（必考）

5. Event Time vs Processing Time 的选择依据；乱序的本质（采集延迟、网络、离线补传）带来的问题？
6. Watermark 追问链（必考）：
   - 水位线是什么（单调递增的时间戳断言）、`forBoundedOutOfOrderness` / 递增水位线的实现；周期性 vs 标点生成；
   - 水位线在多并行度下的传播：一个分区迟到会拖住全局水位（取最小值）的坑与应对（空闲 source 的 idleness 标记）；
   - `allowedLateness` 与侧输出流（side output）的组合：窗口触发两次的语义（update vs insert）；
   - 水位线丢失/不推进的排查（某 source 无数据、并行度调整后水位错乱）。
7. 窗口类型与底层：滚动/滑动/会话窗口的实现差异（滑动窗口的数据复制问题）；增量聚合（ReduceFunction/AggregateFunction）与全窗口（ProcessWindowFunction）的选择——大窗口为什么必须增量聚合？
8. 场景题：实时统计「每 5 分钟的 GMV，允许 2 分钟乱序，超时数据补进下个窗口」，写出你的算子链路与处理迟到数据的完整设计。

## 三、状态管理（必考，区分度最高）

9. 状态分类：Keyed State（Value/List/Map/Reducing/Aggregating）与 Operator State（List/Union/Broadcast）；`keyBy` 后状态怎么分布（hash 到 subtask）？
10. 状态后端追问链：
    - HashMapStateBackend（堆内存，快）与 EmbeddedRocksDBStateBackend（RocksDB，大状态 + 增量 checkpoint）的取舍；
    - RocksDB 的调优项：state TTL、内存列簇（write buffer）、增量 checkpoint 的原理（SST 文件上传）；
    - 大状态作业（TB 级）你的选型与参数模板。
11. 状态 TTL：未清理状态导致 checkpoint 越来越大的经典问题；TTL 的清理策略（增量清理 RocksDB / 全量快照时清理）与「TTL 开了但作业还是变大」的排查。
12. 状态访问的性能：大状态 join（双流 join 的状态膨胀）——状态保留策略（TTL 下线不再匹配的数据）、维表 join 的四种方案（异步 IO 查库 / 广播维表 / 缓存 LRU / 状态预加载）的取舍。

## 四、容错与一致性（必考）

13. Checkpoint 机制追问链（必考）：
    - Chandy-Lamport 分布式快照与 Flink 的 barrier 对齐：barrier 怎么流动、对齐时在干什么（缓存上游数据）；
    - unaligned checkpoint（barrier 不对齐，缓存 in-flight 数据）解决什么问题（大反压下 cp 超时）？代价（更多持久化数据、恢复稍慢）；
    - checkpoint 超时/失败的经典排查：反压 → barrier 对齐卡住 → 大状态序列化慢 → HDFS 慢；
    - state 与 checkpoint 大小的监控项（cp duration、alignment time、state size）。
14. Exactly-once 语义追问链：
    - 端到端 exactly-once 的三要素：source 可重放（Kafka offset）、内部状态快照、sink 两阶段提交（TwoPhaseCommitSinkFunction）；
    - Kafka Sink 的事务流程（pre-commit → cp 完成 → commit）与下游读取事务隔离级别（`read_committed`）的延迟代价；
    - 什么场景退而求其次 at-least-once + 下游幂等（不支持的 sink、延迟敏感），幂等怎么做（主键去重/版本号）。
15. Savepoint 与升级：savepoint vs checkpoint 的区别（触发方式、TTL、格式）；作业升级（改代码/改并行度/改状态结构）怎么恢复状态——状态结构变更（增字段、改 key）的兼容处理；利用 savepoint 做灰度双跑验证的方案。
16. Checkpoint 大文件与恢复时长：TB 状态恢复 20 分钟的业务影响，你怎么把恢复时间压下来（增量 cp、状态拆分作业、local recovery）？

## 五、SQL 与工程实战

17. Flink SQL 执行原理：Table API → Calcite 优化（RBO + CBO）→ DataStream；blink planner 的 minibatch 优化、`table.exec.mini-batch` 与 state TTL 的联动。
18. Flink SQL 的双流 join 与 retract 流追问链：
    - regular join 的状态膨胀与 TTL 设置；interval join（时间区间 + watermark 约束）为什么状态可控；
    - changelog 流（upsert-kafka / CDC connector）在 join、聚合下撤回消息的语义；下游 sink 怎么正确消费 retract 流。
19. Flink CDC：CDC connector 的原理（Debezium 快照 + binlog 增量）、无锁全量迁移的机制、断点续传；用 Flink CDC 做实时数仓 ODS 入湖/入仓的架构。
20. 实时数仓分层：ODS/DWD/DWS 在实时侧的落地（kafka 分层 topic、upsert 存储）、与离线数仓的口径一致（双跑对账/ Lambda 架构 vs Kappa 的取舍）；
21. 维度一致性：实时维表更新怎么广播到作业（CDC 广播状态 broadcast state 模式）、维表延迟导致的脏关联怎么补救（TTL 关联 + 离线回刷）。
22. 资源与稳定性：作业内存模型（TaskManager heap/off-heap/network managed memory）、RocksDB 内存挤占 heap 的配置；K8s 上 Flink（Native K8s / operator）的稳定性实践；
23. 场景题：
    - 实时大屏 GMV 与离线 T+1 对不上，给出你的对账体系（双跑、边界口径、延迟数据归属、修复流）；
    - 作业每天高峰期 checkpoint 失败几次，平时正常——排查路径与根治方案；
    - 让你从 0 搭一个「Binlog → Flink → Doris/Paimon」的实时数仓链路，画出架构、列出每个环节的故障预案。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 运行时与反压 | 20% | slot/chain/credit 流控讲到机制，反压排查有套路 |
| 时间与窗口 | 20% | 水位线传播与并行度坑、迟到数据完整方案 |
| 状态管理 | 25% | 状态后端选型、TTL、大状态调优有实战参数 |
| 容错一致性 | 25% | barrier 对齐 → unaligned、端到端 exactly-once 三要素、savepoint 升级 |
| SQL 与数仓 | 10% | retract 流、CDC、实时离线一致 |

**判定要点**：只会 `keyBy + window` 算中级以下；能把水位线传播、barrier 对齐、RocksDB 调优讲透算高级；有 TB 级状态作业、Exactly-once 落地、实时数仓从 0 搭建的算资深。
