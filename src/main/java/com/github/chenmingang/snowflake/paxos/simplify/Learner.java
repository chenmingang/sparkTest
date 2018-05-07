package com.github.chenmingang.snowflake.paxos.simplify;

import com.github.chenmingang.snowflake.paxos.Meta;

/**
 * 学习者
 */
public class Learner {

    public boolean learn(String proposerName, long value) {
        Meta.INSTANCE.putMeta(proposerName, value);
        return true;
    }
}
