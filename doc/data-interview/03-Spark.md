# Spark 面试题

> 适用岗位：大数据开发工程师（批处理/混合岗）。RDD 模型、Shuffle、内存管理是三座山，调优与倾斜是区分度所在。

---

## 一、核心模型（必考）

1. RDD 追问链：
   - 五大特性逐个讲（分区列表、依赖、计算函数、分区器、最优位置），它们分别服务于什么机制；
   - 宽窄依赖的判定标准与意义（stage 划分、容错恢复粒度）；
   - 血缘（lineage）容错：某个 partition 丢了怎么恢复？checkpoint 与血缘截断的时机选择（血缘太长 + shuffle 太重的作业为什么该 checkpoint）？
2. DAG 执行流程：application → job（action）→ stage（宽依赖切分）→ taskset → task；`cache/persist` 的级别选择（MEMORY_ONLY_SER、MEMORY_AND_DISK）与缓存失效重算问题；`cache` 不触发的原因（lazy 的坑）。
3. Spark 与 MapReduce 的本质区别，回答到"内存迭代 + shuffle 差异 + 线程模型"三层才算过关；什么场景 MR 反而更稳（超大对象、极端内存受限环境）？
4. Spark on YARN 两种模式：client vs cluster 的区别（Driver 位置、日志、提交机依赖）；生产为什么用 cluster？K8s 模式了解吗？

## 二、Shuffle（必考，区分度最高）

5. Shuffle 演进史追问链：
   - hash shuffle（每个 map task 生成 R 个文件）为什么被废弃；
   - sort shuffle 的 map 端排序溢写 + 索引文件；
   - bypass 机制的触发条件（分区数 < 200 且无 map 端聚合）；
   - Tungsten 排序（序列化数据直接排序，反序列化比较器）的优化点；
6. Shuffle 读写的调优参数：`spark.shuffle.file.buffer`、`spark.reducer.maxSizeInFlight`、分区数怎么定（shuffle.partitions 与数据量的比例关系、AQE 前后的差异）；
7. AQE（自适应执行）追问链（Spark 3 必考）：
   - 三个能力：分区合并（倾斜 + 小分区）、join 策略切换（SMJ → BHJ）、倾斜 join 拆分（`skewJoin` 阈值三参数）；
   - AQE 打开后，传统"手动加盐治倾斜"还需要吗？哪些场景 AQE 治不了（热点 key 需要广播、聚合型倾斜）？
8. Join 策略追问链：BHJ（广播）→ SMJ（排序合并）→ SHJ（shuffle hash）的选择条件与代价；广播阈值（`autoBroadcastJoinThreshold`）调大有什么坑（driver OOM、广播超时）；unstable hash join / union 等场景下广播失效排查。

## 三、内存与资源（必考）

9. 统一内存管理追问链：
   - executor 内存布局：reserved + user memory + 统一内存（storage / execution 动态借用）；
   - `spark.memory.fraction`、`spark.memory.storageFraction` 的含义与调整场景；
   - OOM 分类：driver OOM（collect 大结果、广播超限）、executor OOM（task 并发 × 单 task 内存）——各自的排查路径；
10. Executor 资源配置方法：核心数 × 内存 × 并发 task 的关系（1 core 1 task）、`spark.executor.cores/memory/cores.max` 与 YARN container 大小的匹配（超过单 NM 可分配的坑）、动态分配（`dynamicAllocation` + external shuffle service 的原理）；
11. 序列化与 JVM：Kryo 注册的收益与注意点；GC 类型判断（cache 多的作业 CMS/G1 的选择、Spark on JDK17 的 ZGC 实践）；堆外内存（off-heap）在什么场景打开。

## 四、性能优化与实战（区分度）

12. 性能优化方法论清单：数据读入优化（列裁剪/谓词下推/Parquet）、分区裁剪、广播小表、repartition 与 coalesce 的区别与滥用、`foreachPartition` 批量写库、序列化、缓存复用、文件数治理——面试官会挑 2~3 个让你展开细节。
13. 数据倾斜 Spark 版（必考）：两阶段聚合（加盐扩容）、map join 广播、倾斜 key 单独处理（union 补集思路）、`sample` 采样定位热点 key 的代码写法；AQE 时代你还手写这些吗，什么时候仍需要？
14. Spark Streaming（DStream） vs Structured Streaming：微批（批次间隔、反压 `backpressure`）vs 连续处理；window、watermark、输出语义（at-least-once / exactly-once 的实现条件）；
15. 与 Hive/数仓协同：Hive on Spark 中 Spark 参数与 Hive 的关系；动态分区插入的小文件与内存问题（`dynamicPartitionPruning`、排序分发 insert）；
16. 场景题：
    - 一个 1TB 的 join 任务从 4 小时优化到 30 分钟，写出你的完整优化过程（执行计划 → 倾斜 → shuffle → 资源）；
    - Spark 作业频繁出现 "Fetching data failed" / shuffle lost，怎么排查（executor 丢失原因、外部 shuffle 服务、磁盘 IO）；
    - 让你设计一个「每日 10 亿级用户行为明细与维表关联 → 聚合 → 回写 Hive」的任务，给出作业结构与参数模板。
17. 生态对比：Spark vs Flink 的批流能力对比（窗口/状态/ Exactly-once 实现、batch 支持成熟度）；什么团队该选什么（混合负载用 Spark，实时优先用 Flink）——给出你的判断依据。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 核心模型 | 20% | RDD 特性、stage 划分、checkpoint 时机 |
| Shuffle 与 AQE | 30% | 演进史 + AQE 三能力，能说清"治了什么治不了什么" |
| 内存与资源 | 25% | 统一内存布局、OOM 分类排查、资源定值有依据 |
| 优化实战 | 25% | 倾斜治理成体系、有量化案例、会读执行计划 |

**判定要点**：只会 `cache + repartition` 算中级以下；能讲清 shuffle 演进与 AQE 边界、OOM 分类定位算高级；有 1TB+ 级作业从执行计划到资源层的完整优化案例、主导过 Spark 版本升级或平台化调优的算资深。
