package com.github.chenmingang.rxjava;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class SimpleExample {

    public static void main(String[] args) {
        Flowable.just("Hello world").subscribe(System.out::println);

        Flowable.range(1, 10)
                .observeOn(Schedulers.computation())
                .map(v -> v * v)
                .blockingSubscribe(System.out::println);

    }
}
