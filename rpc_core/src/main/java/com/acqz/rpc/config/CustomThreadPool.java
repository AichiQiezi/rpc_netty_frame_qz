package com.acqz.rpc.config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements Executor {

    // 线程池名称
    private final String name;

    // 线程池大小
    private final int poolSize;

    // 最大线程数
    private final int maxPoolSize;

    // 线程空闲时间
    private final int keepAliveTime;

    // 等待队列大小
    private final int queueSize;

    // 线程池
    private final ThreadPoolExecutor executor;

    // 线程编号生成器
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public CustomThreadPool(String name, int poolSize, int maxPoolSize, int keepAliveTime, int queueSize) {
        this.name = name;
        this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueSize = queueSize;

        // 创建线程池
        executor = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize), new CustomThreadFactory(), new CustomRejectedExecutionHandler());
    }

    /**
     * 提交任务到线程池
     *
     * @param command 待执行的任务
     */
    @Override
    public void execute(Runnable command) {
        executor.submit(command);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 获取线程池名称
     *
     * @return 线程池名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取线程池大小
     *
     * @return 线程池大小
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * 获取最大线程数
     *
     * @return 最大线程数
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * 获取线程空闲时间
     *
     * @return 线程空闲时间
     */
    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    /**
     * 获取等待队列大小
     *
     * @return 等待队列大小
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * 自定义线程工厂类，用于生成线程对象
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        CustomThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, "pool-" + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * 自定义拒绝策略，用于处理队列已
     * 满时的情况
     */
    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 输出警告信息
            System.err.println("Task " + r.toString() + " rejected from " + executor.toString());
        }
    }
}
