package com.github.chenmingang.snowflake.paxos;

import org.junit.After;
import org.junit.Test;

public class Test1 {

    @After
    public void after() throws InterruptedException {
        Thread.sleep(10000000L);
    }

    @Test
    public void test1() {
        new Acceptor("1111");
//        new Proposer("1111");
    }
    @Test
    public void test2() {
        new Acceptor("1112");
//        new Proposer("1112");
    }
    @Test
    public void test3() {
        new Acceptor("1113");
//        new Proposer("1113");
    }
    @Test
    public void test4() {
//        new Acceptor("1111");
        new Proposer("1111");
    }
    @Test
    public void test5() {
//        new Acceptor("1111");
        new Proposer("1112");
    }
    @Test
    public void test6() {
//        new Acceptor("1111");
        new Proposer("1113");
    }
}
