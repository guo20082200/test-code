# Elasticsearch 面试题

> 适用岗位：中高级后端 / 搜索与存储方向。考察建议：先问使用场景判断真实度，再沿追问链下探原理；运维题结合规模追问（多少节点、多少数据量）。

---

## 一、核心原理（必考）

1. 倒排索引是什么？与 MySQL 的 B+ 树索引在「检索模型」上的本质区别？什么数据适合放 ES，什么不适合？
2. 文档写入的完整链路追问链（必考）：
   - 写请求到 coordinator 节点后怎么路由到 shard？`routing` 默认算法是什么，自定义 routing 有什么用途与风险？
   - index buffer → refresh（近实时的由来，默认 1s）→ segment 生成 → translog 落盘 → flush 的完整顺序；
   - refresh 和 flush 的区别？`refresh_interval` 调大有什么收益与代价？`?refresh=wait_for` 什么时候用？
   - segment 是不可变的：删除和更新是怎么实现的（.del 标记 / 新 version 文档）？物理删除发生在什么时候（merge）？
3. 深分页问题追问链（必考）：
   - `from + size` 为什么默认限制 10000？每个分片要取 from+size 条再归并的成本讲清楚；
   - `search_after`、`scroll` 的原理与适用场景；scroll 为什么不适合实时翻页（快照、游标成本）？
   - ES 7.10+ 的 PIT（point in time）+ search_after 组合与 scroll 对比？
   - 业务要求「跳到第 5 万页」这种需求你怎么办（限制页深 / 预计算）？
4. 相关性评分：TF-IDF → BM25 的演进原因？BM25 的 k1、b 参数分别控制什么？`function_score` / 自定义评分什么时候用？
5. 分析器（analyzer）链路：character filter → tokenizer → token filter；中文分词怎么选（IK、pinyin 自定义）？`_source` 与 stored fields 的区别？

## 二、集群与分片架构

6. 分片规划追问链（架构师必问）：
   - 分片数为什么建索引时就要定好（不可动态改）？分片过大/过小各有什么问题（merge 压力 / 开销与分布不均）？
   - 一个 shard 多大合适？节点数、分片数、堆内存的经验比例？
   - 时间序列数据（日志/监控）怎么规划：按天/月建索引 + ILM（rollover、shrink、force merge、delete）？
7. 副本与高可用：primary 副本故障转移流程？副本数读写性能的影响？脑裂在新版（7.x quorum 选举 + voting configuration）下还需要配置吗？
8. 集群状态管理：master 选举、cluster state 的广播机制（元数据大时的痛点）？大集群（百节点+）的扩展建议？
9. 写一致性：`wait_for_active_shards`、写入时 primary 与 replica 的同步等待策略？不一致窗口怎么理解？
10. 分片再平衡与数据迁移：reindex、`_shrink`/`_split`、`_clone` 分别什么场景用？在线扩容迁移对业务的影响与限流手段？

## 三、查询与聚合实战

11. 查询执行原理：query phase 与 fetch phase 分别做什么？`_shard_size` 对精准度的影响？
12. filter 与 query 上下文的区别（是否算分 + 是否缓存）？你们的查询语句是全放 query 还是尽量放 filter，为什么？
13. 聚合追问链：
    - 桶聚合（terms）、指标聚合、嵌套聚合的经典结构；
    - terms 聚合的 `size` 与 `shard_size`：为什么分片环境下基数不准？`doc_count_error_upper_bound` 怎么读？
    - cardinality（基数）聚合的 HyperLogLog++ 误差怎么权衡？
    - 聚合内存爆炸的常见原因（高基数 terms、deep nesting、global ordinals）与治理；
    - 聚合排序优化：`execution_hint: map` 什么时候比 global ordinals 快？
14. join 类需求的取舍：`nested` vs `parent/child` vs 宽表冗余——检索性能、更新成本、存储成本的三角权衡，你们选哪个，为什么？
15. 深度优化：索引别名与零停机切换（alias + reindex）；`multi-index` 查询与路由优化；function_score 做业务加权排序的坑。

## 四、性能调优与运维（结合规模追问）

16. 写入调优清单（尽量多说）：批量大小怎么定（以 MB 调不以条数）、并发数、refresh_interval、`index.translog.durability: async` 的风险、副本先置 0（什么场景才敢用）、mapping 精简（禁 dynamic mapping 失控）？
17. 查询调优清单：只取需要的字段（`_source` 过滤 / docvalue_fields）、避免 script 计算、profile API 怎么用、慢查询日志定位？
18. JVM 与资源：ES 堆为什么不要超过 32GB（指针压缩）？堆外内存（Lucene 的 page cache）怎么规划？`fielddata` 堆溢出排查？
19. 大规模治理经验（有真实经验者的区分题）：
    - 你负责的集群多大（节点数/日增数据/总数据）？出过什么事故，怎么解决的？
    - 磁盘水位（85%/90%/95% 三条线）触发什么行为？磁盘红了怎么紧急处理？
    - 热点节点排查（负载不均：分片大小不均 / 热数据 / routing 倾斜）；
    - 集群状态 RED/YELLOW 的排查步骤（`_cat` 系列命令）。
20. 安全与运维：快照备份策略（`_snapshot` + SLM）；跨集群复制（CCR）什么场景需要？升级集群的滚动重启步骤与注意事项？

## 五、场景与架构题（区分架构理解）

21. 设计一个商品搜索系统：
    - 商品结构怎么 mapping 设计（多字段：中文分词 + 拼音 + brand 精确匹配）？
    - 搜索提示（Suggest/Completion）、纠错、同义词词库的更新（同义词在线生效的问题）；
    - 筛选 + 排序 + 分页的整体查询方案；MySQL 与 ES 的数据同步怎么做（Canal/MQ + 防丢失对账）？
22. 设计一个日志平台（类 ELK）：
    - Filebeat → Kafka → Logstash/Ingest → ES 的链路与削峰；
    - ILM 滚动策略（hot-warm-cold 架构，冷节点用可搜索快照）；
    - 日志量突增的自动保护（索引速率限制、丢弃策略）。
23. DB 与 ES 双写一致性追问链（高频）：
    - 同步方案选型：双写（为什么不可靠）、MQ 异步、binlog 订阅（Canal）——各自的一致性窗口与故障恢复；
    - 同步延迟的业务影响怎么掩盖（前端查询路由 / 降级查主库）？
    - 数据不一致怎么发现与修复（全量对账 + 增量补偿）？
24. ES 不擅长的场景盘点：强事务、频繁更新文档（reindex 成本）、深度 JOIN、毫秒级强一致读——面试官会拿"为什么不用 ES 解决 XX"反问你，你怎么答？
25. 替代品对比：OpenSearch（AWS 分叉）、ClickHouse（日志检索的挑战）、Tantivy/自研内存检索——什么信号出现时你会考虑替换 ES？

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 核心原理 | 30% | 写入链路、深分页、segment 机制是否讲透（区别于背文档） |
| 集群架构 | 20% | 分片规划、容量模型、是否真管过集群 |
| 查询聚合 | 20% | 精准度问题、聚合治理、mapping 设计能力 |
| 调优运维 | 20% | 量化经验（规模数据）、事故复盘 |
| 场景设计 | 10% | 同步一致性、冷热分层、选型边界 |

**判定要点**：只说过"ES 就是搜索数据库"算入门；能讲清 refresh/flush/merge 与深分页原理算中级；有规模数据（节点/数据量）、出过事故有复盘、能做分片规划与同步一致性设计的算高级。
