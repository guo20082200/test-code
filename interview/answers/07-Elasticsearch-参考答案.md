# Elasticsearch 参考答案

> 以下为「要点框架 + 关键细节 + 权衡取舍」，面试需结合个人集群规模与实战展开。原题见 ../07-Elasticsearch.md

---

## 一、核心原理（必考）

### 1. 倒排索引 vs B+ 树

> 原题：倒排索引是什么？与 MySQL B+ 树在检索模型上的本质区别？什么数据适合放 ES，什么不适合？

- **倒排索引**：对每个 term 维护「词 → 含该词的文档列表（posting list）」，posting 里是有序 docId、词频（打分）、位置（phrase 查询）；查询是**集合运算**（多 term 求交/并/差），成本与命中集合大小相关，与总文档量弱相关。
- **B+ 树**：按键值有序组织的正排结构，擅长等值/范围/前缀查找；对 `like '%词%'`、多条件自由组合、相关性排序天然无解；倒排则是为「词找文档 + 打分 + 聚合」而生。
- 本质区别一句话：B+ 树是**有序查找模型**（点查/范围查），倒排是**集合运算 + 相关性模型**（布尔组合 + BM25）。
- 类型分工：`text` 走倒排；`keyword` 排序/聚合走 **doc_values（列存，堆外）**；数值/日期/IP/geo 走 **BKD 树**（block KD-tree），支持高效多维范围查询——面试加分点。
- 适合放 ES：全文检索、多条件过滤 + 排序 + 聚合（商品搜索、日志/监控/APM）、近实时写多读少的检索投影。
- 不适合：强事务/ACID、频繁 update（segment 不可变，更新=删+写）、深度 JOIN、毫秒级强一致读、按主键精确点查为主的数据（MySQL 更划算）。

### 2. 文档写入完整链路

> 原题：routing 路由算法与自定义风险；buffer→refresh→segment→translog→flush 顺序；refresh vs flush、refresh_interval、wait_for；segment 不可变下删除/更新与 merge

- **路由**：`shard = hash(routing) % number_of_primary_shards`，routing 默认取 `_id`；这也是主分片数建索引后不可改的根因（改了 hash 取模全错位）。
- 自定义 routing：多租户/按用户定向，让同一租户落同一分片，查询可指定 routing 只查 1 个分片、大幅降低 fanout；**风险**：数据倾斜（大租户热点）、写入/查询/重建索引必须都带上 routing，否则查不到。
- **写入顺序**（primary 分片所在节点）：
  1. coordinator 按路由转发到 primary；
  2. 文档进入 **index buffer（内存）**，同时追加 **translog**（事务日志）；
  3. **refresh**（默认 1s）：buffer 生成新 segment（进入文件系统缓存，此刻可被搜索）——这就是「近实时 NRT」的由来；
  4. translog 按 `index.translog.durability` 落盘：`request`（默认，每次写请求 fsync）/ `async`（周期 fsync，掉电丢窗口数据）；
  5. **flush**（Lucene commit，translog 默认 512MB 或周期触发）：fsync 所有 segment 落盘、生成 commit point、清空 translog——这是故障恢复的锚点；
  6. 后台 **merge** 持续合并小 segment。
- **refresh vs flush**：refresh = 搜索可见性（内存→segment）；flush = 持久化 + 清 translog（恢复点）。refresh 太频繁→小 segment 多、merge 压力大；flush 太频繁→磁盘 IO 高、恢复慢。
- `refresh_interval` 调大（30s~）：写入吞吐↑、segment/merge 压力↓，代价是**可见性延迟**；全量导入期可 `refresh_interval:-1` + 副本 0，导完改回再强制 refresh。
- `?refresh=wait_for`：等下一次 refresh 后返回——「写后立刻要查到」的读己之写场景；代价是请求被拉长到 1s 级，别在批量导入用。
- **segment 不可变**：删除 = 写 `.del` 标记，搜索时过滤掉；更新 = 旧文档标删 + 新 version 文档写入（`seq_no + primary_term` 乐观锁防并发冲突）；**物理删除只发生在 segment merge 时**（这也是频繁更新放大 IO/磁盘占用的根源）。

### 3. 深分页

> 原题：from+size 为何限 10000；search_after/scroll 原理与适用；PIT 对比 scroll；跳第 5 万页怎么办

- **from+size 为什么慢且默认限 10000**：每个分片都要取出 `from+size` 条送协调节点归并，最坏要处理 `分片数 × (from+size)` 条；页越深内存/CPU 成本越线性放大，`index.max_result_window=10000` 是自我保护。
- 三种方案对比：

| 方案 | 原理 | 适用 | 缺点 |
|---|---|---|---|
| from+size | 各分片取 from+size 归并 | 浅页、允许跳页 | 深页成本线性涨，限 10000 |
| scroll | 打开时固定 segment 视图（快照语义）+ scroll_id 游标逐批拉 | 全量导出、离线遍历 | 数据是快照不实时；每份上下文占 search context 内存，游标堆积可拖垮集群；不适合实时翻页 |
| search_after（+PIT） | 用上一页最后一条的 sort 值接着往下查 | 实时深翻页、推荐/信息流 | 只能顺序翻不能跳页；无 PIT 时并发写入可能重复/漏 |

- **scroll 不适合实时翻页**：①视图是打开瞬间的快照，翻页中看不到新写入；②scroll_id 对应的服务端上下文占资源且必须及时 clear，实时高并发翻页会堆积大量上下文。
- **PIT（7.10+）+ search_after vs scroll**：PIT 只轻量记录「当前 segment 列表视图」，无 scroll_id 重上下文，资源开销小、支持 slice 并行遍历，官方明确推荐用 PIT 替代 scroll 做一致性视图分页。
- **跳第 5 万页**：先质疑需求——产品层限制页深（电商普遍只给前 100 页 + 上/下页 + 筛选收敛）；确需任意跳页的榜单类用**预计算**（定时物化排序结果到 MySQL/Redis 供翻页）；或离线算好深页结果异步物化。ES 层硬扛深页是不划算的。

### 4. 相关性评分：TF-IDF → BM25

> 原题：演进原因；k1、b 控制什么；function_score 何时用

- TF-IDF 两个缺陷：**TF 无饱和**（词出现 1000 次比 100 次得分线性放大，易被关键词堆砌刷分）、**无文档长度归一**（长文档天然占优）。
- BM25 修正：**词频饱和**（TF 贡献有上限曲线）、**文档长度归一化**、IDF 平滑——5.x 起默认。
- **k1（默认 1.2）**：控制词频饱和速度，越大 TF 影响越接近线性；**b（默认 0.75）**：文档长度归一强度，0=完全不归一，1=完全按长度惩罚。经验：短字段（标题）可调低 b，避免长标题被过度惩罚。
- **function_score**：把业务信号（销量、新鲜度、距离、人工权重）乘/加进相关性——电商排序 = 召回相关性 × 业务权重粗排；常用 `field_value_factor`（log1p 销量）、`gauss`（时间衰减）、`weight`。坑：字段缺失要给 `missing` 默认值、`score_mode/boost_mode` 语义易搞错、大结果集上 script 每文档执行很慢（能预计算就别算分时算）。

### 5. 分析器与 _source

> 原题：char filter → tokenizer → token filter；中文分词选型；_source 与 stored fields 区别

- 链路三段：**character filter**（HTML 清洗、字符映射）→ **tokenizer**（切词，产出 term + position/offset）→ **token filter**（小写、停用词、同义词、词干化）。
- 中文分词：**IK** 常用「`ik_max_word` 建索引（细粒度全切）+ `ik_smart` 搜索（粗粒度）」，提升召回同时控制噪音；**pinyin** 分词器做拼音提示/搜索；专名识别差时上自定义词库（支持热更新）。注意 analyzer 索引侧与 search_analyzer 查询侧可分开配置。
- `_source`：写入时存的**原始 JSON 全文**（Lucene stored fields，压缩），用于返回结果、update、reindex、高亮；关掉后省空间但文档不可 update/重建来源。
- stored fields：显式 `store:true` 的字段，只在「_source 很大但每次只取个别字段」或需要单独高亮时用；**排序/聚合不走 stored，走 doc_values 列存**——这是常见混淆点。
---

## 二、集群与分片架构

### 6. 分片规划

> 原题：分片数为何建索引时定死；过大/过小的问题；大小与节点/堆内存经验比例；时间序列 ILM 规划

- 分片数建索引后不可改（routing 取模依赖它），只能 reindex / `_split` / `_shrink`——容量规划必须前置。
- **过大**：恢复/迁移/rebalance 慢（都是分片粒度搬运）、单分片 merge 与查询压力大、故障恢复时间长；**过小**：分片数量爆炸→每查询 fanout 高、cluster state 膨胀、master 与 merge 开销高、数据分布易不均。
- 经验比例（口述版）：
  - 单分片 10~50GB：搜索类偏小（10~20GB 保延迟），日志类偏大（30~50GB 省开销）；
  - 单节点分片数 ≤ **堆内存 GB × 20**（官方经验值），并盯住 `cluster.max_shards_per_node` 上限；
  - 规划顺序：总数据量 → 单分片目标大小 → 主分片数 → 按 QPS 与 fanout 反推节点数与副本数。
- **时间序列**（日志/监控）：按时间（天/小时）建索引 + 写别名 + **ILM**：hot（rollover 按 max_age / max_primary_shard_size / max_docs）→ warm（shrink 合并小索引、forcemerge 到 1 个 segment、只读、降副本）→ cold（可搜索快照/冻结层）→ delete（按留存期）；rollover 靠 `is_write_index` 别名无缝切换，全程业务无感。

### 7. 副本与高可用

> 原题：primary 故障转移流程；副本数对读写的影响；7.x 下还要配脑裂吗

- primary 所在节点失联 → master 从 **in-sync 副本集合**中提升新 primary（基于 seq_no 对齐保证不丢已确认写入），随后集群自动补建新副本。
- 副本读：可分摊读流量、提升搜索吞吐；副本写：每次写要同步到全部副本并等待确认，副本越多写越慢、存储翻倍；副本 0 常用于导入/reindex 提速（前提：数据可重建）。
- **脑裂**：7.x 起选举改为类 Raft 的 quorum 机制（须过半 master-eligible 投票），voting configuration 由集群自动维护，`minimum_master_nodes` 已废除——**不再需要手动配置，但部署纪律仍要守住：生产至少 3 个 master-eligible，且不宜过多**。

### 8. 集群状态管理

> 原题：master 选举与 cluster state 广播机制；元数据大时的痛点；百节点+扩展建议

- master-eligible 基于 term/投票选主；**cluster state（mapping、索引设置、分片路由表）由 master 发布并推送**到所有节点（内存中发布 + diff 增量传输）。
- 痛点：分片数到十万级时 cluster state 巨大，任何 mapping 变更/新建索引都触发发布，慢且占 master/CPU，发布超时会引发选主抖动。
- 大集群建议：**专用 master**（3~5 台小规格，与 data 角色分离）；严控总分片数与索引数量（模板收敛、禁止随意动态建索引）；hot/warm/cold 分层；超过单集群舒适区（百节点、几十万分片）用**多集群 + 跨集群搜索（CCS）**水平拆分。

### 9. 写一致性

> 原题：wait_for_active_shards；primary 与 replica 同步等待策略；不一致窗口怎么理解

- `index.write.wait_for_active_shards`：写入前要求活跃分片数达标（1 / quorum / all），不达标直接拒绝写——在「可用性」与「写入冗余度」之间做开关，默认 1（只要 primary 活着就能写）。
- 同步策略：primary 写成功后**并行**转发所有 in-sync 副本，等全部确认才向客户端返回成功；副本超时/失败不会回滚 primary——master 把掉队副本踢出 in-sync 集合并触发恢复，之后按 seq_no 补齐。
- 不一致窗口：①refresh 前 1s 不可见（NRT）；②副本短暂落后时读副本可能读到旧数据（最终一致收敛）；③故障切换瞬间新 primary 已含全部已 ack 写入（seq_no 机制），未 ack 的写入需客户端重试（seq_no 幂等去重）。

### 10. 分片再平衡与数据迁移

> 原题：reindex、_shrink/_split/_clone 各自场景；在线扩容对业务影响与限流

- **reindex**：跨索引复制文档——mapping 变更、分片数变更、跨版本迁移都靠它；`slices=auto` 并行（内部 scroll+bulk），配别名切换实现零停机。
- **_shrink**：N 分片收敛为因子数（海量小索引治理）；**_split**：按因子放大（预建小索引后扩分片）；**_clone**：同分片数克隆。三者都要求源索引只读、副本 0、且先把源分片收敛到单节点。
- 在线扩容影响：新节点加入触发 rebalance，恢复流量占满磁盘/网络造成查询毛刺。限流手段：`cluster.routing.allocation.enable: primaries`（先只迁 primary）、`indices.recovery.max_bytes_per_sec` 限恢复带宽、`node.initial_primaries_recoveries` / `cluster.routing.allocation.node_concurrent_recoveries` 限并发、业务低峰分批加节点。
---

## 三、查询与聚合实战

### 11. query/fetch 两阶段

> 原题：query phase 与 fetch phase 分别做什么；_shard_size 对精准度的影响

- **query phase**：各分片本地执行查询，只返回本地 top N（N = from+size）的 **docId + score + sort value** 给协调节点；注意打分用的是分片本地词频，跨分片 TF/IDF 有偏差（分片越小越不准），`DFS query_then_fetch` 用全局词频修正但代价大、很少用。
- **fetch phase**：协调节点归并出全局 top N 后，回源到对应分片批量拉取 `_source` 等内容——「选文档」在 query 阶段、「取内容」在 fetch 阶段；深分页的开销主要在 query 阶段的归并。
- `_shard_size`（terms 聚合语境）：每个分片先返回的桶数，默认 `size×1.5+10`；返回太少→全局 top 桶可能被个别分片漏掉/低估（低频但全局靠前的桶挤不进协调端归并池），**调大更准、归并成本更高**；判断精准度先看 `doc_count_error_upper_bound` 与 `sum_other_doc_count`（详见第 13 题）。

### 12. filter 与 query 上下文

> 原题：是否算分 + 是否缓存；你们是全放 query 还是尽量放 filter

- query 上下文：算 `_score` 相关性；filter 上下文：不评分（0/1），结果可被**节点级 query cache 缓存**（segment 级 bitset），复用后近乎免费，还省打分开销。
- 实践答案：**能放 filter 的全放 filter**——状态、类目、品牌、时间范围、价格区间等全走 filter 子句，只有文本相关性（标题/描述 multi_match）放 must/should。理由：不算分省 CPU + 高频过滤条件命中缓存 + bool 优化空间更大。
- 相关缓存还有分片级 **request cache**（缓存 size=0 的聚合/总命中数结果），对聚合型看板查询收益大。

### 13. 聚合追问链

> 原题：桶/指标/嵌套结构；terms 的 size 与 shard_size、doc_count_error；cardinality 误差；聚合内存爆炸与治理；execution_hint: map

- 经典结构：`terms`（类目桶）→ 内嵌 `avg/sum/max`（指标）→ 再嵌 `terms`（品牌）；nested 字段要先套 `nested` 聚合；`date_histogram` + 嵌套指标做时间趋势。
- **为什么分片环境下基数不准**：每分片只交回 shard_size 个桶，协调端从「分片数 × shard_size」的池里归并出 top size；某 term 若在多数分片排不进各自前 shard_size，计数被低估甚至整桶丢失——**分片越多越不准**。
  - `doc_count_error_upper_bound`：协调端估计的计数误差上界（= 各分片「刚好没被返回的下一个桶」计数之和的最大影响），值越小越可信，为 0 表示精确；
  - `sum_other_doc_count`：没进 top size 的剩余文档量，太大说明 size 不够或数据发散；
  - 治理：调大 shard_size（如 size×3）、减少分片数；要求绝对准的榜单走预聚合索引。
- **cardinality（HyperLogLog++）**：`precision_threshold`（0~40000，默认 3000）——阈值内近似精确，阈值外标准误差约 2%~5%，内存约 threshold×8 字节；UV 统计按业务容忍度选，通常够用且远比精确去重省。
- **内存爆炸三因**：
  - 高基数 terms（百万桶）→ 改 `composite` 分页聚合、限制 size、拆时间窗；
  - 深层嵌套聚合 → 限制深度、拆多次查询；
  - **global ordinals**（keyword 聚合要构建全局序表，首次构建慢且占堆，refresh 频繁时反复重建）→ 调大 refresh_interval、按需 `eager_global_ordinals`；breaker（`indices.breaker.total.limit`）当护栏。
- **`execution_hint: map`**：命中文档少且基数低时，用哈希 map 现场计数，跳过 global ordinals 构建反而更快；大结果集/高基数字段仍用默认 global ordinals。辅助：terms 上加 `include` 正则或 partition 控制桶数。

### 14. join 类需求的取舍

> 原题：nested vs parent/child vs 宽表冗余的检索性能/更新成本/存储成本权衡；你们选哪个

- **nested**：对象数组索引成隐藏子文档，支持 nested 查询/聚合，检索性能好、语义清晰；缺点：改一个 SKU 也要重写整个父文档（更新放大），文档数=商品×SKU（存储放大）。
- **parent/child**：父子文档独立可单独更新（更新成本最低）；查询靠 has_child/has_parent，依赖全局序表，**检索性能最差**、聚合受限；只适合子对象高频独立更新且检索需求弱的场景。
- **宽表冗余**：同步层拍平，把常用筛选预聚合进商品文档（最低价、价格区间、颜色/尺码 keyword 数组、类目路径）；检索最快、结构最简；代价是更新放大与存储冗余。
- 我们的选型：**商品搜索主索引 = 宽表冗余为主 + 局部 nested**（需要 SKU 组合级筛选时才用）；评论数、库存这类高频变化字段不进主索引（放 Redis/旁路），避免更新放大打爆写入链路。

### 15. 深度优化

> 原题：索引别名与零停机切换；multi-index 查询与路由优化；function_score 业务加权的坑

- **零停机切换**：新建索引（新 mapping）→ 写别名切到新索引 → 存量 reindex / 双写追平 → 校验（`_count` 对比 + 抽样 diff + 对账）→ 读别名原子切换 → 删旧索引；全程业务无感。
- **multi-index 与路由**：按业务/租户分索引再用别名聚合查询；有固定路由键（租户 id）的查询**显式带 routing** 命中单分片，fanout 从 N 降到 1；跨索引查询注意 mapping 字段类型冲突（同名字段类型不一致会拖慢甚至报错）。
- **function_score 的坑**：`score_mode`（多个函数间怎么组合）与 `boost_mode`（与原分怎么组合）叠加语义易导致排序不可解释；`field_value_factor` 字段缺失直接报错（要 `missing`）；random_score 不固定种子会分页重复；script 慢——优先写入期预计算加权字段。
---

## 四、性能调优与运维（结合规模追问）

### 16. 写入调优清单

> 原题：批量大小、并发、refresh_interval、translog async 风险、副本置 0 场景、mapping 精简

- **bulk 按 MB 调不按条数**：单批 5~15MB 起步压测，观察吞吐与 `429 ESRejectedExecutionException` 拒绝率定最优值；条数 meaningless（文档大小差异大）。
- **并发**：从低并发逐步加压到集群 CPU/IO 60~80% 饱和点，而不是拍脑袋上大并发；观测点：`_cat/thread_pool/write` 的 queue/rejected。
- **refresh_interval**：批量导入 30s~1h，日志类 5~30s；全量导入期 `-1` + 结束后强制 refresh。
- **`translog.durability: async`**：约 5s 才 fsync，节点宕机/掉电丢窗口数据——**只敢用在可重放链路**（上游 Kafka 可回放），关键业务数据保持默认 `request`。
- **副本置 0**：仅限「数据可全量重建」的场景：离线导入、reindex、回灌；完成后立即恢复副本数并 refresh + flush。
- **mapping 精简**：显式 mapping + `dynamic: strict`（杜绝 dynamic mapping 失控炸字段）；不检索的 `index:false`、不聚合排序的 `doc_values:false`、不打分的 keyword `norms:false`；`_source` 按需过滤与压缩；id 自动生成（免版本检查开销）。
- 其他：预处理放 ingest pipeline 而非业务侧；`index.sort` 预排序利于压缩与提前终止；大字段（HTML 详情）不进主索引。

### 17. 查询调优清单

> 原题：只取需要的字段（_source 过滤 / docvalue_fields）；避免 script；profile API；慢查询日志定位

- 只取需要的字段：`_source` 白名单过滤；排序/展示用字段走 **docvalue_fields**（读列存，不解析 _source）；`track_total_hits: 10000` 放弃精确总数。
- 避免运行时计算：少用/禁用 script 查询与 script 排序，改为写入期预计算字段；避免前置通配符 wildcard（改 ngram 或 wildcard 字段类型）；负向逻辑尽量改写为正向 filter。
- **profile API**：请求带 `"profile": true`，看每分片 query/fetch/rewrite 耗时，区分「查询本身慢」还是「归并/取回慢」；`explain` 看单文档打分构成。
- **慢查询日志**：`index.search.slowlog.threshold.query.warn/info` 分级配置（如 1s/5s），定期聚合分析慢查询模式（哪个字段、什么查询形态），是日常治理抓手。

### 18. JVM 与资源

> 原题：堆为何不超 32GB（指针压缩）；堆外 Lucene page cache 规划；fielddata 堆溢出排查

- **堆 ≤ 31GB（别硬顶 32GB）**：Java 指针压缩（CompressedOops）在堆 <~32GB 时指针 4 字节，超过后退化为 8 字节——堆再大，能引用的对象反而可能变少；且大堆 GC 停顿不可控。
- **内存规划**：物理内存一半给 JVM 堆，另一半留给 **Lucene 的 mmap page cache（堆外、无 GC）**——倒排/列存的随机读性能靠它；例：64GB 节点 = 31GB 堆 + 33GB page cache；`bootstrap.mlockall` 锁内存 + 禁 swap 是前提。
- **fielddata 溢出排查**：text 字段排序/聚合默认禁用，开 `fielddata:true` 会把全量词表载入堆、必炸；排查 `_cat/fielddata`、`_nodes/stats/indices/fielddata` 与 fielddata breaker 触发记录；治理：改 keyword 走 doc_values（堆外列存），breaker（`indices.breaker.fielddata.limit`）兜底——生产绝不开 fielddata。

### 19. 大规模治理经验

> 原题：集群规模与事故复盘；磁盘水位三线；热点节点排查；RED/YELLOW 排查步骤

- 规模口述模板（替换成自己的真实数字）：如「40 节点（32C/128G/NVMe），日增 2~3TB、总量 60TB、千级索引/万级分片」，并讲扩容演进（从 6 台到 40 台）与容量规划依据——**没有规模数字会被判定为文档型经验**。
- 事故复盘模板：如 force merge 期间磁盘打满 → 触发 flood 水位、索引只读、业务写入报错 → 紧急删/缩日志索引、临时调高水位、恢复后清 `read_only_allow_delete`；复盘落地：ILM 留存策略补齐 + 磁盘告警提前量（70% 即告警）。
- **磁盘水位三线（默认 85/90/95）**：
  - **85% low**：不再向该节点分配新分片；
  - **90% high**：开始把分片迁出该节点；
  - **95% flood_stage**：该节点所有索引被标记 `index.blocks.read_only_allow_delete`，**写入被拒**。
  - 红线紧急处理：删/缩容日志类索引腾空间、临时调高水位阈值触发迁移，恢复后务必清掉 `read_only_allow_delete` 才能恢复写入。
- **热点节点排查**：`_cat/nodes?v` 看 CPU/IO/堆分布 → `_cat/shards?v` 看分片大小/分布；倾斜根因：自定义 routing 集中、热索引被 pin 在少数节点、分片大小不均、单分片热查询；处理：rebalance / 分配过滤规则 / routing 打散 / 大分片拆分。
- **RED/YELLOW 排查步骤**：`_cluster/health?level=indices` 锁定问题索引 → `_cat/shards` 找 UNASSIGNED → `_cluster/allocation/explain` 看未分配原因（常见：节点下线副本丢失、磁盘水位、分片数超上限、allocation filter）→ 按因处置（降副本 / 恢复节点 / 清磁盘 / 改过滤规则）。

### 20. 安全与运维

> 原题：快照备份（_snapshot + SLM）；CCR 场景；滚动升级步骤与注意

- **快照**：注册仓库（S3/HDFS/共享存储）→ **SLM** 策略定时快照（如每小时增量、每日全量、保留 N 天）→ **定期恢复演练**（恢复到指定索引/改名）；没演练过的备份等于没有备份。
- **CCR**：跨机房/异地容灾、异地就近读、机房迁移过渡；注意 license（白金级起）。
- **滚动升级**：禁分片分配（`allocation.enable: primaries`）→ flush → 逐台停机升级：先 data 后 master-eligible，**全程保证 master-eligible 存活过半（quorum）** → 每台启动后等集群健康再下一台 → 全部完成后恢复分配；前置：插件版本匹配、升级助手扫 deprecated API、大版本先在测试集群演练。
---

## 五、场景与架构题（区分架构理解）

### 21. 商品搜索系统设计

> 原题：商品 mapping 多字段设计；Suggest/纠错/同义词在线生效；筛选+排序+分页整体方案；MySQL→ES 同步（Canal/MQ + 防丢失对账）

- **mapping 设计**：
  - 商品名：`text`（`ik_max_word` 建索引 + `ik_smart` 搜索），multi-field 挂 `.pinyin`（拼音分词，供提示/拼音搜索）、`.raw`（keyword 精确匹配）；
  - 品牌/类目/属性：`keyword`（精确过滤 + terms 聚合出 facets）；类目路径用 keyword 数组；
  - 价格/销量/评分：数值类型（doc_values）；SKU 常用筛选项预聚合进主文档（最低价、价格区间、可选颜色/尺码 keyword 数组）——宽表冗余；需要 SKU 组合级筛选时局部 nested；
  - 业务字段：人工权重、上架状态、更新时间、类目/店铺 id。
- **提示与纠错**：Completion suggester（中文+拼音双字段）做搜索框提示；phrase suggester 做纠错（Did you mean）；**同义词在线生效问题**：索引级同义词必须重建索引才能生效——方案是「词库版本化 + 定期重建新索引 + 别名原子切换」，7.x 查询侧同义词可用 `_reload_search_analyzers` 热生效（只覆盖查询侧）。
- **查询整体方案**：`bool`：must=商品名 multi_match（best_fields，标题权重高）+ filter=类目/品牌/价格区间/状态（全走 filter 吃缓存）+ function_score=销量 log1p、时间衰减、人工加权；分页：前 N 页 from+size、更深处 search_after；facets 用 filter 聚合；大促榜单类需求预计算。
- **数据同步**：MySQL binlog → **Canal**（伪装 slave 拉取）→ MQ（按商品 id 哈希保序）→ 同步服务消费 upsert 写 ES（幂等、按 update_time/version 丢弃旧序）；**防丢失对账**：定时全量对账（分批 count/关键字段 checksum）+ 滚动增量对账（update_time 时间窗），差异以 DB 为准补偿重写 + 差异率/延迟告警看板。

### 22. 日志平台设计（类 ELK）

> 原题：Filebeat→Kafka→Logstash/Ingest 链路与削峰；ILM hot-warm-cold（可搜索快照）；日志突增保护

- **链路**：Filebeat（采集、断点续传、背压感知）→ **Kafka 削峰解耦**（按业务/级别分 topic，可重放，是整条链路的缓冲堤坝）→ Logstash 或 ES **Ingest Pipeline**（grok 解析、geoip、脱敏、采样丢弃）→ ES；下游任何一环慢/挂都不影响采集端。
- **ILM 分层**：hot（SSD，rollover 50GB 或 1 天，refresh 30s，副本 1）→ warm（HDD，forcemerge 到 1 segment、只读、降副本）→ **cold/frozen（可搜索快照：索引落对象存储、查询时按需拉起，存储成本降一个量级）** → delete（按日志等级 30/90/180 天留存）；命名 `app-logs-2026.10.22-000001` + 滚动别名。
- **突增自动保护**：Filebeat 限速/采样（DEBUG 日志丢弃）；Kafka 容量兜底 + 消费滞后告警；ES 侧 indexing pressure / 写入队列监控，按业务优先级限流或丢弃低价值日志；磁盘水位告警前置（70%），只读保护兜底——原则：**宁可丢日志，不拖垮平台**。

### 23. DB 与 ES 双写一致性

> 原题：双写/MQ 异步/binlog 订阅（Canal）选型与一致性窗口；延迟掩盖；不一致发现与修复

- **方案对比**：
  - **业务双写（不可靠，尽量别用）**：DB 与 ES 两次独立写非原子，任一侧失败/超时即不一致；无重试、无顺序保证、代码侵入；只适合容忍丢失的旁路数据。
  - **MQ 异步**：DB 事务内投本地消息表/事务消息 → 消费者写 ES；一致性窗口=消费延迟（秒级）；故障恢复靠 MQ 重试 + 死信告警；需要消费幂等与按主键分区保序。
  - **binlog 订阅（Canal，生产首选）**：DB 是唯一事实源，Canal 伪装 slave 拉取 binlog → 投 MQ → 同步服务写 ES；一致性窗口=binlog 端到端延迟（亚秒~秒级）；**故障恢复天然可靠**：位点回拨即可重放、不丢事件；注意 DDL 变更同步、大事务、位点管理。
- **掩盖延迟**：写后短窗口读路由到主库/缓存；前端对刚提交的修改本地回显；强实时查询降级查主库；ES 返回带 update_time 供前端判新鲜度。
- **发现与修复**：全量对账（低峰按主键分批比对 count/checksum/关键字段哈希）+ 增量滚动对账（update_time 窗口）；不一致自动补偿（以 DB 为准重写 ES）+ 差异率/同步延迟看板告警；补偿链路必须幂等（版本号防旧盖新）。

### 24. ES 不擅长的场景盘点

> 原题：强事务、频繁更新、深度 JOIN、毫秒级强一致读——「为什么不用 ES 解决 XX」怎么答

- 答题框架：先承认边界 → 讲机制层面的原因 → 给替代与分工，而不是贬低 ES。
- **强事务**：只有单文档乐观锁（seq_no + primary_term），无跨文档/跨索引 ACID → 交易/状态留在关系库，ES 只做只读检索投影。
- **频繁更新文档**：segment 不可变，更新=标删+重写，merge 放大 IO 与存储；高频变化字段（库存、点赞数）放 DB/Redis，ES 只存低频字段或旁路汇总。
- **深度 JOIN**：没有 SQL JOIN，nested/parent-child 能力有限且慢 → 应用层关联或同步层宽表化；强关联分析交给 OLAP 引擎。
- **毫秒级强一致读**：NRT（refresh 前不可见）+ 主副异步收敛，本质是最终一致 → 计费、库存扣减的判定回 DB，ES 结果仅用于展示。
- 万能收尾句式：「系统里总要有一个事实源（DB）；ES 的定位是**检索与聚合的投影**——需要 ACID、强一致、频繁原地更新的部分都留在事实源，ES 只承载搜索与聚合。」

### 25. 替代品对比与替换信号

> 原题：OpenSearch、ClickHouse、Tantivy/自研内存检索；什么信号出现时考虑替换 ES

- **OpenSearch**：AWS 基于 ES 7.10 的 Apache 2.0 分叉（license 之争产物）；已在 AWS 生态深度绑定或必须纯开源许可时迁移，API 兼容、成本低；代价是落后上游新特性、生态差异。
- **ClickHouse**：列存 OLAP，日志场景压缩比/存储成本远优于 ES（可达 10 倍级），倒排+bloom+全文函数可支撑日志检索；**信号**：日志量巨大、以统计聚合为主检索为辅、存储成本失控、团队具备 CK 运维能力；弱点：点查、复杂相关性排序、高并发检索不如 ES。
- **Tantivy/自研内存检索**：Rust 版 Lucene 等价物，可嵌入应用进程；**信号**：数据量 GB 级、不想运维集群、对延迟极敏感的内嵌轻量检索（站内小搜索、关键词/向量混合召回）。
- **替换触发信号清单**：①成本信号——存储/内存开销与业务价值持续倒挂；②性能天花板——写入吞吐或查询延迟怎么调优都够不到 SLO；③功能缺口——强 JOIN/实时分析成为主需求；④运维复杂度——集群规模导致的稳定性事故频发、治理成本超过收益；⑤许可/绑定——license 变更或云厂商锁定。替换是渐进的：日志类先迁 ClickHouse、核心检索留 ES，按场景拆分而不是一刀切。

---

## 附：答题提示（对应原题评估表）

- 核心原理题务必主动以「segment 不可变」为根因展开，它串起 refresh/flush/merge、深分页、更新成本、近实时五个考点。
- 集群与运维题必须带规模数字（节点/分片/数据量）与至少一个完整事故复盘，否则会被判定为文档型经验。
- 场景设计题先讲事实源与一致性策略（谁是对的、错了怎么发现怎么修），再讲功能设计——这是架构师视角的分水岭。