# data-interview 文件夹说明文档

> 本文档对 `data-interview` 目录的内容进行说明，便于快速了解该大数据面试题库的整体结构与用途。

## 一、目录结构

```
data-interview
├── 01-Hadoop体系.md              # Hadoop 体系（HDFS / MapReduce / YARN）
├── 02-Hive与数仓建模.md           # Hive 与数据仓库建模
├── 03-Spark.md                   # Spark 批处理核心与调优
├── 04-Flink与实时计算.md          # Flink 与实时计算
└── 05-Kafka与数据湖.md           # Kafka、数据湖（Hudi/Iceberg/Paimon）与 OLAP
```

## 二、文件内容概要

| 编号 | 主题 | 适用岗位 | 主要内容 |
|---|---|---|---|
| 01 | Hadoop 体系 | 大数据开发工程师 | HDFS 架构与读写流程（HA/QJM/fencing、pipeline 三副本、机架感知）、块设计与小文件治理、数据可靠性（副本 vs 纠删码）；MapReduce 执行流程与 shuffle 调优、数据倾斜治理；YARN 架构与资源调度器对比（FIFO/Capacity/Fair）；HDFS 运维与调优（Safe Mode、NN 压力指标、Hadoop 3 新特性）。附评估要点表（架构 35% / MR 25% / YARN 25% / 运维 15%）。 |
| 02 | Hive 与数仓建模 | 大数据开发 / 数仓工程师 | Hive 执行链路与引擎选型、表存储（内外部表/分区分桶/ORC/Parquet/压缩）、join 实现方式（Common/Map/Bucket SMB Join）、Hive 数据倾斜专项；数仓建模（范式 vs 维度建模、事实表四类型、星型 vs 雪花、SCD 与拉链表）、数仓分层（ODS/DWD/DWS/ADS）、指标体系、元数据与血缘；HQL 调优清单、调度依赖、数据质量体系（DQC）。附评估要点表（原理优化 35% / 建模 40% / 工程实战 25%）。 |
| 03 | Spark | 大数据开发工程师（批处理/混合岗） | RDD 五大特性与宽窄依赖、血缘容错与 checkpoint、DAG 执行流程、Spark on YARN（client vs cluster）；shuffle 演进史（hash→sort→bypass→Tungsten）、AQE 自适应执行三能力、join 策略（BHJ/SMJ/SHJ）；统一内存管理、OOM 分类排查、executor 资源配比、Kryo/GC；性能优化方法论、数据倾斜 Spark 版、Streaming 与结构化流、场景题（1TB join 优化）。附评估要点表（模型 20% / shuffle 30% / 内存 25% / 优化 25%）。 |
| 04 | Flink 与实时计算 | 实时/流式大数据开发 | 运行时架构（JobManager/TaskManager/slot、ExecutionGraph、反压 credit 流控）；时间语义与 Watermark（乱序、多并行度传播、allowedLateness、窗口类型与增量聚合）；状态管理（Keyed/Operator State、状态后端 HashMap vs RocksDB、TTL、大状态 join）；容错与一致性（Checkpoint 与 barrier 对齐、unaligned checkpoint、端到端 exactly-once 三要素、TwoPhaseCommitSink、Savepoint 升级）；Flink SQL（retract 流、双流 join、CDC、实时数仓分层）、资源稳定性与场景题。附评估要点表（运行时 20% / 窗口 20% / 状态 25% / 容错 25% / SQL 10%）。 |
| 05 | Kafka、数据湖与 OLAP | 大数据开发（进阶） | Kafka 架构（ISR/HW/LEO、controller、KRaft）、高吞吐原理（顺序写/零拷贝/批量压缩）、生产端与消费端调优（acks/幂等事务/rebalance/积压 SOP）、顺序性与分区设计；数据湖三格式深度对比（Hudi/Iceberg/Paimon 的快照模型、COW vs MOR、主键 upsert、选型结论）、湖表与引擎协同、Schema 演进；OLAP（ClickHouse 稀疏索引与 MergeTree、Doris/StarRocks MPP 模型、实时导入 exactly-once、场景题）；综合架构题（实时数仓全景、Lambda vs Kappa、批流一体、数据服务化、成本治理）。附评估要点表（Kafka 25% / 数据湖 25% / OLAP 25% / 综合架构 25%）。 |

## 三、内容特点

1. **追问链式组织**：每个主题下的知识点均以「必考点 → 追问链 → 场景题」的结构展开，从基础概念逐步深入机制层（如 HDFS 读写 → 一致性模型 → 小文件治理量化）。
2. **附评估要点表**：每个文件末尾附「维度 / 权重 / 观察点」表格与「判定要点」，明确入门 / 中级 / 高级 / 资深的评判标准，可直接用于面试评分。
3. **侧重实战与量化**：强调真实治理案例（小文件治理收益、1TB join 优化、倾斜案例前后耗时对比、checkpoint 失败排查等），区分候选人的实战深度。

## 四、使用建议

1. **按主题复习**：按 01 → 05 顺序通读，先自答「追问链」中的问题，再对照各文件附带的评估要点检验掌握程度。
2. **面试官视角**：可直接将各文件的追问链与场景题作为面试执行脚本，并按「评估要点」表打分定级。
3. **重点优先级**：Hadoop（01）是基础必考，Hive 数仓（02）与 Spark（03）覆盖批处理岗高频考点，Flink（04）与 Kafka/数据湖（05）面向实时/进阶岗位；投递实时或进阶岗位时应重点准备 04、05。
