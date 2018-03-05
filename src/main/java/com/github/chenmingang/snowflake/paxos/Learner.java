package com.github.chenmingang.snowflake.paxos;

/**
 * 学习者
 */
public class Learner {
    private Learner(){}
    private static Learner INSTANCE = new Learner();

    public boolean s;
}
