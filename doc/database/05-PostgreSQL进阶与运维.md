# PostgreSQL 面试题 —— VACUUM、复制、分区与运维

> 适用岗位：中高级后端 / PG DBA。VACUUM 与复制是 PG 运维的高频事故区，膨胀治理案例最能区分实战深度。

---

## 一、VACUUM 与膨胀治理（必考）

1. VACUUM 原理追问链：
   - VACUUM 干什么：清理已死行（dead tuple）、更新空闲空间映射（FSM）、更新可见性映射（VM，索引-only 扫描依赖）——三件事分别解决什么问题；
   - 为什么 VACUUM **不能**物理缩小表文件（空间被复用而不是归还 OS）？`VACUUM FULL` 的代价（表锁 + 重写）与适用时机；
   - `autovacuum` 机制：触发阈值（`autovacuum_vacuum_threshold` + `autovacuum_vacuum_scale_factor`）、为什么大表默认参数会失效（scale_factor 对大表阈值过高）——你线上怎么调。
2. 表膨胀的定位与治理（必考）：
   - 怎么测膨胀率（`pgstattuple` 扩展 / `pg_stat_user_tables` 的 n_dead_tup 监控）；
   - 膨胀根因：长事务（快照持有阻止清理）、未提交的旧事务、频繁 UPDATE/DELETE、关闭 autovacuum；
   - 治理 SOP：找长事务（`pg_stat_activity` 的 backend_xid/backend_xmin）→ 杀事务 → 触发 VACUUM → 必要时 `VACUUM FULL`/重建（`pg_repack` 在线重建）；
   - 场景题：某核心表膨胀到物理大小的 10 倍，查询变慢，磁盘告警——你的完整处置流程。
3. 事务 ID 回卷（XID wraparound）追问链（PG 特有高危）：
   - 为什么必须防止 XID 回卷（32 位 + 比较逻辑、冻结 freeze）？`FREEZE` 与 `datfrozenxid`；
   - 数据库进入"接近回卷"只读保护状态的先兆（`age(datfrozenxid)` 监控）与急救（强制 VACUUM FREEZE / 临时关闭长事务）；
   - 你说过怎么监控 XID 年龄（`pg_database` 的 datfrozenxid、多实例告警阈值如 1.5 亿）。

## 二、复制与高可用（必考）

4. 流复制（Streaming Replication）追问链：
   - 物理复制原理：WAL 流式传输（receiver 进程 → 写入本地 WAL → 重放）；与 MySQL binlog 复制的对比（逻辑 vs 物理、可否跨版本/跨架构）；
   - `synchronous_standby_names` 的配置：同步复制（first/ANY 语法）、`synchronous_commit` 的 remote_apply/remote_write/on 三档语义；
   - 复制槽（replication slot）：防止 WAL 被清理导致从库无法追平，槽泄漏导致主库 WAL 无限膨胀的经典事故；
   - 级联复制（级联 standby）与同步级联（cascade + sync）的配置。
5. 主从切换（failover）：
   - `pg_ctl promote` / `pg_promote()`、trigger_file（已废弃）→ 新版提升机制；
   - 脑裂防护与 `recovery.conf` 迁移到 `postgresql.auto.conf` 的机制变化；
   - 切换后从库重新挂回（`pg_rewind` 快速找回差异 WAL，基于时间线 timeline）——对比 MySQL 重搭从库的开销；
   - 时间线（timeline）与 `history` 文件：为什么 PITR 恢复后不能直接复用原 WAL（分叉）？
6. 高可用方案对比：Patroni（基于 etcd/consul/ZK 的自动 failover，核心主从共识）vs 云 RDS / Stolon / repmgr——你生产怎么选，为什么；
7. 逻辑复制追问链（PG10+，高频新增考点）：
   - 逻辑复制 vs 物理复制的适用差异（跨版本、部分表、跨 PG 到其他系统）；
   - 逻辑复制的限制清单：无 DDL 复制（需扩展 pglogical/EventTrigger）、序列不自动同步、大事务内存、序列冲突；
   - 逻辑复制做双向同步（BDR/自研冲突解决）的复杂度与风险；
   - 用逻辑复制做"跨大版本升级"（13 → 16）的步骤与切换注意点。

## 三、分区表（必考）

8. PG 分区表追问链：
   - PG11 之前的继承分区（继承 + 触发器）的痛点；PG11+ 声明式分区（`PARTITION BY RANGE/LIST/HASH`）的改进（自动路由、分区裁剪）；
   - 分区裁剪（partition pruning）与执行计划的关系；分区索引（每个分区单独建索引）与全局索引的取舍（PG 没有全局索引的代价——唯一约束只能在分区内）；
   - 分区维护：新增分区、分区与查询性能的关系（分区太多反而慢——元数据与计划开销，经验值分区数上限）；
   - 分区表 + 自动删除旧分区（按月分区，保留 12 个月，`DROP PARTITION` 比 DELETE 快多少——无 WAL 逐行、空间立即释放）vs MySQL 分区表的对比（MySQL 分区限制：唯一索引必须含分区键、无自动裁剪优化器）。
9. 场景题：日志表按天分区的方案设计（分区命名、保留策略、统计信息更新、查询是否总是能裁剪）。

## 四、权限、安全与扩展生态

10. 权限模型追问链：
    - role（角色）与 user、`GRANT` 的层级（database/schema/table）；`PUBLIC` 的默认权限风险；
    - 行级安全（RLS，Row Level Security）：`CREATE POLICY` 实现多租户隔离（一个库多个租户共享表）的方案与性能注意点；
    - 列级权限（column privileges）与函数安全（SECURITY DEFINER 的提权风险）。
11. 扩展生态（PG 特色）：
    - 必装扩展盘点：pg_stat_statements（慢 SQL 统计）、pg_stat_monitor、pg_hint_plan（手动指定执行计划）、pg_repack、pg_partman（自动分区）、PostGIS、pgvector（向量检索）——各自的定位；
    - 你实际部署过哪些扩展，遇到什么坑（版本兼容、shared_preload_libraries）。
12. pgvector 与 AI 场景：向量索引（HNSW/IVFFlat）的适用、与专用向量库（Milvus）的边界判断。
13. 中文场景：PG 的排序（zhparser/pg_jieba + ICU collation）与 MySQL 的 utf8mb4 对比。

## 五、性能调优与监控

14. 参数调优清单（给依据）：
    - shared_buffers 为什么不是越大越好（PG 的 shared memory 缓存 + OS page cache 双缓存，超过 30% 物理内存的回报递减）；
    - work_mem 与排序/哈希落盘：为什么调大全局会内存爆炸（per 操作 × 并发）；
    - effective_cache_size 只影响优化器代价估算（与真实缓存无关）；
    - max_connections、maintenance_work_mem（VACUUM/索引重建用）、wal_buffers、checkpoint 参数的联动；
    - 并发模型：PG 的锁粒度与 MVCC 下读不阻塞写（与 MySQL 的最大卖点差异）、`statement_timeout` 与锁等待（`lock_timeout`）的配置。
15. 监控与排障：
    - `pg_stat_activity` 的核心列（state、wait_event、backend_xid）与"数据库假死"排查；
    - 锁冲突排查：`pg_locks` 结合 `pg_stat_activity` 找阻塞源（wait_event 为 lock）、`pg_blocking_pids()`；
    - pg_stat_statements 怎么找 Top SQL；慢查询 `log_min_duration_statement` 与 auto_explain；
    - wait_event 分类（IO/锁/扩展）的快速判断。
16. 场景题：
    - 数据库"连接打满、CPU 低、应用卡死"的排查流程；
    - 一个事务持有锁 1 小时不提交，业务全阻塞——你的处置（查阻塞源 → 评估 kill 的连带影响 → kill 后未提交回滚的时间预估）；
    - 从 MySQL 迁 PG 的核心适配清单（SQL 兼容性、自增序列、时间类型、JSON、事务隔离差异、死锁重试）。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| VACUUM 与膨胀 | 30% | 长事务根因、XID 回卷、pg_repack 案例 |
| 复制与高可用 | 30% | 同步复制语义、复制槽事故、逻辑复制边界 |
| 分区与扩展 | 15% | 声明式分区细节、扩展实战 |
| 权限安全 | 10% | RLS 多租户、SECURITY DEFINER 风险 |
| 调优监控 | 15% | 参数有依据、锁排查流程 |

**判定要点**：知道 autovacuum 与流复制算中级；能处置膨胀与 XID 回卷、排锁冲突、规划逻辑复制算高级；主导过 Patroni 高可用、跨版本升级、从 0 建设监控体系的算资深。
