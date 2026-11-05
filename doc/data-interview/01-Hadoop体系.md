# Hadoop 体系面试题（HDFS / MapReduce / YARN）

> 适用岗位：大数据开发工程师。虽是"老三样"，但分布式存储与计算的地基，面试官常用它判断基础是否扎实。

---

## 一、HDFS（必考）

1. HDFS 架构追问链：
   - NameNode/DataNode 职责；NameNode 元数据放哪（内存 + fsimage + edits），SecondaryNameNode 到底干什么（不是热备！）？
   - 高可用演进：单点 → NN HA（QJM 双 NN + JournalNode 多数派）→ Federation（命名空间横向扩展），各解决什么问题？
   - 脑裂怎么防（fencing：sshfence / shellfence）？
2. 读写流程追问链：
   - 写流程：pipeline 三副本的复制顺序、ack 机制、节点选择（本机/同机架/跨机架）——机架感知策略为什么这么设计？
   - 读流程：就近读原理；短路读（short-circuit）优化了什么？
   - 一致性模型：HDFS 为什么只保证"写一次读多次"？append 场景的代价？
3. 块设计：为什么 128MB（寻址时间/传输时间比例）？块大小与文件数量、Map 数量的联动关系？小文件问题的根因与治理方案：
   - NN 内存压力、Map 任务数爆炸、数据倾斜读取；
   - 治理：CombineFileInputFormat、SequenceFile/Parquet 合并、HAR、NameNode Federation——你们线上小文件治理做了什么，量化收益？
4. 数据可靠性：副本策略（3 副本）vs 纠删码（EC，1.5 倍存储）的取舍（CPU 恢复开销、小文件不适用）？磁盘坏块检测与副本恢复流程？
5. 场景题：某个 DataNode 磁盘满了 / 某台机器下线维护，HDFS 侧要做哪些操作，怎么观察数据再平衡进度？

## 二、MapReduce（理解计算模型即可）

6. MR 作业执行流程追问链：
   - split → map → shuffle → sort → reduce 的完整链路；
   - shuffle 是核心：map 端环形缓冲区（默认 100MB、80% 溢写）、分区、溢写排序、merge；reduce 端拉取、归并——每个参数的调优方向；
   - combiner 什么时候能加什么时候不能（求和可以、均值不行）？
7. 数据倾斜追问链（大数据通用必考）：
   - 现象与定位（大部分 map/reduce 很快、个别任务 99% 卡住；counters 观察）；
   - 常见解法：key 加盐打散 + 两次聚合、map 端 join（小表广播）、自定义分区、热点单独处理；
   - 追问：null 值、空字符串、默认值造成的倾斜怎么治（过滤 / 随机前缀）？
8. MR 为什么被 Spark/Flink 替代：磁盘 shuffle vs 内存 RDD、task 进程 vs 线程、DAG vs 线性管道——从架构说明本质差距。

## 三、YARN

9. YARN 架构与调度流程：ApplicationMaster 的职责、Container 生命周期、AM 重启后作业恢复（MR vs Spark 差异）？
10. 资源调度器对比：FIFO / Capacity Scheduler / Fair Scheduler——多队列隔离、弹性借用、队列 AM 占比上限，大厂为什么普遍用 Capacity/Fair 多队列？
11. 资源模型：内存 + CPU 两维资源、`mapreduce.map.memory.mb` 与 JVM `-Xmx` 的关系（容易被 OOMKill 的坑）、vcores 超卖（`yarn.nodemanager.resource.cpu-vcores`）？
12. 场景题：
    - 队列资源被一个大作业吃满，其他业务排队——怎么从调度配置和作业侧双管齐下？
    - Driver/AM 频繁被杀（超出 AM 资源限制 / 磁盘满），排查路径？

## 四、HDFS 运维与调优（区分实战深度）

13. Safe Mode 是什么？重启集群长时间停在 safe mode 的原因（块报告未完成 / 副本不足）？
14. NN 压力指标：RPC 队列、open files 数量、块报告耗时——监控哪些项？`fsimage` 越来越大的治理？
15. Hadoop 3 的新特性你用过哪些：EC、联邦 Router、NN RPC 的观察者读（observer read）、HDFS 磁盘均衡器（diskbalancer）？
16. 场景题：让你接手一个 500 节点 Hadoop 集群，前两周你会做哪些健康检查与治理（版本/补丁、水位、小文件、队列、坏盘、慢节点）？

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| HDFS 架构与读写 | 35% | HA/fencing、pipeline 写入、小文件治理有量化 |
| MapReduce 模型 | 25% | shuffle 每一环讲得清、倾斜解法成体系 |
| YARN | 25% | 调度器实战、资源参数的坑 |
| 运维深度 | 15% | 有节点规模数据、有真实治理案例 |

**判定要点**：只会"HDFS 三副本、MR 分 map/reduce"算入门；能把 shuffle 讲到环形缓冲区与溢写合并、倾斜治理有自己套路算中级；有集群规模数据、EC/联邦等高级特性实战、能接手治理一个存量集群算高级。
