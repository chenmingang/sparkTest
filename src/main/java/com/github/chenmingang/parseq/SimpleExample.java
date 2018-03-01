package com.github.chenmingang.parseq;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.linkedin.parseq.Engine;
import com.linkedin.parseq.EngineBuilder;
import com.linkedin.parseq.Task;
import com.linkedin.parseq.Tuple3Task;
import com.linkedin.parseq.batching.BatchingSupport;
import com.linkedin.parseq.promise.Promises;
import com.linkedin.parseq.promise.SettablePromise;

import java.util.concurrent.*;

public class SimpleExample {

    private volatile ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    final int numCores = Runtime.getRuntime().availableProcessors();

    ThreadFactory threadFactory = new ThreadFactoryBuilder().build();
    private final ThreadPoolExecutor executors = new ThreadPoolExecutor(numCores + 1, numCores + 1,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(30), threadFactory,
            (r, executor) -> {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

    final EngineBuilder builder = new EngineBuilder().setTaskExecutor(executors).setTimerScheduler(scheduler);
    private final BatchingSupport batchingSupport = new BatchingSupport();

    final Engine engine;


    public SimpleExample() {
        builder.setPlanDeactivationListener(batchingSupport);
        engine = builder.build();
    }

    public SettablePromise<Integer> asyncCall(String s) throws Exception {
        final SettablePromise<Integer> promise = Promises.settable();
        executors.execute(() -> {
            try {
                promise.done(call(s));
            } catch (Exception e) {
                promise.fail(e);
            }
        });
        return promise;
    }

    public Integer call(String s) throws Exception {
        Thread.sleep(1000);
        System.out.println(s + ":done");
        return s.length();
    }

    private void start() {

        try {
            doRunExample(engine);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            engine.shutdown();
            executors.shutdown();
            scheduler.shutdown();
        }
    }

    private void doRunExample(Engine engine) throws InterruptedException {

        Task<Integer> async1 = Task.async("name1", () -> asyncCall("11"));
        Task<Integer> async2 = Task.async("name2", () -> asyncCall("2222"));
        Task<Integer> async3 = Task.async("name3", () -> asyncCall("333"));

        Task<Integer> task1 = Task.callable(() -> call("44444"));
        Task<Integer> task2 = Task.callable(() -> call("555"));
        Task<Integer> task3 = Task.callable(() -> call("5"));


        Tuple3Task<Integer, Integer, Integer> par = Task.par(task1, task2, task3);
        Tuple3Task<Integer, Integer, Integer> asyncPar = Task.par(async1, async2, async3);


        Task<Integer> map = par.map((a, b, c) -> {
            return a + b + c;
        });
        Task<String> asyncMap = asyncPar.map((a, b, c) -> {
            System.out.println("map:done");
            return a + b + c + "";
        });
        Task<String> andThen = asyncMap.andThen(String::toUpperCase);

        long l1 = System.currentTimeMillis();
        engine.run(andThen);
        andThen.await();
        long l2 = System.currentTimeMillis();

        long l3 = System.currentTimeMillis();
        engine.run(map);
        map.await();
        long l4 = System.currentTimeMillis();

        System.out.println("Resulting value: " + andThen.get() + ";time:" + (l2 - l1));
        System.out.println("Resulting value: " + map.get() + ";time:" + (l4 - l3));
        System.out.println("time:" + (l4 - l1));

    }

    public static void main(String[] args) {
        new SimpleExample().start();
    }


}
