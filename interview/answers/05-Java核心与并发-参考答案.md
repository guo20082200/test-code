# Java 核心、并发与 JVM 参考答案

> 以下为「要点框架 + 关键细节 + 权衡取舍」，追问链答案覆盖到第二层原理，面试需结合个人项目展开。原题见 ../05-Java核心与并发.md

---

## 一、Java 语言基础

### 1. `==` 与 `equals`；`hashCode` 约定与 HashMap 隐患

> 原题要点：== 与 equals 的区别；hashCode 与 equals 的约定；重写 equals 不重写 hashCode 在 HashMap 里的后果。

- `==`：基本类型比较值；引用类型比较地址（是否同一对象）。`equals` 默认继承 Object 即 `==`，重写后表达业务等价（如 String 按内容）。
- `hashCode` 约定（Object 规范）：
  - 一致性：equals 未变时多次调用 hashCode 返回相同值；
  - equals 相等 ⇒ hashCode 必须相等；
  - hashCode 相等 ⇏ equals 相等（哈希冲突合法）。
- 只重写 equals 不重写 hashCode：两个业务相等的对象 hashCode 不同 → put 与 get 被散列到不同桶 → HashMap「存进去取不出来」、HashSet 去重失效。
- 连带坑：可变对象作 key，put 后修改参与 hash 的字段同样导致 key 丢失；key 应使用不可变对象。

### 2. String 不可变、三者选型、常量池与 `intern()`

> 原题要点：String 为何设计成不可变；String/StringBuilder/StringBuffer 选型；常量池与 intern() 行为。

- 不可变的设计动机：
  - 安全：String 承载类名、路径、URL、连接参数，被篡改后果严重；
  - 线程安全：不可变对象可无同步共享；
  - 常量池复用与 hashCode 缓存（String 内部懒缓存 hash，才能当高效 HashMap key）。
- 实现：final 类 + 内部 final byte[]（JDK9 compact strings），不暴露任何修改入口。
- 选型：循环/大量拼接用 StringBuilder；多线程共享拼接才用 StringBuffer（synchronized，实际少见）；少量静态 `+` 编译期优化即可，但循环内 `+` 每轮新建 StringBuilder。
- 常量池：编译期字面量进 class 常量池；运行期 String Table 在堆中（JDK7 起从永久代迁出）。`new String("a")` 最多两个对象（池 + 堆）；`intern()` 返回池中首次登记的引用，JDK7+ 登记的是堆对象引用（可复用堆中对象）。

### 3. 深/浅拷贝；Cloneable 的缺陷与替代

> 原题要点：深浅拷贝的实现方式；Cloneable 为什么是缺陷设计；替代方案。

- 浅拷贝：只复制对象第一层，引用字段仍共享；深拷贝：引用图递归复制。
- 实现方式：① Cloneable + 重写 clone()（逐层手动 clone）；② 序列化 round-trip（Java 序列化/JSON）；③ 拷贝构造器/静态工厂 copyOf；④ 映射工具（MapStruct 等）。
- Cloneable 缺陷（Effective Java Item 13）：
  - 空接口却改变 clone() 行为，职责混乱；
  - clone() 不走构造器，绕过不变量校验；默认浅拷贝易漏字段；
  - final 字段无法在 clone 中重新赋值，不可变类深拷贝受限；
  - 继承链任何一层实现错误都会传染。
- 替代：拷贝构造/工厂（List.copyOf、record 天然防御性拷贝）、序列化深拷贝、优先不可变对象直接共享。

### 4. 异常体系；finally 的例外；try-with-resources 字节码原理

> 原题要点：Checked vs Unchecked 的取舍；finally 何时不执行；try-with-resources 的字节码原理。

- 体系：Throwable → Error（OOM、StackOverflowError，不该捕获）与 Exception → Checked（IOException 等）与 Unchecked（RuntimeException）。
- 取舍：可预期、可恢复的失败（IO/远程调用）用 checked 强制调用方处理；编程错误/前置条件违规用 unchecked。代价：checked 在 lambda/Stream 中难用、污染签名，现代框架多倾向 RuntimeException + 全局异常处理。
- finally 不执行：`System.exit()`、JVM 崩溃、`kill -9`、守护线程随 JVM 退出、线程永久阻塞在 try 内、断电。另注意 finally 中 return 会吞掉 try 的返回值/异常（bad practice）。
- try-with-resources 原理：编译器脱糖为 try-finally 结构，资源存入合成局部变量，正常与异常路径都调用 close()；主体和 close() 都抛异常时主体异常为主、close 异常经 `addSuppressed` 保留；要求实现 AutoCloseable。

### 5. default 方法 vs 抽象类；JDK8~21 常用特性

> 原题要点：接口 default 方法与抽象类的区别；常用语言特性各解决什么问题。

- default 方法：接口可带实现，核心动机是接口演进（Collection 加 stream 不破坏存量实现）；多实现冲突必须显式 `A.super.m()` 裁决。
- 抽象类：单继承、可有状态（实例字段）与构造器、模板方法；表达 "is-a" 的部分实现复用。一句话区分：抽象类复用「状态 + 骨架」，default 方法解决「行为多继承 + 接口演进」。
- 特性清单（按解决什么问题）：
  - JDK8：Lambda/Stream（声明式数据处理）、Optional（显式可空语义，减少 NPE）、default/static 接口方法；
  - JDK9~11：var 推断、List.of 不可变工厂、HttpClient；
  - JDK14~16：Record（不可变数据载体，自动 equals/hashCode/toString）、switch 表达式、instanceof 模式匹配、文本块；
  - JDK17：sealed 密封类（受控继承体系，配合 switch 模式匹配穷尽性检查）；
  - JDK21：虚拟线程（IO 密集高并发，同步写法拿异步吞吐）、Record Patterns、模式匹配 switch。
- 使用建议：Optional 只用于返回值；Record 替代纯 DTO/值对象；Stream 以可读性为先，复杂逻辑拆步骤。

### 6. 反射开销与优化；JDK vs CGLIB 动态代理

> 原题要点：反射的性能开销在哪、怎么优化；模块化的 setAccessible 限制；JDK 与 CGLIB 代理区别与限制。

- 反射开销：
  - Method/Field 查找（getDeclaredMethod 会拷贝内部数组）；
  - 每次调用的访问检查 + 参数 Object[] 装箱；
  - 难以静态内联（inflation 机制：默认调用约 15 次后生成 GeneratedMethodAccessor 字节码类，开销骤降但仍有包装层）。
- 优化：static final 缓存 Method/Field；setAccessible(true) 关闭检查；热路径换 MethodHandle（invokeExact，JIT 友好可内联）或 LambdaMetafactory（生成接口 lambda，接近直调）；VarHandle 替代字段反射读写。
- 模块化限制（JDK9+）：跨模块访问非 public 成员需目标模块 `opens`；运行时逃生门是 `--add-opens`（框架升级常踩）。
- 代理对比：
  - JDK 动态代理：基于接口（Proxy + InvocationHandler，生成 `$Proxy0`），只能代理接口方法；
  - CGLIB：继承目标类生成子类 + MethodInterceptor，不能代理 final 类/final 方法/private；构造函数会执行两次（Objenesis 可规避）；
  - Spring 默认：有接口走 JDK，无接口走 CGLIB（proxyTargetClass 可强制）。
## 二、集合框架

### 7. HashMap 源码级追问链

> 链路：put 流程 → 扰动函数 → 容量 2 的幂 → 树化阈值 8/退化 6 → JDK7 头插死循环 → JDK8 修复与残留问题。

**put 完整流程（JDK8 putVal）**
- hash 扰动：`(h = key.hashCode()) ^ (h >>> 16)`；空表先 resize 懒初始化；
- `(n - 1) & hash` 定位桶：空桶直接放入；首节点命中（hash 相等且 ==/equals）覆盖 value；
- 首节点是 TreeNode 走红黑树插入；否则链表遍历（尾插），命中覆盖，否则尾插新节点；
- 插入后链长 ≥ 8：先看容量 ≥ 64（MIN_TREEIFY_CAPACITY），不满足优先扩容，满足才 treeifyBin；
- 最后 `++size > threshold` 触发 resize：容量翻倍，按 `hash & oldCap` 新增位把桶拆成 lo/hi 两条链，节点要么留原位、要么移到「原位 + oldCap」，保持相对顺序。

**为什么高 16 位异或、为什么容量是 2 的幂**
- 定位只用低位（n 小时只用到低几位），高 16 位信息全丢；高低位异或让高位也参与寻址，减少碰撞；
- 2 的幂使 `(n-1) & hash` 等价于取模但只需位运算；扩容翻倍时由新增位决定去留，无需重算哈希且拆分均匀。

**树化阈值 8 / 退化 6**
- 源码注释：负载 0.75 下链长服从泊松分布，长度到 8 的概率约亿分之六，8 是「基本不会到」的兜底值，防哈希退化攻击而非常态；
- 退化用 6 而非 8：留缓冲带，避免在阈值附近反复插删导致链表↔红黑树来回转换；
- 树化前提：桶数组长度 ≥ 64，小表冲突靠扩容解决更划算。

**JDK7 死循环与 JDK8 修复**
- JDK7 transfer 头插法：并发扩容时两线程各自 transfer，同一链表被反转成 A→B→A 环，后续 get 死循环 CPU 100%；
- JDK8 改尾插保序 + 扩容拆 lo/hi 链，并发扩容不再成环；
- 残留问题：HashMap 仍非线程安全——并发 put 丢数据、size 不一致、树化竞争异常；并发场景必须换 ConcurrentHashMap。

### 8. ConcurrentHashMap

> 原题要点：JDK8 相比 JDK7 分段锁改了什么；size() 实现；能否存 null；扩容期间 get 的行为。

- JDK7：Segment（ReentrantLock 子类，默认 16 段）分段锁，锁粒度 = 段。JDK8：去掉 Segment，改为 CAS + synchronized 锁「单个桶的头节点」，粒度更细；结构与 HashMap 对齐（数组 + 链表 + 红黑树），sizeCtl 控制初始化/扩容，多线程 helpTransfer 协助扩容（ForwardingNode 标记已迁移的桶）。
- size()：先 CAS 累加 baseCount，竞争大时迁移到 CounterCell[]（LongAdder 思路分散热点），sumCount 汇总；并发下是弱一致近似值（无全局快照锁）。
- 不允许 null（key/value 都不行）：get 返回 null 有二义性——「不存在」还是「值就是 null」？单线程 HashMap 可用 containsKey 消歧，并发下两次调用间状态可能被改、无法消歧，Doug Lea 干脆禁止。
- 扩容期间 get：get 全程无锁（Node.val/next 为 volatile），遇到 ForwardingNode 转发到 nextTable 继续查，保证读到已迁移数据；返回弱一致结果。

### 9. ArrayList 扩容；subList / Arrays.asList 的坑；CopyOnWriteArrayList

> 原题要点：ArrayList 1.5 倍扩容；subList 的坑；Arrays.asList 的坑；COW 的适用场景与缺点。

- 扩容：默认容量 10 且懒分配（首次 add 才分配数组）；grow = `oldCapacity + (oldCapacity >> 1)` 即 1.5 倍，Arrays.copyOf 整组复制；可预测大小时 `new ArrayList<>(n)` 预分配避免多次复制。
- subList：视图！持有原 list 引用，原 list 结构性修改后访问 subList 抛 ConcurrentModificationException；对 subList 的修改直接写原数组；强转回 ArrayList 抛 ClassCastException。
- Arrays.asList：返回定长视图 Arrays$ArrayList，add/remove 抛 UnsupportedOperationException；修改元素会反映到原数组；传基本类型数组时整个数组被当单个元素（需 IntStream 或装箱）。
- CopyOnWriteArrayList：写时复制整组 + volatile 保证可见性，读无锁；适合读极多写极少（监听器列表、配置白名单）；缺点：写 O(n) 复制、双倍内存、写串行、迭代器快照弱一致不反映最新写、高频写性能灾难。

### 10. Fail-Fast 与 Fail-Safe

> 原题要点：modCount 的作用；哪些集合是安全的。

- Fail-Fast：ArrayList/HashMap 等迭代器创建时记录 expectedModCount = modCount，每次 next 校验 modCount 未变，变了抛 ConcurrentModificationException——尽力尽早暴露「迭代期间结构性修改」，不是线程安全承诺（modCount 非 volatile）。
- 单线程遍历中删除的正确姿势：iterator.remove()、removeIf、先收集后删除。
- Fail-Safe：在副本上迭代，不抛异常但弱一致：CopyOnWriteArrayList/Set、ConcurrentHashMap 的迭代器与视图。
- 没有绝对安全的集合：按一致性需求选择——强一致需外部锁，弱一致接受快照/最终一致。

### 11. 场景题：访问顺序淘汰 Map / 排序 + 并发 / 手写 LRU

> 原题要点：按访问顺序淘汰的 Map 用什么；排序 + 并发用什么；手写 LRU 的思路（LinkedHashMap accessOrder / 双向链表 + HashMap）。

- 按访问顺序淘汰：LinkedHashMap（构造传 accessOrder=true），get/put 触发 afterNodeAccess 把节点移到队尾，重写 removeEldestEntry 控制容量淘汰——非线程安全，需外部同步。
- 排序 + 并发：ConcurrentSkipListMap（跳表，无界，支持 Comparator 与范围查询 headMap/tailMap/floorKey）；数据量小、写少也可锁 TreeMap。
- 手写 LRU 两种思路：
  - 借力实现：`LinkedHashMap` + 重写 `removeEldestEntry(size() > capacity)`，三行搞定（先说这个体现工具熟练度）；
  - 底层手写：HashMap<K, Node> + 双向链表（头部=最新，尾部=最旧）：get/put 命中则摘链移到头部；put 超容量删尾；get/put 均 O(1)；加虚拟头尾节点消除边界判断；并发版整体加锁或分段加锁。
- 加分项：生产单机缓存首选 Caffeine（W-TinyLFU：频率统计 + 访问记录走异步队列削峰，命中率和锁竞争都优于朴素 LRU）。
## 三、并发编程

### 12. JMM：happens-before、volatile、i++、DCL

> 链路：happens-before 规则与 volatile 双语义/内存屏障 → i++ 为什么线程不安全 → DCL 为什么必须 volatile。

**happens-before 与 volatile**
- 常用规则：程序顺序（单线程内）、监视器锁（unlock 先于后续 lock）、volatile（写先于后续读）、线程 start/join、传递性。hb 是可见性推导工具：有 hb 关系，后者必能看到前者的写入。
- volatile 双语义：可见性（写后立即对其他读线程可见）+ 有序性（禁止特定重排序）；不保证原子性。
- 内存屏障插入（JSR-133 Cookbook 简化）：volatile 写前插 StoreStore、写后插 StoreLoad；volatile 读后插 LoadLoad + LoadStore；StoreLoad 全屏障最贵，x86 上体现为 lock 前缀指令。

**i++ 为什么线程不安全**
- `i++` 是读（getfield）→ 改（iadd）→ 写（putfield）三步，非原子，线程交错导致丢失更新（后写覆盖前写）；
- 三大特性归属：原子性由锁/CAS/原子类保证；可见性由 volatile/synchronized/final 保证；有序性由 volatile/happens-before 保证（单线程内 as-if-serial）；
- 修复：AtomicInteger（CAS 自旋，高竞争失败率高可换 LongAdder）、synchronized、显式锁。

**DCL 必须 volatile**
- `new Singleton()` 三步：① 分配内存 ② 初始化 ③ 引用赋值；②③ 可重排为 ①③②，另一线程可能拿到「已赋值未初始化完成」的引用；
- volatile 修饰实例字段禁止该重排（写后 StoreLoad），读线程要么读到 null 要么读到完整对象。

### 13. synchronized 锁升级全过程

> 原题要点：偏向锁（JDK15 为何废弃）、轻量级锁的自旋、重量级锁的开销来源；锁消除与锁粗化。

- 锁状态记录在对象头 Mark Word，升级路径（单向）：无锁 → 偏向锁 → 轻量级锁 → 重量级锁。
- 偏向锁：首次 CAS 记录线程 ID，同线程再入零同步成本；出现第二线程竞争就要撤销偏向（需等全局安全点，STW，代价大）。JDK15 默认禁用（JEP 374）：现代高并发应用偏向撤销成本高于收益、代码维护复杂，整体收益为负。
- 轻量级锁：线程栈上建 Lock Record，CAS 把 Mark Word 换成指向 Lock Record 的指针；失败则自旋（自适应自旋：按历史自旋成功率与持锁时长动态调次数）；自旋超限或三方竞争 → 膨胀为重量级。
- 重量级锁：ObjectMonitor 依赖 OS mutex，用户态/内核态切换 + 线程挂起/唤醒，开销大；但竞争激烈时挂起比长时间自旋更省 CPU。
- 锁消除：JIT 逃逸分析确认对象不逃逸出方法/线程 → 去掉同步（如方法内局部 StringBuffer 的 append）。
- 锁粗化：连续对同一对象反复加解锁（循环内 append）合并为一次大范围加锁，减少同步次数。

### 14. AQS 深度追问链

> 链路：state + CLH 队列 + CAS + park/unpark → 公平/非公平差异的那一处代码 → Condition 与 wait/notify → 用 AQS 实现互斥锁/倒计时器。

**四件套怎么串起来**
- state（volatile int）：同步资源抽象——ReentrantLock 的重入次数、Semaphore 的剩余许可、CountDownLatch 的计数；所有修改走 CAS；
- CLH 变体双向队列：获取失败 addWaiter 入队，Node.waitStatus = SIGNAL 表示「我挂起了，释放时请 unpark 我」；
- 自旋 + park：acquireQueued 循环判断前驱是否 head，是则再 CAS 抢一次；否则 `LockSupport.park(this)` 挂起，等前驱 release 时 unparkSuccessor 唤醒；
- release：CAS 回改 state 成功后唤醒后继。一句话：CAS 定胜负、CLH 保秩序、park/unpark 省自旋开销。

**公平 vs 非公平的差异点**
- 只差在获取入口一处：非公平 `lock()`/tryAcquire 上来先 CAS 抢（插队），失败才入队；公平版先 `hasQueuedPredecessors()`——队列里有先来者就不抢；
- 唤醒侧两者一致（按队列顺序），但被唤醒线程恢复后仍要重新 CAS 竞争，非公平下可能再被新请求插队 → 非公平吞吐高（减少切换）但可能饥饿。

**Condition vs wait/notify**
- 载体：Condition 基于 AQS 条件队列 + Lock；wait/notify 基于 monitor（必须持 synchronized）；
- 能力：一个 Lock 可建多个 Condition（生产/消费各一队列，唤醒精准不惊群）；提供 awaitUninterruptibly、awaitNanos、awaitUntil；
- await 内部：释放全部锁 → 入条件队列 → park → signal 将节点转回同步队列重新抢锁；signal 前必须持锁。

**用 AQS 实现**
- 互斥锁（独占）：`tryAcquire`：CAS state 0→1 并 setExclusiveOwnerThread；state>0 且 owner 为当前线程 → state+1（重入）；否则 false。`tryRelease`：state-1，减到 0 清 owner 并返回 true 触发唤醒后继。
- 倒计时器（共享）：state=N；`tryAcquireShared`：state==0 返回 1 否则 -1（不消费 state）；`tryReleaseShared`：CAS 递减，减到 0 返回 true，共享模式传播唤醒所有等待线程。
### 15. ThreadPoolExecutor 追问链

> 链路：参数与执行流程 → submit 吞异常与取异常 → allowCoreThreadTimeOut → 线程数公式与等待不可测 → 动态线程池 → Executors 的坑。

**参数与执行流程**
- 7 参数：corePoolSize、maximumPoolSize、keepAliveTime+unit、workQueue、threadFactory、handler；
- 流程：线程数 < core → 直接建核心线程（即使有空闲线程）；否则入队；队列满且线程数 < max → 建非核心线程；队列满且线程数 = max → 拒绝策略（AbortPolicy 抛异常 / CallerRunsPolicy 调用者执行形成背压 / DiscardPolicy 静默丢 / DiscardOldestPolicy 丢最老 / 自定义落库重试）；
- 注意是「先入队再扩容到 max」，与直觉相反——想让队列前先扩线程需自定义队列（offer 永远返回 false 的技巧）。

**submit 吞异常与获取**
- submit 把任务包成 FutureTask，run 中 catch 的异常存入 outcome 且不打印；只有调 `future.get()` 才以 ExecutionException 抛出——没人调 get 异常就「消失」；
- 获取方式：① 保留 Future 并 get；② 继承 ThreadPoolExecutor 重写 `afterExecute`（区分 Throwable 直接抛出与 FutureTask outcome 里的异常）；③ execute + 自定义 UncaughtExceptionHandler；生产必须有兜底监控，否则任务静默失败。

**allowCoreThreadTimeOut**
- `allowCoreThreadTimeOut(true)`：核心线程空闲超过 keepAliveTime 也回收（getTask 超时逻辑对核心生效，要求 keepAliveTime > 0）；适合潮汐流量低峰释放资源，代价是高峰重建线程、线程本地缓存丢失。

**线程数公式与等待时间不可测**
- CPU 密集：N+1；IO 密集：`N * (1 + W/S)`（等待/计算时间比），更精确按目标利用率 `N * Ucpu * (1 + W/S)`；
- W 不可测：压测定基线（找吞吐拐点与 P99 平台期）+ 动态线程池：运行时 `setCorePoolSize/setMaximumPoolSize`（JDK 原生支持在线调整，会预启动/回收线程）+ 配置中心下发 + 队列水位/拒绝数监控告警（美团动态线程池方案）。

**Executors 的坑（阿里规约禁用）**
- newFixedThreadPool / newSingleThreadExecutor：LinkedBlockingQueue 无界（Integer.MAX_VALUE）→ 任务堆积 OOM；
- newCachedThreadPool / newScheduledThreadPool：maximumPoolSize = Integer.MAX_VALUE → 线程无限创建 OOM；
- 正确姿势：手动 `new ThreadPoolExecutor(...)`，显式有界队列 + 命名 ThreadFactory + 明确拒绝策略。

### 16. ThreadLocal

> 原题要点：内存泄漏根因（Entry 的 value/key 谁弱谁强）；InheritableThreadLocal 与 TTL 解决什么问题；线程池场景为什么不能用前者。

- 结构：每个 Thread 持有 ThreadLocalMap（线性探测，非链表）；Entry 的 key（ThreadLocal 对象）是**弱引用**，value 是**强引用**（Entry extends WeakReference<ThreadLocal<?>>）。
- 泄漏根因：key 被 GC 后 Entry 变 (null, value)，value 被 Thread → map → Entry 强引用链持有；线程池线程长存 → value 永不释放。get/set 中的探测式清理（expungeStaleEntry）只是兜底不保证及时；**规范：try/finally 里必须 remove()**。
- InheritableThreadLocal：子线程**创建时**拷贝父线程 map（childValue 钩子）——只解决「new 线程继承上下文」；线程池复用线程不重新创建 → 仅首次建线程继承一次，之后任务拿到陈旧值甚至其他请求的值（脏数据）。
- TransmittableThreadLocal（阿里 TTL）：在**任务提交时**捕获快照、**执行时**回放、**执行后**还原（TtlRunnable 包装或 Java Agent 无侵入），正确解决线程池复用下的上下文传递（traceId、租户、MDC）；生产推荐 agent 方式全局生效。

### 17. 并发工具选型与 CompletableFuture

> 原题要点：CountDownLatch/CyclicBarrier/Semaphore/Phaser 选型；CompletableFuture 编排能力与默认线程池的坑。

- CountDownLatch：一次性倒计数，await 等到 0；主任务等 N 个子任务完成；count 不可重置。
- CyclicBarrier：可复用，parties 相互等待到齐一起放行，可带 barrierAction；支持 broken 状态与 reset。
- Semaphore：N 个许可控制并发上限——限流、资源池；acquire/release，支持公平模式与批量 acquire(n)。
- Phaser：多阶段栅栏，动态 register/deregister、arriveAndAwaitAdvance 按阶段推进，替代前两者组合的重型场景。
- CompletableFuture：thenApply（同/异步转换）、thenCompose（扁平化依赖）、thenCombine（合并两路）、allOf/anyOf（汇聚）、exceptionally/handle（异常兜底）、whenComplete（回调）——可编排 DAG 任务流。
- 默认线程池坑：不传 Executor 走 ForkJoinPool.commonPool——并行度 = CPU-1、全 JVM 共享（parallelStream 同用）、守护线程低优先级，IO 任务互相饿死甚至「假死」；**规范：所有 async/thenAsync 显式传自定义线程池并做隔离监控**。
### 18. 伪共享与 LongAdder

> 原题要点：伪共享是什么；@Contended 与缓存行；LongAdder 为什么比 AtomicLong 快。

- CPU 缓存以 cache line（一般 64B）为粒度加载；伪共享：无关联的多个热变量落在同一缓存行，多核各自写 → MESI 协议下缓存行在核间反复失效同步（一致性流量），性能断崖下跌。
- @Contended（JDK8）：字段前后填充隔离，需 `-XX:-RestrictContended` 启用（JDK 内部类默认生效）；手动补空 long 字段不可靠（编译器可能重排/消除）。
- LongAdder：base + Cell[] 分段累加——CAS base 失败（有竞争）则迁移到自己的 Cell 累加；sum() 遍历汇总（非原子快照，弱一致）。写路径无竞争分散、空间换时间；
- AtomicLong 所有线程 CAS 同一个 value：高竞争下自旋失败率 + 缓存行争用双杀。低竞争下 AtomicLong 更省内存也够快——选型仍是权衡。

### 19. 场景题：并行调用框架（≤100 并发 + 总耗时 + 任一失败整体失败）

> 原题要点：并发上限、总耗时统计、fail-fast 三约束的框架设计。

- 主干：隔离的自定义线程池 + Semaphore(100) 控并发 + CompletableFuture 编排：
  1. 每个远程调用包成 `CompletableFuture.supplyAsync(task, bizPool)`，提交前 semaphore.acquire()、finally release()（或直接池大小=100 + 有界队列代替信号量）；
  2. fail-fast：共享 AtomicBoolean failed，任一 exceptionally 置位并对其余任务 cancel(true)/让任务开头检查标志快速返回；用 `allOf` 聚合，get 拿到首个异常，失败要传播（completeExceptionally）；
  3. 总耗时：提交前 nanoTime 起表，allOf 完成后算差值输出指标；
  4. 超时：allOf.get(totalTimeout) + 单任务级 get(timeout) 防个别任务拖死整体；JDK9+ 用 orTimeout/completeOnTimeout。
- 设计要点：Future.cancel 不保证中断 IO，需配合任务内检查点/可中断 IO；线程池与调用方隔离；队列水位与拒绝数监控；「部分成功是否可接受」要和业务确认降级策略。
- 追问延展：批量 fan-out 先 map 成 future 列表再统一 allOf，切勿循环内串行 get。

### 20. 虚拟线程（JDK21）

> 原题要点：虚拟线程 vs 传统线程池；什么时候用虚拟线程反而是错的。

- 本质：JVM 管理的 M:N 调度（ForkJoinPool 载体线程 carrier），虚拟线程阻塞时 unmount 让出 carrier，栈存堆上按需伸缩；百万级并发连接不再要百万 OS 线程；thread-per-request 模型回归——同步写法获得异步吞吐，适合 IO 密集（网关、下游调用等待多）。
- 与线程池区别：平台线程 1:1 内核线程、栈 MB 级、创建/切换贵 → 需池化复用；虚拟线程创建成本 ≈ 一个对象 → **不该池化**，用完即弃，用 Semaphore 限流代替池限流。
- 反而错的场景：
  - CPU 密集：没有阻塞点就没有调度收益，M:N 反增开销；
  - synchronized 长临界区（JDK21 pinning）：块内阻塞会钉住 carrier 无法让出，饿死载体池——改 ReentrantLock（JDK24 JEP 491 已修复 pinning）；
  - native 方法内阻塞同样钉住 carrier；
  - 滥用 ThreadLocal：百万虚拟线程 × 大对象副本 = 内存灾难，改 ScopedValue（预览）。

## 四、JVM

### 21. 运行时内存区域与 OOM

> 原题要点：各区域放什么、可能的 OOM 类型；栈溢出/堆溢出/元空间溢出的制造方法与排查差异。

- 程序计数器：线程私有，当前字节码行号，唯一不会 OOM 的区域；
- 虚拟机栈：栈帧（局部变量表/操作数栈/动态链接/方法出口）→ StackOverflowError（深度超 -Xss）或 OOM（扩展失败）；
- 本地方法栈：native 方法，同理；
- 堆：对象实例与数组，GC 主战场 → `OOM: Java heap space` / `GC overhead limit exceeded`；
- 方法区（JDK8+ 元空间，占本地内存）：类元数据、运行时常量池 → `OOM: Metaspace`；字符串常量池 JDK7 起在堆；
- 直接内存：NIO DirectByteBuffer → `OOM: Direct buffer memory`。

**制造与排查差异**
- 栈溢出：无限递归/超大局部变量制造，`-Xss` 控制；报错自带完整调用栈，直接看栈帧定位递归点；
- 堆溢出：循环 `list.add(new byte[1MB])` 制造，`-Xmx` 控制；dump 后用 MAT 支配树找大对象（常见：无界缓存、全量查询）；
- 元空间溢出：循环生成动态类（CGLIB 代理/反射）+ `-XX:MaxMetaspaceSize`；排查类加载数（jstat -class）与 dump 中 ClassLoader/Class 统计——堆溢出找「对象」，元空间溢出找「类」。

### 22. 对象创建、TLAB、逃逸分析、晋升规则

> 原题要点：TLAB；栈上分配（逃逸分析、标量替换）；大对象直接进老年代的条件；对象进老年代的时机（年龄阈值、动态年龄、担保失败）。

- 分配顺序：TLAB（Eden 内线程私有缓冲，指针碰撞分配几乎无锁）→ TLAB 不够用再 CAS 申请新 TLAB 或走堆上慢速分配；
- 逃逸分析（-XX:+DoEscapeAnalysis 默认开）：不逃逸的对象可 ① 标量替换（拆成基本量放进栈帧，HotSpot 的主流实现）② 栈上分配（经由标量替换达成）③ 锁消除；
- 大对象直入老年代：`-XX:PretenureSizeThreshold`（仅 Serial/ParNew 生效）；G1 中超过 Region 一半的对象是 Humongous，占连续 region，直接老年代语义；
- 进老年代：年龄达 `-XX:MaxTenuringThreshold`（默认 15）；动态年龄判定——Survivor 中某年龄及以下总和超过 Survivor 一半，该年龄及以上直接晋升；Minor GC 后 Survivor 装不下的直接进老年代；老年代空间不足、担保失败（promotion failure）触发 Full GC。

### 23. 垃圾判定与四种引用

> 原题要点：引用计数 vs 可达性分析；四种引用的用途（软引用缓存、弱引用在 ThreadLocal）。

- 引用计数：循环引用无法回收，JVM 不采用；可达性分析：从 GC Roots（栈帧局部变量、静态字段、常量、JNI 引用、活跃线程等）向下搜索，不可达即垃圾（finalize 兜底一次）。
- 强引用：普通引用，宁可 OOM 不回收；
- 软引用（SoftReference）：内存不足才回收 → 内存敏感缓存（如图片缓存），`-XX:SoftRefLRUPolicyMSPerMB` 控制存活时长；要防「快进快出」导致缓存失效风暴；
- 弱引用（WeakReference）：下次 GC 必回收 → ThreadLocalMap 的 key、WeakHashMap、规范化映射；
- 虚引用（PhantomReference）：get 永远为 null，唯一用途是入队通知回收时机做资源清理 → DirectByteBuffer 的 Cleaner（堆外内存回收的关键）。
### 24. GC 算法与收集器追问链

> 链路：三算法与代际适配 → CMS 四阶段/浮动垃圾/CMF/三色标记增量更新 → G1 Region/SATB/Mixed GC/MaxGCPauseMillis 过小的后果 → ZGC 着色指针与演进。

**算法与代际适配**
- 标记-清除：简单但碎片 → 适合 CMS 老年代；标记-复制：无碎片但浪费空间 → 适合存活率低的新生代（Eden:S0:S1 = 8:1:1）；标记-整理：无碎片、移动对象成本高 → 适合老年代。

**CMS**
- 四阶段：初始标记（STW，只标 GC Roots 直连）→ 并发标记（与用户线程并发遍历引用链）→ 重新标记（STW，修正并发期变动）→ 并发清除；
- 浮动垃圾：并发标记/清除期间新产生的垃圾本轮收不掉，留到下次 → 需 `-XX:CMSInitiatingOccupancyFraction` 提前触发留余量；
- concurrent mode failure：并发回收速度 < 分配速度、老年代先满 → 退化 Serial Old 全停顿整理（灾难）；根因常是晋升过快或预留不足；
- 三色标记：白（未访问）/灰（自身已标、子未扫）/黑（全标）；漏标充要条件：黑色新增指向白色 + 灰到白路径全部断开。CMS 用**增量更新**（Incremental Update）：写后屏障记录「黑→白」的新增引用，重新标记时以黑为根重扫——管的是「多出来的引用」。

**G1**
- Region 化堆（1~32MB，E/S/O/H 角色动态分配），按「垃圾比例收益」选 CSet，可预测停顿；
- SATB（Snapshot-At-The-Beginning）：标记开始拍快照，**写前屏障**记录被删除的旧引用，按快照可达性判活——管的是「被删掉的引用」；比增量更新重标更轻，代价是更多浮动垃圾；
- Mixed GC：`-XX:InitiatingHeapOccupancyPercent`（默认 45%）触发并发标记周期，结束后分多次 Mixed GC 回收部分老年代 Region；
- `MaxGCPauseMillis` 设得过小的后果：G1 为达标把每次 CSet 收得过小 → 回收速度跟不上分配速度 → GC 频率暴涨、晋升压力增大 → to-space 耗尽/晋升失败 → Full GC（JDK10 前单线程 Serial），停顿不降反升。经验 100~200ms 起步，配合 GC 日志与 IHOP 调整。

**ZGC**
- 着色指针：把 GC 元数据存在 64 位指针的高位 bit（Marked0/Marked1/Remapped/Finalizable），视图切换零额外元数据成本；
- 读屏障：load 时检查指针颜色，发现坏颜色则「自愈」（修正引用并更新指针），保证并发移动对象时应用始终读到正确地址——ZGC 能并发整理且停顿亚毫秒的核心；停顿只做根扫描类工作，与堆大小基本无关（TB 级堆同样停顿）；
- 演进：JDK11 实验性（<10ms）→ JDK15 生产可用 → JDK17 亚毫秒 → JDK21 分代 ZGC（JEP 439，`-XX:+ZGenerational`，吞吐大涨）→ JDK23 起分代成为默认。

### 25. 类加载与双亲委派

> 原题要点：双亲委派模型；破坏场景（SPI、Tomcat 隔离、热部署）；自定义类加载器何时需要写。

- 过程：加载 → 验证 → 准备（静态变量赋零值，static final 常量在此就位）→ 解析（部分延后）→ 初始化（clinit；new、读写静态字段、反射、初始化子类连带父类、主类等主动引用触发）。
- 双亲委派：App → Platform(Ext) → Bootstrap，先委派后加载。目的：核心类唯一性（防伪造 java.lang.String）+ 稳定层次与安全。
- 破坏场景：
  - SPI（JDBC）：Bootstrap/Platform 加载的 DriverManager 要加载 classpath 下厂商驱动 → 线程上下文类加载器（TCCL）做「逆向委派」；
  - Tomcat：WebappClassLoader 先自己加载 webapp 内的类（隔离各应用依赖冲突），失败再委派——顺序颠倒的隔离型破坏；common 类库共享；
  - 热部署/OSGi：自定义加载器加载新版字节码，旧类连同实例一起被 GC 完成替换。
- 自定义加载器时机：字节码解密加载、来源非文件（网络/DB/内存）、模块隔离、热替换、字节码增强。常规重写 `findClass`（保留 loadClass 的委派逻辑），做热部署才需要重写 loadClass。

### 26. 排查实操题（口述过程）

> 原题要点：CPU 100% 定位线程与代码行；频繁 Full GC 排查思路与根因清单；拿不到 dump 的补救；Arthas 常用命令。

**CPU 100%**
- `top` 定位进程 → `top -Hp <pid>` 定位线程 → `printf "%x\n" tid` 转十六进制 → `jstack <pid>` 搜 `nid=0x…` 看线程栈定位代码行；
- 注意区分业务线程死循环与 GC 线程飙高（另查 GC）；jstack 多抓几次防偶发；持续观测推荐 async-profiler 火焰图（比快照准）。

**频繁 Full GC 排查**
- `jstat -gcutil <pid> 1000` 或 GC 日志：看回收后老年代是否下降——降不下去 = 泄漏，降得下去 = 分配过快或空间不足；
- 根因清单：① 泄漏（静态集合无淘汰、ThreadLocal 不 remove、监听器未注销、连接未关）② 大对象/全量查询 ③ 堆或 Survivor 偏小过早晋升 ④ 元空间/直接内存不足连带 ⑤ 代码显式 System.gc()（RMI 定时触发）⑥ CMS promotion failure / G1 to-space 耗尽；
- dump 后 MAT：支配树 + Leak Suspects + 两时间点 dump 对比增量。

**拿不到 dump 的补救**
- 在线抓取：`jmap -dump:live,format=b,file=/tmp/heap.hprof <pid>`（live 会先 Full GC，大堆低峰执行）；`jcmd <pid> GC.heap_dump /path`；容器内 kubectl exec + 挂载持久卷；
- 低开销替代：JFR 飞行记录（JDK11+ 免费持续低耗采集）；
- 长效：启动参数 `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=...`，容器再加 `-XX:+ExitOnOutOfMemoryError` 让 K8s 重启自愈。

**Arthas**
- `thread -b`：一键找死锁/最深阻塞源头（比人工读 jstack 快）；
- `watch 类 方法 '{params,returnObj,throwExp}'`：线上观测出入参与异常（支持条件表达式，抓偶发问题）；
- `trace 类 方法`：方法内部调用链逐层耗时（定位慢在哪一跳，`-n 1` 控制次数）；
- `ognl '@类@静态字段'`：运行时读写静态变量、调静态方法（手动清缓存、看开关）；
- 补充：jad 反编译验证线上版本、sc/sm 查类、profiler 火焰图、dashboard 总览。

### 27. JVM 调优与容器环境

> 原题要点：调过哪些参数、如何量化效果；-Xss、MetaspaceSize、GC 日志参数；K8s 内存 limit 与 JVM 感知（MaxRAMPercentage）。

- 方法论：先测量后调参——GC 日志 + Prometheus 指标 + 压测对比（吞吐、P99、Full GC 频次/时长、回收后老年代水位）；没有量化不叫调优。
- 常用参数：
  - `-Xss256k~1m`：海量线程场景调小省内存；深递归应用调大；
  - `-XX:MetaspaceSize`：元空间触发 Full GC 的初始水位（不是初始容量），类多的应用调大避免启动后反复 Full GC；`-XX:MaxMetaspaceSize` 防泄漏打爆机器内存；
  - GC 日志：JDK11+ `-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=50m`（JDK8 用 PrintGCDetails 系列）；
  - 其它：`-XX:+AlwaysPreTouch`（启动时触碰内存页，避免运行期缺页抖动）、`-XX:ParallelGCThreads`/`-XX:ConcGCThreads`。
- 容器（K8s）注意：
  - 老版本 JVM（< 8u191）不感知 cgroup，按宿主机 CPU/内存分配 → 堆外内存把容器顶爆被 OOMKilled（kill -9，不是 Java OOM，无 dump）；
  - 8u191+/10+ `UseContainerSupport` 默认开启；仍建议显式 `-XX:MaxRAMPercentage=50~75`（给堆外留量：元空间 + 线程数×栈 + DirectMemory + JIT 代码缓存 + GC 自身结构）；
  - CPU limit 影响 availableProcessors → GC/JIT 线程数，必要时 `-XX:ActiveProcessorCount` 显式指定；request=limit 避免 CFS throttle 造成长尾抖动。
## 五、编码题

### 28. 三线程交替打印 A/B/C（两种实现）

> 原题要点：synchronized/wait 与 Semaphore（或 Lock+Condition）至少两种实现。

**方案一：synchronized + wait/notifyAll（共享 state 轮转）**

```java
public class PrintAbcSync {
    private final Object lock = new Object();
    private int state = 0; // 0=A, 1=B, 2=C

    public static void main(String[] args) {
        PrintAbcSync p = new PrintAbcSync();
        new Thread(() -> p.print(0, "A", 1)).start();
        new Thread(() -> p.print(1, "B", 2)).start();
        new Thread(() -> p.print(2, "C", 0)).start();
    }

    private void print(int waitState, String content, int nextState) {
        for (int i = 0; i < 10; i++) {
            synchronized (lock) {
                while (state != waitState) {   // while 而非 if：防虚假唤醒
                    try { lock.wait(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                System.out.print(content);
                state = nextState;
                lock.notifyAll();
            }
        }
    }
}
```

**方案二：Semaphore 许可接力（精准唤醒，无惊群）**

```java
import java.util.concurrent.Semaphore;

public class PrintAbcSemaphore {
    private final Semaphore sa = new Semaphore(1);
    private final Semaphore sb = new Semaphore(0);
    private final Semaphore sc = new Semaphore(0);

    public static void main(String[] args) {
        PrintAbcSemaphore p = new PrintAbcSemaphore();
        new Thread(() -> p.print(p.sa, p.sb, "A")).start();
        new Thread(() -> p.print(p.sb, p.sc, "B")).start();
        new Thread(() -> p.print(p.sc, p.sa, "C")).start();
    }

    private void print(Semaphore self, Semaphore next, String content) {
        for (int i = 0; i < 10; i++) {
            try {
                self.acquire();      // 等自己的许可
                System.out.print(content);
                next.release();      // 把许可交给下一个线程
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

并发设计说明：
- 条件判断必须 `while` 重查（虚假唤醒 + 条件可能被其它线程抢先消费），用 `if` 是经典错误；
- notifyAll 有惊群（所有等待线程醒来再竞争），三 Condition 的 Lock 版可精准唤醒；Semaphore 版语义就是「许可接力」，最简洁；
- state 在锁内读写无需 volatile（锁保证 happens-before）；Semaphore 版同步由 AQS 保证；
- 延展：LockSupport.park/unpark 版不需要锁与条件变量（permit 语义天然一对一唤醒）；生产者-消费者可按同思路切换模板。

### 29. ReentrantLock + Condition 有界阻塞队列

> 原题要点：实现 put/take 的阻塞语义；分析与 ArrayBlockingQueue 的差异。

```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBlockingQueue<E> {
    private final Object[] items;
    private int takeIdx, putIdx, count;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition(); // 生产者等
    private final Condition notEmpty = lock.newCondition(); // 消费者等

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity > 0 required");
        this.items = new Object[capacity];
    }

    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        lock.lockInterruptibly();
        try {
            while (count == items.length) notFull.await();  // 满：等"非满"
            items[putIdx] = e;
            if (++putIdx == items.length) putIdx = 0;       // 环形推进
            count++;
            notEmpty.signal();                              // 只唤醒消费者
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public E take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) notEmpty.await();            // 空：等"非空"
            E e = (E) items[takeIdx];
            items[takeIdx] = null;                          // 释放槽位引用防泄漏
            if (++takeIdx == items.length) takeIdx = 0;
            count--;
            notFull.signal();                               // 只唤醒生产者
            return e;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try { return count; } finally { lock.unlock(); }
    }

    public static void main(String[] args) {
        BoundedBlockingQueue<Integer> q = new BoundedBlockingQueue<>(2);
        new Thread(() -> { try { for (int i = 1; i <= 4; i++) q.put(i); }
                           catch (InterruptedException ignored) {} }).start();
        new Thread(() -> { try { for (int i = 1; i <= 4; i++) System.out.println("take " + q.take()); }
                           catch (InterruptedException ignored) {} }).start();
    }
}
```

与 ArrayBlockingQueue 的差异分析：
- 同构点：ArrayBlockingQueue 正是环形数组 + 单把 ReentrantLock + notEmpty/notFull 两个 Condition，本实现即其最小骨架；
- 差异：
  - ArrayBlockingQueue 提供 offer/poll 非阻塞与超时版、remove(Object)（锁内遍历删除）、drainTo 批量搬运、弱一致迭代器（ITRS，JDK9+ 优化）；
  - 锁可配公平/非公平（构造参数），公平防饥饿但降吞吐；
  - 对比 LinkedBlockingQueue：后者 put/take 两把锁分离 + AtomicInteger count，读写不互斥吞吐更高，但级联唤醒（signalNotEmpty）与 size 语义更复杂；
- 并发设计说明：await 必须在 while 中重查条件；notFull/notEmpty 分离使 signal 精准（唤醒对侧等待者）；lockInterruptibly 保证阻塞获取可被取消；索引与 count 全部锁内维护，数组槽位无并发访问。
### 30. 单机令牌桶 RateLimiter

> 原题要点：实现支持预分配与突发的令牌桶；追问 CAS 自旋在其中的应用。

```java
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 单机令牌桶（Guava RateLimiter 简化版）：
 * - 惰性补牌：不做后台定时器，每次请求按流逝时间一次性折算令牌
 * - 预分配/突发：初始满桶，桶容量即最大突发量
 * - 欠账模式：令牌不足时顺延"最早可消费时刻"，请求在锁外等待
 */
public class TokenBucketRateLimiter {
    private final double capacity;        // 桶容量 = 最大突发令牌数
    private final double tokensPerSecond; // 注入速率

    private double storedTokens;          // 桶内令牌
    private long nextFreeNanos;           // 最早可消费时刻（> now 表示有欠账）

    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketRateLimiter(double capacity, double tokensPerSecond) {
        if (capacity <= 0 || tokensPerSecond <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        this.tokensPerSecond = tokensPerSecond;
        this.storedTokens = capacity;     // 预分配：初始满桶，允许瞬时突发
        this.nextFreeNanos = System.nanoTime();
    }

    public void acquire() { acquire(1); }

    /** 阻塞获取 permits 个令牌：计算在锁内，等待在锁外 */
    public void acquire(int permits) {
        long waitNanos = reserve(permits);
        if (waitNanos > 0) LockSupport.parkNanos(waitNanos);
    }

    /** 非阻塞尝试获取 */
    public boolean tryAcquire(int permits) {
        lock.lock();
        try {
            long now = System.nanoTime();
            if (now < nextFreeNanos) return false;   // 有欠账，直接失败
            refill(now);
            if (storedTokens >= permits) {
                storedTokens -= permits;
                nextFreeNanos = now;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private long reserve(int permits) {
        lock.lock();
        try {
            long now = System.nanoTime();
            long waitNanos = Math.max(0, nextFreeNanos - now); // 上一波欠账需补的时间
            refill(now);
            double need = Math.max(0, permits - storedTokens); // 不足部分转欠账
            storedTokens = Math.max(0, storedTokens - permits);
            nextFreeNanos = now + waitNanos + (long) (need / tokensPerSecond * 1e9);
            return waitNanos;
        } finally {
            lock.unlock();
        }
    }

    /** 惰性补牌：自 nextFreeNanos 以来的时间折算成令牌，上限 capacity */
    private void refill(long now) {
        if (now > nextFreeNanos) {
            double delta = (now - nextFreeNanos) / 1e9 * tokensPerSecond;
            storedTokens = Math.min(capacity, storedTokens + delta);
        }
    }

    public static void main(String[] args) {
        // 5 令牌/秒、容量 5：先突发 5 个，之后每 200ms 放行一个
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 5);
        long start = System.nanoTime();
        for (int i = 1; i <= 8; i++) {
            limiter.acquire();
            System.out.printf("第 %d 次通过，距开始 %d ms%n",
                    i, (System.nanoTime() - start) / 1_000_000);
        }
    }
}
```

**CAS 自旋在其中的应用（追问）**
- 可把 `storedTokens + nextFreeNanos` 打包进一个 AtomicLong（如低位存令牌定点数、高位存纳秒时间戳），更新走 CAS 自旋循环：

```java
// 无锁版核心循环（示意）
long s;
do {
    s = state.get();
    // 1. 解包 tokens/lastNanos，按当前时间惰性补牌（纯计算）
    // 2. 扣减 permits、推算 nextFreeNanos（纯计算）
} while (!state.compareAndSet(s, pack(newTokens, newNanos))); // 失败重读重算
```

- 权衡：多字段原子更新必须打包成一个字（单字段 CAS 不够），冲突即重算——低竞争下 CAS 吞吐高、无线程挂起；高竞争下自旋空转烧 CPU，ReentrantLock（自适应自旋 + park 阻塞）更稳；Guava 用 synchronized、部分开源限流用 CAS+自旋，都是这个权衡的落地。

并发设计说明：
- 不用定时线程发牌：定时器有漂移与唤醒开销；惰性补牌按时间戳折算，空闲期令牌自然累积（突发能力），精度由 nanoTime 保证；
- 「计算在锁内、等待在锁外」是 Guava 核心技巧：临界区只做算术，等待不占锁，并发请求按 nextFreeNanos 排队获得平滑限流；
- 欠账模型（nextFreeNanos > now）天然支持「预支未来令牌」的 BURSTY 语义，也容易扩展 WARMUP（按 nextFreeNanos 距离梯度降速）。