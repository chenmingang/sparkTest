package com.github.chenmingang.snowflake.paxos.simplify;

import com.github.chenmingang.snowflake.paxos.Meta;

/**
 * 批准者
 */
public class Acceptor {
    Meta meta = Meta.INSTANCE;

    private volatile long lastProposerVersion = -1L;

    public boolean handlePrepare(long version) {
        if (version > lastProposerVersion) {
            return true;
        }
        return false;
    }

    public boolean handleProposer(String proposerName, long value) {
        if (meta.exist(value) && !meta.exist(proposerName, value)) {
            return false;
        } else {
            return true;
        }
    }


}
