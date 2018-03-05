package com.github.chenmingang.snowflake.paxos;

public class Test {

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
        test5();
        test6();
    }

    public static void test1() {
        new Acceptor("1111");
//        new Proposer("1111");
    }

    public static void test2() {
        new Acceptor("1112");
//        new Proposer("1112");
    }

    public static void test3() {
        new Acceptor("1113");
//        new Proposer("1113");
    }
    public static void test4() {
//        new Acceptor("1111");
        new Proposer("1111").proposal();
    }
    public static void test5() {
//        new Acceptor("1111");
        new Proposer("1112").proposal();
    }
    public static void test6() {
//        new Acceptor("1111");
        new Proposer("1113").proposal();
    }
}
