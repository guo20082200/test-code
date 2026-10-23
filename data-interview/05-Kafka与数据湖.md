# Kafka、数据湖与 OLAP 面试题

> 适用岗位：大数据开发（进阶）。Kafka 是实时链路的血管，数据湖与 OLAP 是近两年面试增量最大的板块。

---

## 一、Kafka（大数据视角）

1. 架构追问链：
   - ISR 机制：leader epoch、HW（高水位）与 LEO 的关系，为什么需要 HW（消费可见性与数据一致性）；
   - controller 的选举与职责（分区 leader 选举）、KRaft 模式下 controller quorum 的变化（为什么去 ZK）；
   - Unclean leader election 的权衡（可用性 vs 一致性）你们怎么配的，为什么。
2. 高吞吐原理追问链（必考）：顺序写、page cache（为什么 Kafka 不自己管缓存）、零拷贝 sendfile、批量+压缩（zstd/lz4）、分区并行——每个点对应的参数与监控指标？
3. 生产端调优：acks 与 min.insync.replicas 组合的语义矩阵（0/1/all 的丢与重风险）、幂等生产者（pid + seq 去重）与事务（跨分区原子 + exactly-once streams）、`linger.ms`/`batch.size` 的权衡、大消息的拆分方案；
4. 消费端追问链：
   - 分配策略演进：range → roundrobin → sticky → cooperative-sticky（增量 rebalance 解决 stop-the-world），rebalance 风暴的原因与治理（`max.poll.interval.ms` 误判、session 心跳、静态成员 membership）；
   - offset 提交语义：自动提交丢消息的窗口、手动提交的幂等配合；
   - 消费积压 SOP（跳过/扩容/搬运/降级）你执行过吗，量化数据？
5. 顺序性与分区设计：业务 key 到分区的 hash、热点 key 导致分区倾斜的处理（key 加盐、自定义分区器）；扩分区对 key 顺序性的破坏——需求发生时怎么设计兼容？
6. Kafka 的存储运维：日志段与索引（timeindex/offsetindex）、retention 与 log compaction（compact 主题的 key 约束）、跨机房同步（MirrorMaker2 的 offset 映射与方向切换）；
7. 场景题：Kafka 集群写入流量翻倍在即，给出容量与稳定性的准备清单（broker 扩容 + 分区重分配的流量影响、机架感知、监控水位、客户端参数基线）。

## 二、数据湖（Hudi / Iceberg / Paimon）

8. 数据湖解决什么问题：Hive 表「不支持更新、小文件、schema 演进弱、时间旅行缺失」四大痛点逐条对应湖表能力；
9. 三大表格式深度对比（必考）：
   - 快照模型：Iceberg 的元数据树（snapshot → manifest list → manifest → data file）与 Hudi 的 timeline；
   - 更新语义：COW vs MOR 的读写权衡（写放大 vs 读放大、compaction 成本）；Hudi 的 COW/MOR 与 Iceberg v2 的 merge-on-read、Paimon 的 LSM 设计；
   - 主键支持：Hudi（原生主键表）、Iceberg v2（equality delete）、Paimon（LSM 主键表）各自怎么实现 upsert；
   - 选型结论：已上 Flink 生态选什么、批处理为主选什么、你们为什么选了现在的方案？
10. 湖表与引擎协同：Flink 写 Iceberg/Hudi 的 checkpoint 提交语义、小文件合并（compaction 计划与执行分离）、快照过期与孤儿文件清理的运维 SOP；
11. Schema 演进：加列/改类型/重命名在各格式下的支持差异（Iceberg 的演进能力为什么强）；分区演进（partition evolution）什么场景有用？
12. 场景题：
    - 「Binlog → Flink → 湖表」的准实时入湖，分钟级可见：链路设计 + compaction 防积压 + 数据一致性验证方案；
    - 湖表查询越来越慢：小文件、manifest 膨胀、快照未过期——给你一份治理清单并说明优先级；
    - 存量 10 万张 Hive 表迁湖：迁移顺序、双跑校验、权限与血缘迁移怎么设计？

## 三、OLAP（ClickHouse / Doris / StarRocks）

13. OLAP 引擎在数仓的定位：为什么 Hive/Spark 之外需要交互式分析引擎（P99 查询秒级的目标差距）？
14. ClickHouse 追问链：
    - MergeTree 引擎族：主键索引（稀疏索引）的原理、排序键设计与查询剪枝的关系；
    - 数据分布：分片（shard）+ 副本（replica）+ 分布式表的写入路径（本地写 vs 分发写）、数据 skew 处理；
    - 物化视图与预聚合的用法与坑（异步刷新一致性）；join 能力弱的原因与替代方案（宽表/字典表）；
    - 变更数据的困境：ReplacingMergeTree 的去重时机不确定——upsert 需求怎么满足（FINAL 查询的代价、OPTIMIZE 命令、版本列）？
15. Doris / StarRocks 追问链：
    - FE/BE 架构与 MPP 执行；明细模型/聚合模型/Unique 模型（merge-on-write）的选型；
    - 与 ClickHouse 的对比（join 能力、并发能力、运维复杂度、生态兼容 MySQL 协议）；
    - 实时导入链路（Stream Load / Routine Load / Flink connector）的 exactly-once 实现（label 幂等）；
    - 物化视图与 Colocate Join 的加速原理。
16. 场景题：
    - 「实时看板」：Kafka → Flink → OLAP → BI，要求 1 分钟内可见且支持任意维度下钻，设计全链路并说明每环的延迟预算；
    - OLAP 查询 P95 劣化排查：扫描量（分区裁剪失效）、并发排队、compaction 积压、内存限制——排查顺序与治理；
    - 大宽表建模：维表打宽在 Flink 做还是 OLAP 做的权衡（join 时机、更新代价、存储成本）。

## 四、综合架构题（区分架构能力）

17. 画出你们公司的实时数仓全景图（采集 → MQ → 计算 → 存储 → 服务），说明每一环的技术选型理由与故障预案；如果没有全景，说出你要补的三件事。
18. Lambda vs Kappa 架构追问链：双链路口径不一致的经典难题你们怎么解（口径代码共用/对账任务/实时兜底策略）？让你重新设计会选哪个，为什么？
19. 批流一体：Flink 批模式与 Paimon/Iceberg 的批流一体实践到什么程度了，生产可用性你的判断依据？
20. 数据服务化：数仓结果怎么供给业务（API 网关 + 缓存 + 预聚合 + 限流）；一个指标接口 RT 要求 100ms，链路怎么设计？
21. 成本治理：存储（冷热分层/EC/湖表归档）、计算（Spot 实例/队列错峰/任务治理下线）的降本案例，量化收益；
22. 场景题：你是团队第一个大数据工程师，业务有「埋点日志、MySQL 业务库、IoT 时序数据」三类数据，给出第一版数据平台的架构选型与 3 个月建设计划（克制，不要过度设计）。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| Kafka | 25% | ISR/HW、幂等与事务、rebalance 治理到机制层 |
| 数据湖 | 25% | 三格式对比有自己结论、compaction/小文件治理有 SOP |
| OLAP | 25% | 索引与模型原理、实时链路延迟预算、排查体系 |
| 综合架构 | 25% | 双链路一致、成本量化、克制的第一版架构 |

**判定要点**：只会用工具算中级以下；原理到机制层 + 有治理 SOP 算高级；能给三大格式/多引擎下选型结论并承担过迁移/从 0 建平台算资深。
