# MySQL 面试题 —— 事务、锁与隔离级别

> 适用岗位：中高级后端 / DBA。事务与锁是并发正确性的核心，死锁实战案例是区分真实经验的关键题。

---

## 一、事务与 ACID

1. ACID 四个特性的实现机制分别靠什么：
   - 原子性（undo log 回滚）、持久性（redo log + 双写）、隔离性（MVCC + 锁）、一致性（约束 + 前两者保证）——把每个特性落到具体组件上。
2. 事务的启动与提交：`autocommit`、`BEGIN` vs `START TRANSACTION`、`COMMIT` 时 redo log 怎么落盘（`innodb_flush_log_at_trx_commit` 的三个取值与崩溃风险矩阵）？
3. 长事务的危害追问链：
   - 占用 undo log（回滚段膨胀）、锁持有时间过长、binlog 大事务延迟复制——线上长事务怎么发现（`information_schema.innodb_trx` / performance_schema）与治理？
4. 分布式事务简述：MySQL 侧 XA（两阶段）与本地消息表/柔性事务的关系；XA 在什么场景真被用过？

## 二、MVCC（必考）

5. MVCC 完整原理追问链：
   - undo log 版本链的结构：每个版本记录什么（trx_id、roll_pointer）；
   - ReadView 的四个成员（m_ids、min_trx_id、max_trx_id、creator_trx_id）与判断规则：某版本对当前事务是否可见的完整判断逻辑（版本 trx_id 在 m_ids 里吗？小于 min 吗？等于 creator 吗？）；
   - RC 与 RR 下 ReadView 生成时机的差异（每次快照 vs 首次快照），由此推导两种隔离级别下不可重复读是否被解决；
   - 快照读 vs 当前读（`SELECT ... FOR UPDATE` / `UPDATE` / `DELETE` / `LOCK IN SHARE MODE`）在实现上的区别——为什么 RR 下幻读"看起来被解决了"但其实没有。
6. 一致性读与当前读的场景题：
   - RR 下事务内第一次 SELECT 后，另一事务插入并提交，再 SELECT 为什么看不到（快照）？但 `UPDATE` 能"看到"并锁住它吗（当前读 + 幻读仍在）？
   - 推导题：A 事务先 SELECT（快照），B 事务 UPDATE 同一行提交，A 再 UPDATE 这行——A 的行为是什么（当前读读到 B 的新版本，加锁），为什么"更新丢失"被避免了？

## 三、锁机制（必考）

7. InnoDB 锁的类型全盘点：
   - 共享锁/排他锁（S/X）、意向锁（IS/IX，为什么需要——加表锁前快速判断）、自增锁（AUTO-INC 的三种模式与复制安全问题）；
   - 行锁的三种实现：记录锁（Record Lock）、间隙锁（Gap Lock）、临键锁（Next-Key Lock）；
   - 表锁与元数据锁（MDL）：DDL 时 MDL 阻塞全表 DML 的经典事故。
8. 临键锁与间隙锁追问链（必考，最高频区分题）：
   - 间隙锁锁的是什么（范围而非行），RR 隔离级别下唯一索引等值查询什么时候加间隙锁、什么时候只加记录锁（查询存在与否、唯一索引 vs 普通索引）？
   - 场景推演：`SELECT * FROM t WHERE id = 10 FOR UPDATE`（id 有普通索引，表中无 10 但有 5 和 15），会锁哪些范围？
   - 为什么 RC 隔离级别没有间隙锁（由此推导 RR 才防幻读）？
   - 间隙锁的负面影响：范围更新（`UPDATE ... WHERE status='N'`）可能锁住大量无关行的阻塞案例；删除间隙锁的方案（把隔离级别降到 RC / 用唯一索引精确匹配 / 改造 SQL）。
9. 死锁追问链（实战题）：
   - 死锁产生的四个必要条件在 InnoDB 里怎么体现；
   - 现场推演：两个事务分别 `UPDATE a` 再 `UPDATE b` 与反向操作，哪个时刻互相持锁等待；
   - 怎么定位：`SHOW ENGINE INNODB STATUS` 的 LATEST DETECTED DEADLOCK 怎么读（两个事务的持锁/等待的 lock mode、等待的 index/record）？
   - 死锁处置策略：死锁检测开关（`innodb_deadlock_detect`）在大并发下的性能开销（为什么热点行更新时关掉检测反而更快，但代价是可能长时间等待）、事务重试机制怎么落地；
   - 讲一个你真实遇到的死锁案例：场景、锁类型、解决（改 SQL 顺序/索引/隔离级别）。
10. 锁等待与阻塞排查：
    - `information_schema.innodb_trx / innodb_lock_waits / innodb_locks` 三张表怎么看当前谁阻塞谁？
    - `innodb_lock_wait_timeout` 与监控告警；一条 UPDATE 卡住 50 秒的排查路径（事务未提交 → 间隙锁 → 大事务）。
11. 热点行更新优化：
    - 单行热点（如库存、计数器）高并发更新的瓶颈（行锁排队、死锁检测开销）；
    - 优化方案对比：异步队列、预扣减/缓冲（Redis 前置）、分桶（库存分 N 行）、按业务键哈希路由——各方案的一致性与复杂度权衡。

## 四、隔离级别与一致性

12. 四种隔离级别的现象矩阵：脏读/不可重复读/幻读/丢失更新分别在哪个级别出现，画矩阵；
13. 隔离级别选型：生产为什么默认 RR（InnoDB 默认、配合 binlog row 格式的复制一致性）？什么场景你会主动降到 RC（并发与间隙锁代价）？MVCC 在 RR 下的"快照一致性读"解决了什么业务痛点？
14. 一致性读在备份与只读从库的应用：`--single-transaction` 备份原理（一致性快照）、从库长事务导致主从延迟的关联。
15. 场景题：一个转账接口（扣 A 加 B，金额不可为负），在不引入分布式锁的前提下，用 SQL 与隔离级别怎么保证正确且高并发（`UPDATE SET balance = balance - ? WHERE id = ? AND balance >= ?` 的行锁原子性、乐观锁版本号对比）。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| MVCC | 35% | ReadView 判断规则能现场推导、快照读/当前读边界清晰 |
| 锁机制 | 40% | 间隙锁场景推演正确、死锁案例真实、有热点行治理 |
| 事务与隔离 | 25% | ACID 落到组件、隔离级别选型有理由 |

**判定要点**：背得出"MVCC 多版本"算入门；能现场推演 ReadView 与间隙锁加锁范围算中级；有死锁/锁等待实战案例、主导过热点行优化算高级。
