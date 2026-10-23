# MySQL 面试题 —— 存储引擎、索引与查询

> 适用岗位：中高级后端 / DBA 方向。考察建议：索引是最高频区分点，沿追问链下探；手写执行计划解读考察真实功力。

---

## 一、存储引擎（必考）

1. InnoDB vs MyISAM 深度对比追问链：
   - 事务、行级锁、外键、崩溃恢复（redo log）四个维度分别什么区别？
   - InnoDB 为什么默认主键用聚簇索引？没有主键时会怎样（隐藏 rowid）？
   - MyISAM 还存在的合理使用场景有哪些（只读、全文检索旧版）？
   - 线上 `ALTER TABLE ... ENGINE=InnoDB` 迁移过吗？大表怎么迁移（pt-osc 流程）？
2. InnoDB 的三大特性追问链：
   - Buffer Pool：LRU 链表结构（old/new 分区，为什么分两段）、`innodb_buffer_pool_size` 怎么定、Buffer Pool 命中率怎么监控？
   - Change Buffer：解决什么问题（二级索引的随机写）、什么时候不起作用（唯一索引）、参数与触发条件？
   - 自适应哈希索引（AHI）：什么条件下构建、内存开销、为什么有时要关闭？
3. InnoDB 的磁盘结构：表空间（ibd/system tablespace）、区（extent，1MB 16 页）、页（16KB）、行格式（Compact/Dynamic）；`innodb_file_per_table` 的作用与坑。
4. 一条 UPDATE 语句在 InnoDB 内部的完整路径：Buffer Pool 读页 → 修改 → 脏页 → redo log → 双写缓冲（doublewrite，解决什么问题）→ checkpoint 刷盘——把每一环的数据流向讲清楚。

## 二、索引（必考，区分度最高）

5. B+ 树索引追问链：
   - 为什么 MySQL 选 B+ 树而不是 B 树、红黑树、哈希、跳表（写一个对比表）？
   - 树高怎么算：主键 bigint 8 字节 + 页指针 6 字节，一个 16KB 页能存多少索引项？2000 万行要几次 IO？
   - 为什么建议用自增主键而不是 UUID（页分裂、碎片、随机 IO）？
6. 聚簇索引与二级索引追问链：
   - 聚簇索引的叶子节点存什么；二级索引叶子节点存什么（主键值）？
   - 回表是什么，为什么要有覆盖索引？索引下推（ICP）优化了什么（条件在索引层过滤，减少回表）？
   - 联合索引（a, b, c）的完整分析：最左前缀、范围查询后失效、`ORDER BY`/`GROUP BY` 的排序利用、`a = 1 AND c = 3` 能用到几个索引列？
7. 索引失效场景全盘点（常考陷阱题）：
   - 函数运算（`DATE(col)`）、隐式类型转换（字符串列传数字）、`%前缀%` 模糊、`OR` 连接、`!=`/`NOT IN`、列参与运算——逐一说清为什么失效（无法定位索引区间）。
   - 追问：`a LIKE 'abc%'` 能用索引吗？`a IN (1,2,3)` 能吗？
8. 索引设计与优化实战：
   - 一张 5000 万行的订单表，常见查询：按用户查、按商家查、按时间区间查、按状态过滤——你的索引设计（联合索引列序怎么定：区分度、排序、覆盖）？
   - 冗余索引与无用索引怎么发现（`sys.schema_unused_indexes`）？索引维护的代价（写入放大）怎么评估？
   - 为什么区分度低的列（性别/状态）不适合单独建索引？什么时候反而有用（索引跳跃扫描 ICP 组合）？
9. 慢 SQL 分析（必考实操）：
   - `EXPLAIN` 每个关键列怎么读：`type`（const → eq_ref → ref → range → index → ALL 的成本排序）、`key_len` 怎么计算（varchar 变长 + 字符集 + NULL）、`rows` 与 `filtered`、`Extra`（Using filesort / Using temporary / Using index / Using where）？
   - `Using filesort` 什么时候出现、怎么消除？`Using temporary` 什么时候出现（distinct、union、group by）？
   - 优化器没走你预期的索引怎么办：`FORCE INDEX` vs `IGNORE INDEX` vs 直方图（`ANALYZE TABLE` 更新统计信息）？
10. 优化器与统计信息：`cardinality` 怎么来（采样）、统计信息过期导致走错索引的案例；`EXPLAIN FORMAT=JSON` / `optimizer trace` 怎么用？

## 三、查询优化与设计

11. 分页优化追问链：
    - 深分页 `LIMIT 1000000, 20` 为什么慢（扫描 100 万行丢弃）？
    - 优化方案对比：延迟关联（先查索引列再 join）、游标分页（`WHERE id > ?`）、覆盖索引分页——各适用什么场景？为什么"记住上页最后一条"的方案在跳页场景不行？
12. JOIN 的实现与优化：
    - Nested Loop / Block Nested Loop / Batched Key Access 的执行方式与成本；
    - join 驱动表选择原则（小表驱动大表、内连接优化器自选）；`join_buffer_size` 的作用；
    - 为什么大表 join 慢的根因是"无法下推的逐行扫描"——什么场景该拆成应用层多次查询？
13. 子查询与派生表：为什么有的子查询会被物化（materialization），物化的代价；`EXISTS` vs `IN` 的正确用法场景。
14. 场景题：
    - 排行榜需求（按分数倒序取 Top 100）怎么用索引与 SQL 写？
    - 相邻记录（上一页/下一页）取数、`LEAD/LAG` 窗口函数替代方案？
    - 全表扫 `COUNT(*)` 从 5 秒优化到 100ms 的方案清单（覆盖索引计数、缓存计数、预聚合表）——哪些可行哪些不可行？
15. 数据类型设计决策：
    - int/bigint/varchar/decimal/datetime 的存储与选择陷阱（隐式转换、精度丢失）；
    - 金额为什么用 decimal 而不是 float（浮点误差）？或考虑用分存储的取舍；
    - 字符集：utf8 vs utf8mb4（emoji、索引长度上限 767/3072 字节）、排序规则 collation 的影响。

---

## 附：评估要点

| 维度 | 权重 | 观察点 |
|---|---|---|
| 存储引擎 | 25% | Buffer Pool/Change Buffer/redo 链路讲到机制层 |
| 索引 | 45% | 追问链全接住、能现场分析 EXPLAIN、设计有理由 |
| 查询优化 | 30% | 有深分页/join/统计信息等实战案例 |

**判定要点**：能背"B+ 树、覆盖索引"算入门；能算树高、解释 key_len、现场拆解 EXPLAIN 算中级；有慢 SQL 治理数据（优化前后耗时）、主导过索引体系设计算高级。
