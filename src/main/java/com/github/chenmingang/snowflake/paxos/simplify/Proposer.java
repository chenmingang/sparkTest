package com.github.chenmingang.snowflake.paxos.simplify;


/**
 * 提案者
 */
public class Proposer {
    private volatile long lastProposerVersion = -1L;
    private String name;

    public Proposer(String name) {
        this.name = name;
    }

    public long prepare() {
        lastProposerVersion++;
        return lastProposerVersion;
    }

    public long proposer() {
        return new Double(Math.random() * 10).longValue();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
