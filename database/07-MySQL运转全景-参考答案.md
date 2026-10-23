# MySQL 运转全景 参考答案

> 以下为「要点框架 + 关键细节 + 权衡取舍」，覆盖从磁盘文件到一条 UPDATE 完整生命周期的机制层理解。面试时建议结合个人运维/调优案例展开。原题见 ../07-MySQL运转全景.md。

---

## 一、整体架构与分层

### 1. 一条 UPDATE 的完整路径（主线）
- **Server 层**：连接器（TCP/握手/认证/权限校验）→ 分析器（词法/语法解析成解析树）→ 优化器（按成本估算选索引、join 顺序）→ 执行器（调引擎接口取行，索引条件下推）。
- **InnoDB 层**：① 页不在 buffer pool 则先从 .ibd 读入；② 加行锁（记录锁/间隙锁）；③ 内存里改三处——写 undo（旧版本，DB_ROLL_PTR 指向它形成版本链）、写 redo 到 log buffer（变更向量）、修改数据页成脏页进 flush list。
- **提交**：两阶段提交——redo prepare 刷盘 → Server 层写 binlog 刷盘 → redo commit 标记。
- **后台**：page cleaner 异步刷脏页（先 doublewrite 后 .ibd），purge 异步清理旧版本。
- **复制**：binlog dump 线程推给从库 → IO 线程写 relay log → SQL 线程重放。

### 2. 与 Oracle 的三个「错位」
- **多一套日志**：Oracle 只有 redo（+undo）；MySQL InnoDB 之外 Server 层还有 binlog，不参与页级前滚，是复制与 PITR 的根基；两套日志靠 2PC 对齐。
- **引擎可插拔**：SQL 解析优化在 Server 层，存储/事务/锁/日志由引擎决定；MyISAM 等不走 redo/undo。
- **没有控制文件**：物理结构元数据在 8.0 放进数据字典（InnoDB 表），加上 redo 与 binlog 三者各管一段；也正因此 8.0 的 DDL 是原子的（DD 变更写 redo 统一提交）。

### 3. 分层定位排障
- 「慢」要分 Server 层（优化器选错索引、函数导致索引失效）还是引擎层（buffer pool 太小、刷脏跟不上、锁等待）。
- 「不一致」要分崩溃恢复（redo 管）和主从延迟（binlog 管）。
- 原则：先定位层，再动参数，避免乱调。

### 4. Server 层四组件
- **连接器**：连接管理、认证、权限校验（连接时 + 执行时）。
- **分析器**：词法/语法解析，生成解析树；8.0 起分析/优化整合进统一框架。
- **优化器**：按统计信息估算成本，选索引、确定 join 顺序、是否走覆盖索引。
- **执行器**：逐行调引擎接口（读一行/写一行），处理权限与条件下推。
- 查询缓存 8.0 已移除（命中率低，高并发下反成锁瓶颈）。

### 5. 术语对齐
- MySQL 的 database 即 schema（逻辑命名空间）；Oracle 的 schema ≈ user，database 指整套文件。
- MySQL 实例 = 一个 mysqld 进程（内存 + 线程），Oracle 实例 = SGA + 进程组；MySQL 单进程多线程。
- 「进程 vs datadir」的二分法替代 Oracle 的「实例 vs 数据库」。

### 6. 没有控制文件怎么办
- 8.0 数据字典（DD）是 mysql 库下的 InnoDB 字典表（mysql.tables、mysql.indexes 等），随 redo/binlog 受保护。
- 「总账」拆成三份：数据字典 + redo + binlog。
- 原子 DDL：DD 变更写进 redo 并统一提交，5.7 里 DDL 崩溃会留下中间状态（.frm 与字典不一致）。

## 二、磁盘物理结构

### 7. 表空间类型
- 独立表空间（file-per-table，5.6.6 起默认）：每表一个 schema/tab.ibd；可表级迁移（可传输表空间）、按表管理空间、TRUNCATE 真正归还空间。
- 系统表空间 ibdata1：5.7 及以前装着数据字典、undo、change buffer；8.0 起字典移入 mysql 库 InnoDB 表、undo 独立，ibdata 只剩 change buffer（8.4 起也移除）；**ibdata1 无法收缩**。
- 通用表空间：手工创建、多表共享，用于精细控制。

### 8. ibdata1 惨案
- 5.x 把 undo 留在 ibdata1，长事务使 undo 暴涨且 ibdata 无法收缩，造成几百 GB 无法回收。
- 8.0 整改：字典移出、undo 移入独立表空间（自动截断）、change buffer 8.4 移除；5.7 生产应在初始化时就 `innodb_data_file_path=ibdata1:1G:autoextend` + 独立 undo。

### 9. 三套日志定位（谁管什么）
| 问题 | 靠什么 |
|---|---|
| 崩溃后已提交数据不丢 | InnoDB redo（+双 1 配置） |
| 主备同步 | binlog 复制（或 GTID/组复制） |
| 读一致性/回滚 | InnoDB undo（MVCC 版本链） |
| 物理结构总账 | 数据字典（8.0 InnoDB 表） |
| 时间点恢复 PITR | 备份 + binlog（mysqlbinlog 重放） |

### 10. redo 文件形态
- 8.0.30+：`#innodb_redo/` 下 32 个 recycled 文件，总容量 = `innodb_redo_log_capacity`（默认 100MB，在线可调）。
- 旧版本：2 个 ib_logfile，改大小需重启。
- 循环写：老 redo 被覆盖的前提是对应脏页已全部刷盘。

### 11. relay log 与预热
- relay log：从库上主库 binlog 的中转站；relay_log.info 记录应用位点；复制中断可重拉。
- ib_buffer_pool：buffer pool 热页清单，重启时快速预热（innodb_buffer_pool_dump_at_shutdown/load_on_startup，默认开）。

### 12. 双 1 下为何不丢
- `innodb_flush_log_at_trx_commit=1`：每次提交 fsync redo；`sync_binlog=1`：每次提交 fsync binlog；两者都落盘后事务才算真正提交，所以已应答事务不丢。

## 三、逻辑结构：表空间 → 段 → 区 → 页

### 13. 逻辑层次
- 表空间 → 段（聚簇索引一棵树 = 叶子段 + 非叶子段，二级索引同理）→ 区（固定 1MB = 16K 页 × 64 页）→ 页（16KB）→ 行。
- 碎片区（frag 页）可跨段共享，小表不必占满整区。

### 14. 16KB 页内部结构
- File Header（38B）：页号、前后页指针（双向链表）、页类型、Space ID、LSN、校验和。
- Page Header（56B）：记录数、槽数、free 链表头、Last Insert、页级 LSN。
- Infimum/Supremum 虚拟行；User Records 单向链表按主键序；Free Space；Page Directory（二分定位槽的「书脊」）；File Trailer（8B，校验和 + 页 LSN，doublewrite 恢复时验证）。
- 行内：变长字段长度 + NULL 位图 + 记录头（next_record）+ 隐藏列 DB_TRX_ID / DB_ROLL_PTR + 数据列。

### 15. 隐藏列与 MVCC
- DB_TRX_ID：最后修改该行的事务 ID；DB_ROLL_PTR：指向 undo 版本链的入口。二者是 MVCC 一致性读的入口。

### 16. 页分裂与合并
- 插入使页满 → 中点分裂；随机主键让分裂遍地开花（碎片化）。
- 删除使页使用率低于 MERGE_THRESHOLD（默认 50%）→ 与邻居合并。
- 碎片严重用 OPTIMIZE TABLE（在线重建）或 ALTER TABLE ... ENGINE=InnoDB 回收。

### 17. 页校验
- innodb_checksum_algorithm（8.0.16+ 默认 full_crc32），每次读页验证完整性；坏页导致实例启动受阻，配合 doublewrite 处理。

### 18. 二级索引存主键值
- 二级索引叶子存「索引列 + 主键值」，命中后拿主键回聚簇索引再查一次 = 回表；覆盖索引（索引列覆盖查询列）避免回表所以快。

### 19. 主键选择
- 自增主键顺序追加，页分裂少、空间利用率高；随机 UUID 频繁页分裂、碎片化、buffer pool 命中率下降、索引更大。

## 四、内存结构

### 20. Buffer Pool 与 LRU
- 缓存数据页/索引页，读先查池、写先改池（脏页）。8.0 在线动态调整，专用服务器给物理内存 50%～70%。
- 改进版 LRU：新页先入 old 子链表（距头 5/8 处，innodb_old_blocks_pct=37），在 old 停留超 innodb_old_blocks_time（默认 1000ms）再被访问才晋升 young。

### 21. 防全扫污染
- 全表扫描的页流在 old 区「走个过场」（1 秒内只访问一次）就被淘汰，冲不进热点区——与 Oracle RECYCLE 池异曲同工。

### 22. size 与 instance
- 50%～70% 物理内存经验值；innodb_buffer_pool_instances 把池分成多个实例降低高并发 LRU 锁争用（内部按 chunk 分配）；8.0 在线调整。

### 23. WAL 写入路径
- 提交的持久性由 redo + binlog 双双落盘兑现；数据页由 page cleaner 异步刷——顺序写日志远快于随机写页。「COMMIT 会把数据页写盘」是误区。

### 24. change buffer
- 二级索引页不在池中时，把插入/修改先缓存，待页被读入时合并（merge）；写多读少、大量二级索引的表收益大；唯一索引不适用（需立即检查唯一性）。

### 25. AHI
- 对热点页自动建内存哈希，等值查询 O(1)；占内存、争用多时可关闭（innodb_adaptive_hash_index=OFF）。

### 26. per-thread 内存
- sort buffer / join buffer / read buffer 按需分配、可能并发 ×N；盲目调大易 OOM；先看执行计划再动参数。

## 五、后台线程

### 27. 线程职责表
| 线程 | 职责 | 挂了/慢了会怎样 |
|---|---|---|
| page cleaner | 从 LRU 尾部与 flush list 批量刷脏（先 doublewrite 后 .ibd） | 刷脏滞后 → checkpoint age 顶满 → 写入抖动甚至停写 |
| log writer/flusher | log buffer 写 redo 文件并 fsync | 提交全部卡死 |
| purge coordinator/worker | 清理已提交事务的 undo 版本链（history list），回收 undo 页 | 长事务压着 → undo 膨胀、二级索引旧记录堆积、查询变慢 |
| master thread | 每秒心跳：刷 redo、合并 change buffer、自检 | 整体节律紊乱 |
| binlog dump | 主库给每个从库推 binlog 事件 | 该从库复制断流 |

### 28. 与 Oracle 对照
- page cleaner ≈ DBWn，log writer ≈ LGWR，purge ≈ SMON 的 undo 清理部分，binlog dump ≈ ARCn+LNS。
- 无 CKPT 线程：InnoDB 检查点是「记录当前已刷到的 LSN」这一被动动作，随刷脏自然推进。

### 29. purge 清理
- 清理「早于最老活跃事务」的 undo 版本；被长事务压住则版本链保留 → undo 膨胀、查询沿长链回溯变慢。

## 六、一条 UPDATE 的完整旅程

### 30. 五步旅程
① Server 层四组件（权限→解析→优化→执行，执行器逐行调接口）→ ② InnoDB 内存改三处（undo/数据页/redo，加行锁）→ ③ 提交两阶段（prepare 刷 redo → 写 binlog → commit 标记）→ ④ 后台 page cleaner 刷脏（doublewrite → .ibd）、purge → ⑤ 复制扩散（dump → relay log → 重放）。

### 31. 内存改三处 + mtr
- 三处：undo 写旧版本（DB_ROLL_PTR 指向）、buffer pool 数据页应用修改成脏页（进 flush list）、redo 写 log buffer。
- 顺序：先记 undo、先记 redo，最后改数据页——与 Oracle 相同的「先日志后数据」。
- mtr（mini-transaction）保证一个变更组的原子性，mtr 结束即写 log buffer。

### 32. 2PC 三追问
- **Q1**：不直接写数据页因为 WAL——顺序写日志远快于随机写页；提交的持久性承诺由 redo+binlog 落盘兑现。
- **Q2**：崩溃恢复时以 binlog 为准对账——redo 处于 prepare 且对应 XID 的 binlog 已完整写入 → 提交；binlog 没写 → 回滚；binlog 截断 → 回滚告警。保证主库恢复 = 从库重放。
- **Q3**：组提交 = 并发事务的两次 fsync 由 leader 带队一次刷盘，IOPS 摊薄；观测 Binlog_group_commit_trigger_count；这是双 1 在写密集场景可用的原因。

### 33. 为什么必须 2PC
- 若 redo 与 binlog 各写各的，任一侧宕机都会出现主库恢复出来的数据与从库/备份不一致，主备数据源分裂。

### 34. 谁写谁
- Server 层执行器是 2PC 协调者，写 binlog；InnoDB 写 redo；两阶段把两个日志拴在一条绳上。

## 七、redo：持久性防线

### 35. redo 记录什么
- 页级物理变更（物理逻辑日志：页号 + 类型 + 具体修改），不是 SQL；每条带 LSN。
- 产生者：数据页、索引页、undo 页、change buffer 页、DD 页（8.0）。MyISAM 不产生 redo。

### 36. LSN 与 checkpoint
- LSN 是 redo 全局单调坐标，出现在 log buffer、redo 文件、每页 File Header、flush list。
- checkpoint 推进 = 更老脏页刷掉 → 允许覆盖更老 redo。
- async 水位 ~75%：加大刷脏力度；sync 水位 ~90%：强制激进刷脏、用户请求被拖住——redo 太小导致写入周期性卡顿的机制（对应 Oracle checkpoint not complete）。

### 37. 场景题：写入锯齿状
- 病因：redo 容量（默认 100MB）太小，checkpoint age 长期 >90%，批量导入触发 sync 强制刷脏停写。
- 处置：`SET GLOBAL innodb_redo_log_capacity=8G`（8.0.30+ 在线生效）后锯齿消失；8.0.30 前需规划重启窗口调整。

### 38. flush_log_at_trx_commit 三值
- **1**（默认）：每次提交 fsync redo，最安全，IOPS 最高（组提交摊薄）。
- **2**：只写 OS page cache 每秒 fsync；进程崩溃不丢，主机断电丢约 1 秒。
- **0**：每秒才写+刷；mysqld 崩溃也可能丢 1 秒，生产不建议。
- 「双 1」= flush_log_at_trx_commit=1 + sync_binlog=1。

### 39. redo 损坏处置
- 现象：实例无法完成崩溃恢复、起不来（Unable to read...log）。
- 处置：先确认备份；无备份时受控用 innodb_force_recovery 从 1 逐级试到 5（6 会丢未刷脏数据 = 承认损坏），导出数据重建实例。force_recovery 是「逃生舱」不是工具。

### 40. redo 容量规划
- 写密集库 4G～8G 起步；观测 Innodb_redo_log_capacity_resident 或 SHOW ENGINE INNODB STATUS 的 LOG 段，目标 checkpoint age 不长期超 75%。
- redo 盘必须低延迟 SSD——flush_log_at_trx_commit=1 时直接决定提交 RT。

## 八、undo 与 MVCC

### 41. undo 记录内容
- INSERT 记主键（回滚即删）；UPDATE/DELETE 记旧值全行。每条 undo 属某事务的事务槽。
- undo 页的修改同样产生 redo——redo 保护 undo。

### 42. MVCC 版本链 + ReadView
- 版本链：行 DB_ROLL_PTR → undo 版本1（trx_id + roll_ptr）→ 版本2……回溯。
- ReadView 成员：m_ids（活跃事务列表）、up_trx_id（最早活跃 ID）、low_trx_id（下一个待分配 ID）。
- 判断：trx_id < up_trx_id → 可见；≥ low_trx_id → 不可见；在中间 → 查 m_ids，活跃则不可见。沿链回溯到可见版本。

### 43. RC vs RR
- RC：每条语句新建 ReadView；RR：事务内复用第一条快照读的 ReadView。RR 因此解决不可重复读。

### 44. 长事务代价（MySQL 版 ORA-01555）
- Oracle 长查询怕 undo 被覆盖（snapshot too old）；InnoDB 不覆盖 undo，长事务代价变成：undo 永不清理（purge 只清早于最老 ReadView 的）→ undo 膨胀、history list length 飙升；其他会话沿长链回溯查询变慢；持锁阻塞他人；binlog 影响从库并行重放。

### 45. 长事务排查处置
- 查 information_schema.INNODB_TRX（trx_started 最老）；SHOW ENGINE INNODB STATUS 看 History list length。
- innodb_undo_log_truncate=ON（默认）回收闲置回滚段空间，但清不掉被长事务引用的部分；根治 = 杀长事务/拆大事务。
- 建议：脚本告警 trx_started 超过阈值（如 10 分钟）。

### 46. 案例：挂 3 天的事务
- 现象：undo 500GB、查询越来越慢、history list length 2 亿+。
- 处置：kill 会话 → purge 追平 → undo 截断回收。
- 启示：InnoDB 不覆盖 undo，长事务账单是「全局的」；监控 trx_started 应作标配告警。

## 九、binlog：Server 层逻辑日志

### 47. 三种格式
- **STATEMENT**：原始 SQL，日志小；但 NOW()/UUID()/LIMIT 无序等在主从重放可能不一致。
- **ROW**（默认与推荐）：每行前后镜像，绝对一致、支持 WRITESET 并行复制；日志量大（UPDATE 全表 = 全表行记录）。
- **MIXED**：默认 STATEMENT，不安全语句自动切 ROW；折中但排查心智负担大。

### 48. redo vs binlog 分工表
| 维度 | redo | binlog |
|---|---|---|
| 层级 | 引擎层（InnoDB） | Server 层（所有引擎） |
| 性质 | 物理逻辑日志（页内变更向量） | 逻辑日志（SQL/行变更） |
| 写法 | 固定容量循环写 | 追加写、写满换文件、可长期保留 |
| 用途 | 崩溃恢复（前滚） | 复制、PITR、订阅同步 |
| 提交角色 | prepare/commit 两态 | 2PC 的「中间裁判」 |
| Oracle 对应 | online redo log | 归档日志 + 逻辑复制的合体 |

### 49. 复制链路
- 主库写 binlog → dump 线程推事件 → 从库 IO/receiver 线程写 relay log → SQL/协调线程重放。
- 位点体系：file+offset 或 GTID（gtid_mode=ON，切换不再人工对位点，生产标配）。
- 延迟观测：SHOW REPLICA STATUS（8.0.22+ 术语）的 Seconds_Behind_Source。

### 50. 延迟根因
- 单线程重放瓶颈、大事务、从库单点写压力、表无主键导致 ROW 重放全表扫。
- 加从库只分散读压力，不解决单链重放瓶颈。

### 51. 案例：无主键大表
- 2 亿行日志表无主键，ROW 格式复制下一条 UPDATE 全表扫，从库单条重放 40 秒，延迟数小时。
- 处置/预防：表必须有显式主键（sql_require_primary_key=ON 强制）；WRITESET 并行重放也依赖主键。

### 52. binlog 保留
- binlog_expire_logs_seconds 自动清理；删除原则：必须保证恢复链完整（全备之后所有 binlog 都在）。
- 空间紧张：先备份再缩短保留期，别把 PITR 命根子删了。

## 十、崩溃恢复与 doublewrite

### 53. 页断裂
- 16KB 页写入非原子，写到一半断电 → 页半新半旧，页尾校验对不上，redo 重放无从下手。

### 54. doublewrite 机制
- 刷脏时先把完整页副本顺序写 doublewrite 文件（fsync），再写正式 .ibd 位置。
- 恢复时 .ibd 页校验失败 → 从 doublewrite 取完整副本覆盖 → 再应用 redo。写入放大换来页级原子性，「便宜的保险」；生产保持 innodb_doublewrite=ON。

### 55. 崩溃恢复四步
① 扫描 redo 定位最后 checkpoint LSN，重放所有变更到 redo 末尾（页断裂先取 doublewrite 副本）；② 前滚完成，达到「崩溃瞬间」状态（已提交未刷盘 + 未提交的修改都回来了）；③ 2PC 对账：redo prepare 状态的事务查 binlog——XID 完整存在 → 提交；不存在 → 回滚；binlog 截断 → 回滚告警；④ undo 回滚未提交事务（8.0 允许先开放连接、后台继续回滚，恢复更快可用）。
- 与 Oracle 差异：前滚、回滚同源，MySQL 独有第③步——两套日志必须用 binlog 做裁判。

### 56. 2PC 的意义
- 保证主库恢复结果 = 从库重放结果；binlog 是裁判，崩溃时刻主备数据源不分裂。

## 十一、可靠性规划

### 57. 双 1 组合矩阵
| 组合 | 语义 | 适用 |
|---|---|---|
| 1/1（双 1，默认） | 提交时 redo 与 binlog 都 fsync，实质不丢已应答事务 | 金融/核心 OLTP 唯一选择 |
| 1/0 | redo 严格，binlog 每秒刷 | 主库宕机可能丢 binlog → 从库多数据主备不一致，慎用 |
| 2/1 | binlog 严格，redo 靠 OS 缓存 | 断电丢约 1 秒；从库可如此（不承担持久性） |
| 0/0 | 都靠每秒 | 仅测试/可全量重导边缘场景 |

### 58. 案例：关双 1 断电
- 设 flush_log_at_trx_commit=2, sync_binlog=0；机房断电后主库丢约 1 秒事务，从库比主库「新」，状态错乱。
- 启示：双 1 的成本是主备一致 + 持久性的保费；要性能去优化硬件与组提交，而不是关保险。

### 59. 容量规划清单
- buffer pool：50%～70% + dump/load 预热；redo：4G～8G 起步看 checkpoint age；undo：双表空间 + 自动截断（innodb_max_undo_log_size）；binlog：保留窗口 ≥ 最长恢复窗口；ibdata1：5.7 一开始就 1G:autoextend + 独立 undo。

### 60. 备份体系
- 物理备份：XtraBackup / MEB（热备，拷文件 + 应用 redo）；8.0.17+ Clone Plugin。
- 逻辑备份：mysqldump / mydumper（可读、可跨版本、大库慢）。
- PITR：全备 + mysqlbinlog 重放到指定时刻；GTID 使位点判断更可靠。
- 没演练过的备份等于没有备份。

## 十二、观测与诊断

### 61. 病象 → 病因速查
| 病象 | 病因 |
|---|---|
| 写入周期性抖动/跌零 | redo 太小 checkpoint age 顶满；或 page cleaner 跟不上 |
| 提交 RT 随负载上涨 | redo 盘慢、组提交失效（单并发）、binlog 盘慢、sync_binlog=1 且无 NVMe |
| undo 持续增长 | 长事务/大事务压住 purge（看 history list length 与 INNODB_TRX） |
| 主从延迟 | 大事务、从库单线程重放、无主键表 ROW 全扫、从库机器差 |
| 偶发锁等待超时 | 间隙锁/插入意向冲突（RR）、热点行串行、死锁 |
| 查询忽快忽慢 | buffer pool 污染（全扫）、统计信息过期、AHI 争用、页不在池中 |

### 62. 观测指标地图
- 整体：SHOW GLOBAL STATUS（Threads_running、Innodb_row_lock_waits、Aborted_connects）；buffer pool：SHOW ENGINE INNODB STATUS、sys schema（命中率、free buffers、脏页比例）；redo：Innodb_redo_log_capacity_*、checkpoint age、LSN 增速；长事务：INNODB_TRX、History list length；锁：performance_schema.data_lock_waits、sys.innodb_lock_waits；执行层：EXPLAIN/EXPLAIN ANALYZE（8.0.18+）、slow log、sys.statements_with_full_table_scans；复制：SHOW REPLICA STATUS、performance_schema.replication_*；表空间：INNODB_TABLESPACES、sys.schema_table_statistics（DATA_FREE 碎片率）。

### 63. 场景题：提交 RT 线性上涨
- 排查树：① redo 盘 IO 延迟（flush_log_at_trx_commit=1 时提交直接 fsync redo，用 iostat/io 延迟确认）；② 组提交是否失效（并发度低时每事务一次 fsync）；③ binlog 盘慢（sync_binlog=1 同样 fsync）；④ 硬件：无 NVMe/持久化缓存时的 fsync 代价。逐项用 SHOW ENGINE INNODB STATUS 的 LOG 段 + 磁盘指标验证。

### 64. 场景题：ibdata1 800GB
- 根因：5.7 的 undo 在 ibdata1 且长期长事务，ibdata 无法收缩。
- 处置：逻辑迁移重建实例（新库启用独立 undo），迁移窗口内停写。
- 启示：5.7 建实例时的 undo 布局决定命运；8.0 已默认规避。

### 65. 场景题：DDL 半途崩溃
- 现象：5.7 加字段中途 OOM kill，重启后字典与 .frm 不一致，表无法访问。
- 处置：以 frm/ibd 恢复工具 + 受控修复拉回，随后升级 8.0。
- 启示：8.0 原子 DDL（DD 进 InnoDB + redo 保护）正是为此而生——版本本身是最大的稳定性参数。

## 十三、常见误区与延伸

### 66. 十误区逐条
1. 「MySQL=InnoDB」✗：Server 层管 SQL 与 binlog，MyISAM 完全不走 redo/undo，谈事务先确认引擎。
2. 「binlog 就是 redo」✗：redo 管崩溃恢复（页级），binlog 管复制/PITR（逻辑级），2PC 负责对齐。
3. 「COMMIT 会把数据页写盘」✗：提交只保证 redo（+binlog）落盘，脏页由 page cleaner 异步刷。
4. 「database=Oracle 的 database」✗：MySQL 的 database≈schema；实例 vs 数据库换成 进程 vs datadir。
5. 「RR 不会产生间隙锁问题」✗：RR 下 UPDATE/DELETE 命中范围加间隙锁/临键锁，插入并发冲突是锁超时大户；能改 RC（配合 ROW 格式）更简单。
6. 「undo 越多越好」✗：undo 只该有「最老活跃事务」要求的那么多，多余版本全是 purge 的债。
7. 「sort/join buffer 调大就快」✗：连接级按需分配，盲目调大易 OOM，先看执行计划。
8. 「查询缓存能提速」（5.7）✗：表一更新全失效，高并发下成锁瓶颈，8.0 移除。
9. 「doublewrite 可以关」✗：页原子性的保险，部分写页恢复不了时，省下的 IOPS 换不回数据。
10. 「从库延迟靠加从库解决」✗：延迟多是重放能力问题（大事务/无主键/并行度），加从库只分散读压力。

### 67. 8.0 vs 5.7 变化
- 数据字典进 InnoDB → 原子 DDL；查询缓存移除；窗口函数/CTE/降序索引/隐藏索引（灰度下线神器）；undo 独立 + 自动截断；redo 8.0.30+ 在线可调；新复制术语（replica/source）+ WRITESET 并行复制；instant ADD COLUMN（秒加列）；8.4：change buffer 移除、默认认证收紧。

### 68. 引擎生态
- InnoDB：唯一严肃推荐（事务/行锁/MVCC/崩溃恢复）；MyISAM：表锁、无事务、无崩溃恢复，仅剩只读归档；Memory/Archive/CSV 各有专途；MyRocks 在写放大敏感场景（日志存储）有生产案例。

### 69. 复制进阶
- 半同步：从库收到 binlog 才应答，折中双 1 主库 + 异步复制的丢数据窗口。
- MGR/InnoDB Cluster：Paxos 组复制、多写/单写 + 自动故障转移 + MySQL Router，无共享存储（角色接近 RAC+DG 合体但语义不同）。
- 延迟从库：延迟 N 小时是最朴素的误操作保险；ROW 格式 binlog + 工具可实现行级闪回。

### 70. Oracle ↔ MySQL 映射
- Instance → mysqld；Schema/User → Database（库）；Tablespace+Data File → .ibd；Buffer Cache → Buffer Pool（LRU young/old 防污染）；Redo Log Buffer → log buffer/redo 文件；归档日志 → binlog（还兼复制）；Undo → undo 表空间/回滚段（CR 块 vs 版本链）；DBWn → Page Cleaner；LGWR → Log Writer；SMON undo 清理 → Purge 线程；控制文件 → 数据字典；8KB 块/ITL → 16KB 页/全局锁系统；Data Guard → 异步/半同步复制、MGR；RMAN → XtraBackup/Clone Plugin；AWR/V$ → performance_schema/sys（更自助，无内置 AWR）。
