package com.github.chenmingang.parseq;

import com.linkedin.parseq.Engine;
import com.linkedin.parseq.Task;
import com.linkedin.parseq.promise.Promises;
import com.linkedin.parseq.promise.SettablePromise;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public class EngineAgent {

    private Engine engine;
    private ThreadPoolExecutor executors;
    private ScheduledExecutorService scheduler;

    public EngineAgent(Engine engine, ThreadPoolExecutor executors,ScheduledExecutorService scheduler) {
        this.engine = engine;
        this.executors = executors;
        this.scheduler = scheduler;
    }


    public <T> SettablePromise<T> async(Supplier<T> supplier) {
        final SettablePromise<T> promise = Promises.settable();
        getExecutors().execute(() -> {
            try {
                promise.done(supplier.get());
            } catch (Exception e) {
                promise.fail(e);
            }
        });
        return promise;
    }

    public <T> Task<T> task(Supplier<T> supplier) {
        return Task.async(() -> async(supplier));
    }

    public void run(final Task<?> task) {
        engine.run(task);
    }

    public void shutdown() {
        engine.shutdown();
        executors.shutdown();
    }

    public ThreadPoolExecutor getExecutors() {
        return executors;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
