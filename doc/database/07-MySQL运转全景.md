# MySQL 运转全景 面试题 —— 从磁盘文件到一条 UPDATE 的完整生命

> 适用岗位：中高级后端 / DBA。考察建议：本套题以「一条 UPDATE 的生命周期」为主线，串起磁盘结构、内存结构、日志体系、两阶段提交与崩溃恢复；追问链重点考察「谁管什么」的边界感与「为什么」的机制理解。

---

## 一、整体架构与分层（必考）

1. 用一条主线讲清楚 MySQL 的运转：客户端接入后，一条 UPDATE 从进入到最终落盘的完整路径是什么？（连接器 → 分析器 → 优化器 → 执行器 → InnoDB 接口 → buffer pool 改页 → 写 undo 与 redo → 两阶段提交 → 后台刷脏）
2. 与 Oracle 相比，MySQL 有哪三个最关键的「错位」？（多一套 binlog、引擎可插拔、没有控制文件）各自带来什么架构后果？
3. 分层定位是排障第一步：为什么「慢」和「数据不一致」要先分清 Server 层还是引擎层？分别举一个例子（优化器选错索引 vs buffer pool 太小；崩溃恢复归 redo 管 vs 主从延迟归 binlog 管）。
4. Server 层四大组件（连接器/分析器/优化器/执行器）各负责什么？执行器调用引擎接口取行的粒度是什么（逐行下推）？
5. 术语对齐：MySQL 的 database 对应 Oracle 的什么？「实例」怎么理解？为什么说 MySQL 的「库」更接近 schema？
6. MySQL 没有控制文件，那物理结构的元数据（表、列、索引、表空间映射）存在哪？8.0 的数据字典与原子 DDL 是什么关系？

## 二、磁盘物理结构（必考）

7. 独立表空间（file-per-table）vs 系统表空间（ibdata1）vs 通用表空间：默认是哪种？各自优缺点？TRUNCATE 在哪一种能真正归还空间？
8. 经典惨案：5.x 时代「ibdata1 几百 GB 无法收缩」是怎么造成的？8.0 做了哪些整改（DD 移出、undo 独立、change buffer 的变迁）？
9. 三套日志的定位：redo / undo / binlog 分别在哪、写谁、职责是什么？填一张「谁管什么」对照表（崩溃恢复/主备同步/读一致性/PITR）。
10. 8.0.30 前后 redo 文件形态的变化（ib_logfile0/1 → #innodb_redo 目录）与容量控制参数（innodb_redo_log_capacity）？
11. relay log 与 relay_log.info 的作用；ib_buffer_pool 是干什么的（重启预热）？
12. 双 1 配置下宕机为什么「已应答事务」不丢？redo 与 binlog 各自怎么保证？（详见第十四节）

## 三、逻辑结构：表空间 → 段 → 区 → 页（必考）

13. InnoDB 逻辑存储层次：一棵聚簇索引 B+ 树对应几个段（叶子段/非叶子段）？区为什么固定 1MB（16K × 64 页）？碎片区（frag 页）解决什么问题？
14. 页内结构解剖：一个 16KB 页从上到下有哪些部分（File Header/Page Header/User Records/Page Directory/File Trailer）？Page Directory 的作用（二分定位槽）？
15. 行的隐藏列 DB_TRX_ID 与 DB_ROLL_PTR 分别是什么？它们与 MVCC 的关系？
16. 页分裂与合并：什么条件触发？为什么随机主键（UUID）会导致「分裂遍地开花」？MERGE_THRESHOLD（默认 50%）是什么？
17. 页校验：innodb_checksum_algorithm（8.0.16+ 默认 full_crc32）解决什么问题？坏页怎么处理（doublewrite 兜底，见第十二节）？
18. 为什么 InnoDB 的二级索引叶子节点存的是主键值而不是行地址？「回表」和「覆盖索引」怎么来的？
19. 主键选择如何影响性能：自增主键 vs 随机 UUID 在页分裂、碎片化、buffer pool 命中率上的差异？

## 四、内存结构：buffer pool 与伙伴（必考）

20. Buffer Pool 的作用与改进版 LRU：young/old 两个区域怎么划分（默认 5/8 与 3/8）？innodb_old_blocks_time（1000ms）解决什么问题？
21. 追问：为什么全表扫描不会冲掉热点页？这个「防污染」设计与 Oracle 的哪个池异曲同工？
22. buffer pool 大小与实例：innodb_buffer_pool_size 给多少（50%～70%）？innodb_buffer_pool_instances 为什么能降低高并发 LRU 锁争用？8.0 支持在线调整吗？
23. 写入路径（WAL 思想）：为什么「改页在内存、提交只保证日志落盘、刷脏交给后台」？提交时数据页已经写盘了吗（常见误区 3）？
24. change buffer：解决什么问题（二级索引页不在池中的随机写）？什么条件收益大（写多读少、大量二级索引）？什么情况不适用（唯一索引）？
25. 自适应哈希索引（AHI）：什么条件下构建？等值查询 O(1) 的代价？什么场景要关掉（争用多）？
26. per-thread 内存（sort buffer/join buffer/read buffer）：为什么「调大不一定更快、反而可能 OOM」？正确顺序是先看什么（执行计划）？

## 五、后台线程与节律

27. page cleaner / log writer / purge coordinator / master thread / binlog dump 线程各负责什么？分别「挂了/慢了会怎样」？
28. 与 Oracle 对照：page cleaner ≈ DBWn，log writer ≈ LGWR，purge ≈ SMON 的 undo 清理部分；为什么 InnoDB 没有 CKPT 线程（检查点是「记录当前已刷到的 LSN」这一被动动作）？
29. purge 线程清理什么（已提交事务的 undo 版本链、history list）？它被什么压住会导致 undo 膨胀、二级索引旧记录堆积？

## 六、一条 UPDATE 的完整旅程（必考，全题核心）

30. 以 `UPDATE t SET c=c+1 WHERE id=100;` 为例，完整讲出五步旅程（Server 层四组件 → InnoDB 三处内存修改 → 两阶段提交 → 后台异步刷盘 → 复制扩散）。
31. InnoDB 层「内存里改三处」是哪三处？顺序是什么（先记 undo、再记 redo、最后改数据页）？mtr（mini-transaction）保证什么？
32. 两阶段提交（2PC）的三个追问：
    - Q1：为什么提交不直接写数据页（WAL：顺序写日志远快于随机写页）？
    - Q2：崩溃那一刻卡在 2PC 中间怎么办（以 binlog 为准对账：redo prepare 且 binlog 完整 → 提交；否则回滚）？
    - Q3：组提交是什么（leader 带队一次 fsync 摊薄 IOPS）？观测指标有哪些？
33. 为什么必须 2PC？若 redo 与 binlog 各写各的，任一侧宕机会发生什么（主库恢复与从库/备份不一致，主备数据源分裂）？
34. 提交路径上 binlog 由谁写（Server 层执行器，2PC 协调者）？InnoDB 只写 redo，两者的协调关系是什么？

## 七、redo：持久性防线（必考）

35. redo 记录的是什么（页级物理变更，页号+类型+具体修改，不是 SQL）？什么会产生 redo（数据页/索引页/undo 页/change buffer 页/DD 页）？MyISAM 有 redo 吗？
36. LSN 是什么？checkpoint 推进的机制（老 redo 被覆盖的前提是对应脏页已全部刷盘）？async 水位（75%）与 sync 水位（90%）分别发生什么？
37. 场景题：写入吞吐呈锯齿状、偶尔整库停写数秒，观测 checkpoint age 长期 >90%——病因是什么？处置怎么做（调大 innodb_redo_log_capacity，8.0.30+ 在线生效）？
38. innodb_flush_log_at_trx_commit 三个取值（1/2/0）的行为与风险矩阵？生产默认是哪个？「双 1」指的是什么组合？
39. redo 损坏会怎样（实例无法完成崩溃恢复）？处置路径是什么（确认备份；无备份时 innodb_force_recovery 从 1 逐级试到 5，6 会丢数据）？为什么说 force_recovery 是「逃生舱」不是工具？
40. redo 容量规划经验：写密集库从多大起步（4G～8G）？观测什么指标校准（checkpoint age 不长期超 75%）？redo 所在盘的硬件要求（低延迟 SSD，直接决定提交 RT）？

## 八、undo 与 MVCC（必考）

41. undo 记录什么：INSERT / UPDATE / DELETE 分别记什么（INSERT 记主键，UPDATE/DELETE 记旧值全行）？undo 页的修改本身会写 redo 吗（会，redo 保护 undo）？
42. MVCC 版本链：DB_ROLL_PTR 如何把多版本串成链？ReadView 的成员（m_ids、up_trx_id、low_trx_id）与可见性判断规则（trx_id < up_trx_id 可见；≥ low_trx_id 不可见；中间查 m_ids）？
43. RC 与 RR 下 ReadView 生成时机的差异（每条语句新建 vs 事务内复用第一条快照读的）？由此推导两种隔离级别下不可重复读是否被解决？
44. 长事务的代价（MySQL 版 ORA-01555，但表现相反）：为什么 InnoDB 不覆盖 undo 反而「永不清理」？挂了 3 天的事务会带来什么（undo 膨胀、查询变慢、锁阻塞、复制连带）？
45. 长事务排查与处置：查哪张表（information_schema.INNODB_TRX 的 trx_started）、看什么指标（SHOW ENGINE INNODB STATUS 的 History list length）、根治手段是什么（杀长事务/拆大事务，innodb_undo_log_truncate 清不掉被引用的部分）？
46. 案例：某交互式客户端 SELECT ... FOR UPDATE 后挂起 3 天未提交，undo 表空间 500GB、history list length 2 亿+——完整处置流程与启示（监控 trx_started 应作为标配告警）？

## 九、binlog：Server 层逻辑日志（必考）

47. binlog 三种格式（STATEMENT / ROW / MIXED）各记什么、优劣、为什么 8.0 默认与推荐 ROW？ROW 格式支持什么（WRITESET 并行复制）？代价是什么（日志量大）？
48. redo 与 binlog 的分工表：层级、性质、写法、用途、提交角色、Oracle 对应物——逐项对比。
49. 复制链路：主库 dump 线程 → 从库 IO 线程（relay log）→ SQL 线程重放；GTID 解决什么问题（切换不再人工对位点）？延迟观测指标（SHOW REPLICA STATUS 的 Seconds_Behind_Source）？
50. 主从延迟常见根因（单线程重放瓶颈、大事务、从库单点写、表无主键导致 ROW 重放全表扫）？为什么「加从库」不解决延迟（只分散读压力）？
51. 案例：2 亿行无主键日志表，ROW 复制下一条 UPDATE 从库单条重放 40 秒，延迟数小时——处置与预防（sql_require_primary_key=ON 强制主键）？
52. binlog 保留策略：binlog_expire_logs_seconds 与删除原则（必须保证恢复链完整：全备之后所有 binlog 都在）？空间紧张时正确的顺序（先备份再缩短保留期）？

## 十、崩溃恢复与 doublewrite（必考）

53. 页断裂（partially written page）是怎么产生的（16KB 写盘非原子、半新半旧）？为什么 redo 重放也无从下手？
54. doublewrite 的完整机制：刷脏时先写哪、再写哪？恢复时发现页校验失败怎么处理（从 doublewrite 拿完整副本覆盖 → 再应用 redo）？为什么说它是「便宜的保险」（生产保持开启）？
55. 崩溃恢复四步流程：① 定位 checkpoint LSN 重放 redo（前滚）② 达到崩溃瞬间状态 ③ 两阶段提交对账（redo prepare 状态的事务：binlog 完整 → 提交；否则回滚；binlog 截断 → 回滚告警）④ undo 回滚未提交事务（8.0 可延迟、先开放连接）——与 Oracle 恢复的差异在哪（多出 2PC 对账一步，因为有两套日志体系）？
56. 为什么「主库恢复结果 = 从库重放结果」是 2PC 的全部意义？从库视角怎么理解（binlog 是裁判）？

## 十一、可靠性规划：双 1、容量与备份

57. 双 1 与刷盘组合矩阵：1/1、1/0、2/1、0/0 各自的语义、风险、适用场景？
58. 案例：某库设 flush_log_at_trx_commit=2, sync_binlog=0，机房断电后主库丢约 1 秒事务、从库反而比主库新、业务状态错乱——根因与启示（双 1 是主备一致 + 持久性的保费；要性能去优化硬件与组提交，而不是关保险）？
59. 空间容量规划清单：buffer pool（50%～70% + dump/load 预热）、redo（4G～8G 起步看 checkpoint age）、undo（双表空间 + 自动截断；5.7 老库检查 undo 是否在 ibdata1）、binlog（保留窗口 ≥ 最长恢复窗口）、ibdata1（5.7 一开始就 1G:autoextend + 独立 undo）？
60. 备份体系对比：物理备份（XtraBackup / Clone Plugin，热备）vs 逻辑备份（mysqldump / mydumper）的适用场景？PITR 怎么做（全备 + mysqlbinlog 重放到指定时刻）？为什么「没演练过的备份等于没有备份」？

## 十二、观测与诊断（场景题）

61. 高频「病象 → 病因」速查：写入周期性抖动/跌零、提交 RT 随负载上涨、undo 持续增长、主从延迟、偶发锁等待超时、查询忽快忽慢——分别对应什么病因？
62. 观测指标地图：整体健康（Threads_running、Innodb_row_lock_waits）、buffer pool（命中率、free buffers、脏页比例）、redo（checkpoint age、LSN 增速）、长事务（INNODB_TRX、History list length）、锁等待（performance_schema.data_lock_waits）、执行层（EXPLAIN ANALYZE、sys.statements_with_full_table_scans）、复制（SHOW REPLICA STATUS）、表空间（DATA_FREE 碎片率）——各用什么入口？
63. 场景题：线上库「提交 RT 随负载线性上涨」的排查树（redo 盘慢、组提交失效单并发、binlog 盘慢、sync_binlog=1 且无 NVMe——逐项怎么验证）？
64. 场景题：5.7 老库 ibdata1 膨胀到 800GB、磁盘将满，且 ibdata 无法收缩——完整处置（逻辑迁移重建实例、新库启用独立 undo、迁移窗口内停写）？
65. 场景题：5.7 加字段中途实例被 OOM kill，重启后字典与 .frm 不一致、表无法访问——根因（非原子 DDL）与根治（升级 8.0 原子 DDL：DD 进 InnoDB + redo 保护）？

## 十三、常见误区与延伸

66. 十个常见误区速查：逐条判断对错并给理由——①「MySQL = InnoDB」②「binlog 就是 redo」③「COMMIT 会把数据页写盘」④「database = Oracle 的 database」⑤「RR 不会产生间隙锁问题」⑥「undo 越多越好」⑦「sort/join buffer 调大就快」⑧「查询缓存能提速」⑨「doublewrite 可以关」⑩「从库延迟靠加从库解决」？
67. 8.0 vs 5.7 关键变化清单：数据字典进 InnoDB（原子 DDL）、查询缓存移除、窗口函数/CTE/降序索引/隐藏索引、undo 独立 + 自动截断、redo 在线可调、WRITESET 并行复制、instant ADD COLUMN；8.4 的变化（change buffer 移除、默认认证收紧）——选两个你最有体会的展开。
68. 引擎生态：InnoDB 之外 MyISAM / Memory / Archive / CSV / MyRocks 各剩什么场景？为什么 MyISAM 只能做只读归档类边缘场景（无事务、无崩溃恢复、表锁）？
69. 复制进阶：半同步复制（semi-sync）解决什么问题（折中双 1 主库 + 异步复制的丢数据窗口）？MGR / InnoDB Cluster（基于 Paxos、无共享存储、自动故障转移）的定位？延迟从库（误操作保险）与行级闪回怎么实现？
70. 收尾彩蛋：Oracle ↔ MySQL 概念映射速查（Instance→mysqld、Buffer Cache→Buffer Pool 防污染、DBWn→Page Cleaner、LGWR→Log Writer、控制文件→数据字典、RMAN→XtraBackup/Clone、AWR→performance_schema/sys）——抽查 3 个能对答。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 分层架构与主线 | 15% | 一条 UPDATE 全链路能闭环、层边界清晰 |
| 磁盘/逻辑/内存结构 | 20% | 页结构、LRU 防污染、change buffer 机制层理解 |
| 两阶段提交 | 20% | 三个追问全接住、崩溃对账规则正确 |
| redo/undo/binlog 三件套 | 20% | 分工表清晰、长事务代价、redo 容量场景题 |
| 崩溃恢复与 doublewrite | 10% | 四步流程、页断裂机制 |
| 观测诊断与实战 | 15% | 病象→病因速查、案例处置完整 |

**判定要点**：能讲清「提交只刷日志不刷页、双 1 保持久性」算中级；能完整推导 2PC 崩溃对账规则、现场讲 MVCC 版本链判断、独立处置 redo/undo/ibdata1 案例算高级；能主导可靠性规划（容量/备份演练/监控基线）并给出量化收益算资深。
