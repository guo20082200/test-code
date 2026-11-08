package com.goaerion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DfeTest {

    // 所有线程共享同一个 SimpleDateFormat 实例（错误用法）
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws InterruptedException {
        // 使用较多线程以增加并发冲突概率
        ExecutorService pool = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 50; i++) {
            pool.execute(() -> {
                while (true) {
                    try {
                        // 不断用随机时间格式化，持续冲击共享的 Calendar 内部状态
                        Date randomDate = new Date((long) (Math.random() * 2000000000000L));
                        SDF.format(randomDate);
                        //System.out.println(".................");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.err.println("成功复现：ArrayIndexOutOfBoundsException");
                        e.printStackTrace();
                        System.exit(1); // 抓到就退出
                    } catch (Exception e) {
                        // 也可能出现 NumberFormatException 等其他并发异常，一并忽略
                        e.printStackTrace();
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
    }

}
