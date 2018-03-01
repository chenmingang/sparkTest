package com.github.chenmingang.snowflake.paxos;

import java.util.HashMap;
import java.util.Map;

/**
 * 各机器id的元数据
 */
public class Meta {

    private Meta() {
    }

    private String self;

    public static final Meta INSTANCE = new Meta();

    private Map<String, String> meta = new HashMap<>();

    public void putMeta(String key, String value) {
        meta.put(key, value);
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }
}
