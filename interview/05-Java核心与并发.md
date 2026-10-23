# 高级 Java 工程师面试题 —— Java 核心、并发与 JVM

> 适用岗位：高级 Java 开发工程师（3~8 年）。考察方式建议：概念题快速过，重点在「追问链」和场景题上区分深度。

---

## 一、Java 语言基础（10 分钟，快速筛掉基础不牢的）

1. `==` 与 `equals` 区别，`hashCode` 与 `equals` 的约定是什么？重写 `equals` 不重写 `hashCode` 在 HashMap 里会出什么问题？
2. String 为什么设计成不可变？`String`、`StringBuilder`、`StringBuffer` 的选型；字符串常量池与 `intern()` 的行为。
3. 深拷贝与浅拷贝的实现方式？Cloneable 为什么被认为是缺陷设计，替代方案是什么？
4. 异常体系：Checked vs Unchecked 的取舍；`finally` 一定执行吗（什么情况下不执行）？`try-with-resources` 的字节码原理。
5. 接口 default 方法与抽象类的区别；JDK8~21 你最常用的几个语言特性（Stream、Optional、Record、虚拟线程……），分别解决什么问题？
6. 反射的性能开销在哪？怎么优化（MethodHandle、缓存 Method、字段AccessibleObject.setAccessible 的模块化限制）？动态代理 JDK vs CGLIB 的区别与限制。

## 二、集合框架（10 分钟）

7. `HashMap` 源码级追问链：
   - put 一次的完整流程（hash 扰动 → 定位桶 → 冲突处理 → 树化判断 → 扩容）？
   - 为什么扰动函数要高 16 位异或？为什么容量是 2 的幂？
   - 树化为什么阈值是 8，退化是 6？树化的前提条件？
   - JDK7 头插法的并发死循环是怎么形成的？JDK8 怎么修复的（还有问题吗）？
8. `ConcurrentHashMap`：JDK8 相比 JDK7 的分段锁改了什么？size() 怎么实现的？能否存 null，为什么？多线程 put 扩容期间 get 会怎样？
9. `ArrayList` 扩容机制（1.5 倍）；`subList` 的坑；`Arrays.asList` 的坑；`CopyOnWriteArrayList` 的适用场景与缺点。
10. Fail-Fast 与 Fail-Safe 机制：modCount 的作用；哪些集合是安全的？
11. 场景题：需要按访问顺序淘汰的 Map 你用什么？需要排序 + 并发呢？手写一个 LRU 的思路（LinkedHashMap accessOrder / 双向链表 + HashMap）。

## 三、并发编程（20 分钟，核心区分度）

12. Java 内存模型（JMM）：
    - happens-before 规则列举与推导，volatile 的两个语义（可见性 + 禁止重排序），内存屏障怎么插入的？
    - `i++` 为什么线程不安全？原子性、可见性、有序性分别由什么保证？
    - double-checked locking 单例为什么必须加 volatile？
13. `synchronized` 锁升级全过程：偏向锁（JDK15 为何废弃）、轻量级锁的自旋、重量级锁的开销来源。锁消除、锁粗化是什么？
14. AQS 深度追问链：
    - state + CLH 队列 + CAS + park/unpark 四件套怎么串起来？
    - 公平与非公平的差异就在一处代码，是哪一处？
    - `Condition` 的 await/signal 与 `Object.wait/notify` 的区别？
    - 用 AQS 实现一个简单的互斥锁 / 倒计时器，说思路。
15. `ThreadPoolExecutor` 追问链（必考）：
    - 参数、执行流程、拒绝策略（略）；
    - 为什么 JDK 后续提供了 `submit` 但异常会被"吞掉"？怎么拿到任务里的异常？
    - core 线程允许超时回收吗（allowCoreThreadTimeOut）？
    - 线程池的核心线程数怎么定？CPU 密集 / IO 密集公式，然后追问：如果 IO 等待时间不可测怎么办（动态线程池/压测）？
    - `Executors` 各工厂方法的坑（无界队列、无限线程）。
16. ThreadLocal：
    - 内存泄漏的根因（Entry 的 value 是弱引用还是强引用？key 呢？）；
    - `InheritableThreadLocal` 与 `TransmittableThreadLocal` 解决什么问题，线程池场景为什么不能用前者？
17. 并发工具选型：`CountDownLatch` vs `CyclicBarrier` vs `Semaphore` vs `Phaser`；`CompletableFuture` 的编排能力、默认线程池的坑（ForkJoinPool.commonPool）。
18. 伪共享是什么？`@Contended`、缓存行、`LongAdder` 为什么在高并发计数上比 `AtomicLong` 快？
19. 场景题：实现一个"最多同时 100 个请求、总耗时统计、任一失败整体失败"的并行调用框架，怎么设计？
20. 虚拟线程（JDK21）了解吗？与传统线程池的区别？什么时候用虚拟线程反而是错的（CPU 密集、synchronized pinning 问题）？

## 四、JVM（15 分钟）

21. 运行时内存区域：每块区域放什么、可能的 OOM 类型？栈溢出 vs 堆溢出 vs 元空间溢出的制造方法和排查差异。
22. 对象创建与内存分配：TLAB、栈上分配（逃逸分析、标量替换）、大对象直接进老年代的条件？对象什么时候进老年代（年龄阈值、动态年龄判定、担保失败）？
23. 垃圾判定：引用计数 vs 可达性分析；四种引用类型的用途（软引用在缓存、弱引用在 ThreadLocal 中的应用）。
24. GC 算法与收集器追问链：
    - 标记-清除/复制/整理的适用代际及原因；
    - CMS 的四个阶段、两个并发问题（浮动垃圾 / concurrent mode failure）、三色标记与增量更新；
    - G1 的 Region、SATB、Mixed GC 触发条件、`MaxGCPauseMillis` 设得过小会怎样？
    - ZGC 的着色指针、读屏障、为什么能亚毫秒停顿？JDK11 → 17 → 21 的演进（并发标记整理、分代 ZGC）。
25. 类加载：双亲委派模型、破坏双亲委派的场景（SPI、Tomcat 隔离、热部署）；自定义类加载器什么时候需要写？
26. 排查实操题（口述过程）：
    - CPU 飙到 100% 怎么定位到具体线程和代码行？（top -H → printf 转十六进制 → jstack）
    - 频繁 Full GC 的排查思路与常见根因清单；
    - OOM 时 dump 文件拿不到（启动参数没配）怎么办？线上安全的 dump 方式；
    - Arthas 的常用命令：thread -b、watch、trace、ognl 各解决什么问题。
27. JVM 调优：你调过哪些参数、为什么调、效果怎么量化？`-Xss`、`-XX:MetaspaceSize`、GC 日志参数，容器环境（K8s 内存 limit 与 JVM 感知，`MaxRAMPercentage`）要注意什么？

## 五、编码题（可选 20 分钟，二选一）

28. 手写：三个线程交替打印 A/B/C（或生产者-消费者），至少给出两种实现（synchronized/wait、Semaphore/Lock+Condition）。
29. 手写：基于 `ReentrantLock` + `Condition` 实现一个有界阻塞队列，并分析与 `ArrayBlockingQueue` 的差异。
30. 手写：单机 `RateLimiter`（令牌桶），支持预分配和突发；追问：CAS 自旋在里面的应用。
