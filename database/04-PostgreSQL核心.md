# PostgreSQL 面试题 —— 架构、MVCC 与索引

> 适用岗位：中高级后端 / PG DBA 方向。考察建议：PG 与 MySQL 的对比意识贯穿始终，MVCC 与索引实现差异是最核心的区分点。

---

## 一、PG 架构与进程模型（必考）

1. PG 的进程模型追问链：
   - 多进程 vs MySQL 多线程的本质差异；每个连接一个 backend 进程，连接数上限（`max_connections`）为什么通常建议调小（进程内存 × 连接数）？
   - 共享内存（shared_buffers）与 WAL buffer 的作用；`postmaster` 与 `checkpointer` / `bgwriter` / `autovacuum launcher` / `walwriter` 各负责什么；
   - 连接池的两种形态：应用侧（PgBouncer 事务级/会话级）vs 内核侧（`max_connections` 与连接风暴）。
2. 场景题：应用突然建立 5000 个连接导致 OOM，PG 侧的缓解手段（连接池、`max_connections`、`shared_buffers` 调小、连接超时 `statement_timeout`）。

## 二、MVCC 与事务（必考，区分度最高）

3. PG 的 MVCC 实现追问链：
   - 每行数据里的系统列：`xmin` / `xmax` / `ctid` 分别记录什么；
   - UPDATE 的实现：**新版本插入新行 + 旧行标记**（相比 MySQL 的原地更新 + undo 回滚段，本质差异是什么）；
   - 可见性判断：`xmin < 当前事务快照且已提交` 才可见——与 MySQL 的 ReadView 对比（两个阵营怎么实现快照隔离）；
   - 由此推导：为什么 PG 更新会产生**表膨胀**（旧版本行占空间）——vacuum 的根源动机。
4. 隔离级别追问链：
   - PG 四种隔离级别实际等价于哪两种（读未提交被实现成读已提交、可重复读 → 快照隔离）？
   - PG 的 RR（快照隔离）与 MySQL RR 的差异：PG 的 RR 下**完全不存在幻读**（快照读覆盖所有读，包括 `SELECT ... FOR UPDATE` 也是快照锁）吗？`EvalPlanQual` 机制是什么（更新冲突时重读最新版本）？
   - 序列化隔离级别（SSI）：基于什么算法（读写依赖图、冲突检测）、什么场景才需要？
   - `deferrable` 事务与串行化快照（`SET TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY, DEFERRABLE`）有什么用？
5. 事务控制细节：`SAVEPOINT` 与子事务（`EXCEPTION` 块内部回滚）；`COMMIT` 的 WAL 刷盘（`synchronous_commit` 的三档：on/off/remote_apply 与数据丢失风险）；两阶段提交 `PREPARE TRANSACTION` 的用途（与 XA 关联）。

## 三、WAL 与可靠性（必考）

6. WAL 追问链：
   - 为什么所有变更先写 WAL（与 MySQL redo 同样的 WAL 思想，但 PG 的 WAL 是**逻辑级**还是物理级——实际上"物理-逻辑混合"怎么理解）；
   - `checkpoint` 机制：`checkpoint_timeout` / `max_wal_size` 触发、checkpoint 完成时 `pg_control` 更新——崩溃恢复起点怎么确定；
   - `wal_level` 的取值（minimal / replica / logical）与用途；
   - `pg_wal`（旧称 pg_xlog）膨胀与清理（归档与 `max_wal_size` 的联动、`wal_keep_size`）。
7. 崩溃恢复流程：读 `pg_control` → 从 checkpoint 位置重放 WAL → 恢复到哪个点（`recovery_target_time` 等恢复目标的使用）——恢复与 MySQL 的差异。
8. 归档与备份：`archive_mode=on` + `archive_command` 的配置；连续归档备份（基础备份 + WAL）与 PITR（时间点恢复）的完整步骤；与 XtraBackup 的逻辑对比（PG 无官方在线物理备份工具，常用 pg_basebackup / barman / pgBackRest）。

## 四、索引体系（必考）

9. B-Tree 索引追问链：
   - 与 MySQL 的关键差异：**索引与数据存储分离**（PG 的表是无序堆表，索引是独立结构）——没有聚簇索引！这带来什么影响（回表代价、非主键列也能唯一）？
   - 排序规则（collation）与操作符类（opclass，如 text_pattern_ops）；索引的 NULL 处理（NULL 可以进索引，与 MySQL 差异）；
   - `INCLUDE` 子句（含包含列，类似覆盖索引但约束不同）与 MySQL 覆盖索引的对比；
   - 索引失效：函数索引、隐式类型转换——PG 中怎么用表达式索引解决。
10. 非 B-Tree 索引盘点：
    - Hash 索引（等值查询，PG10 后支持崩溃安全）；
    - GIN（通用倒排）：数组、JSONB、全文检索的实现与适用场景；
    - GiST（通用搜索树）：几何、范围类型、全文检索的替代实现；
    - BRIN（块范围索引）：适合什么数据（顺序插入的大表、时间序列），与 B-Tree 的代价对比；
    - Bloom 索引（多列等值组合）与 SP-GiST 的场景。
11. 部分索引与表达式索引实战：`CREATE INDEX ... WHERE status = 'active'` 的场景；函数索引如何让 `WHERE lower(email) = ?` 走索引。
12. 场景题：一张 5 亿行的日志表（按时间顺序插入、按时间范围查询、按用户 ID 等值查询、偶尔按 JSONB 字段过滤）——设计索引方案（BRIN + B-Tree + GIN 的组合）与清理策略。

## 五、SQL 与查询优化（含 JSONB/全文检索特色）

13. 执行计划解读（必考）：
    - `EXPLAIN (ANALYZE, BUFFERS)` 的每个输出怎么读：`Seq Scan` vs `Index Scan` vs `Bitmap Heap Scan` vs `Index Only Scan` 的代价模型；
    - 为什么 PG 在大比例读取时**故意选择 Seq Scan**（顺序 IO 比随机 IO 快）——与 MySQL 优化器的差异；
    - 嵌套循环 / 哈希连接（Hash Join）/ 归并连接（Merge Join）的选择条件与 `work_mem` 的关系（哈希表超过 work_mem 会落盘）；
    - 并行查询（`parallel_workers`、`min_parallel_table_scan_size`）在什么场景收益大、什么场景反而慢。
14. 统计信息与代价模型：`ANALYZE` 的采样、`default_statistics_target` 与多列统计（`CREATE STATISTICS`）；`plan_cache_mode` 与 prepared statement 的计划缓存（PG 默认每次重新规划 vs MySQL 的计划缓存差异）。
15. JSONB 追问链（PG 特色必考）：
    - JSONB 与 JSON 的差异（二进制存储、去重、键排序）；
    - JSONB 的索引：GIN 的 `jsonb_path_ops` vs 默认操作符类的区别（大小、查询能力）与选择；
    - JSONB 作为"动态列"与"传统加列 + NULL"的取舍；什么时候 JSONB 是反模式（频繁更新、高基数查询字段）。
16. 全文检索：tsvector / tsquery、`to_tsvector` 与中文分词（zhparser/pg_jieba 的扩展）、与 Elasticsearch 的定位差异（什么时候该上 ES）。
17. 窗口函数与高级 SQL：ROW_NUMBER 去重、LAG/LEAD 环比、`DISTINCT ON`（PG 独有语法）的用途——写一条实际业务 SQL。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 架构与进程模型 | 15% | 多进程模型、shared_buffers 与连接规划 |
| MVCC 与事务 | 30% | xmin/xmax 推导、表膨胀根源、快照隔离边界 |
| WAL 与可靠性 | 20% | checkpoint/恢复目标、PITR 步骤 |
| 索引体系 | 20% | 无聚簇索引的推论、GIN/GiST/BRIN 选型 |
| SQL 与执行计划 | 15% | 能读 EXPLAIN BUFFERS、work_mem 落盘理解 |

**判定要点**：能对比 MySQL 说清"PG 多版本在行、无聚簇索引"算中级；能现场推导可见性规则、设计混合索引方案、读懂并行与落盘计划算高级；有 PG 生产调优（膨胀治理、连接风暴、PITR 恢复演练）案例算资深。
