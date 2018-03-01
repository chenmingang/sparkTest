package com.github.chenmingang.parseq;

import com.linkedin.parseq.Engine;
import com.linkedin.parseq.Task;
import com.linkedin.parseq.promise.Promises;
import com.linkedin.parseq.promise.SettablePromise;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

public class EngineAgent {

    private Engine engine;
    private ThreadPoolExecutor executors;
    private ScheduledExecutorService scheduler;

    public EngineAgent(Engine engine, ThreadPoolExecutor executors,ScheduledExecutorService scheduler) {
        this.engine = engine;
        this.executors = executors;
        this.scheduler = scheduler;
    }

    public <T, R> SettablePromise<R> async(T param, Function<T, R> function) {
        final SettablePromise<R> promise = Promises.settable();
        getExecutors().execute(() -> {
            try {
                promise.done(function.apply(param));
            } catch (Exception e) {
                promise.fail(e);
            }
        });
        return promise;
    }

    public <T, R> Task<R> task(T param, Function<T, R> function) {
        return Task.async(() -> async(param, function));
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
