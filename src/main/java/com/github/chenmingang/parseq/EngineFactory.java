package com.github.chenmingang.parseq;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.linkedin.parseq.EngineBuilder;
import com.linkedin.parseq.batching.BatchingSupport;

import java.util.concurrent.*;

public class EngineFactory {

    private static EngineFactory INSTANCE = new EngineFactory();
    private final EngineAgent defaultEngine;

    private EngineFactory() {
        int numCores = Runtime.getRuntime().availableProcessors();
        defaultEngine = getEngine(numCores + 1, 1, numCores + 1);
    }

    public static EngineAgent defaultEngine() {
        return INSTANCE.defaultEngine;
    }

    public static EngineAgent getEngine(int poolSize, int scheduleSize, int queueNum) {
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(scheduleSize);
        ThreadFactory threadFactory = new ThreadFactoryBuilder().build();
        ThreadPoolExecutor executors = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueNum), threadFactory,
                (r, executor) -> {
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        final EngineBuilder builder = new EngineBuilder().setTaskExecutor(executors).setTimerScheduler(scheduler);
        final BatchingSupport batchingSupport = new BatchingSupport();

        builder.setPlanDeactivationListener(batchingSupport);
        return new EngineAgent(builder.build(), executors, scheduler);
    }

}
