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

    private Map<String, Long> meta = new HashMap<>();

    public void putMeta(String key, long value) {
        System.out.println("设置元数据 " + key + "#" + value);
        meta.put(key, value);
        for (Map.Entry<String, Long> kv : meta.entrySet()) {
            System.out.println("meta: " + kv.getKey() + "#" + kv.getValue());
        }
    }

    public boolean exist(String key) {
        return meta.containsKey(key);
    }

    public boolean exist(String key, Long value) {
        return exist(key) && meta.get(key).equals(value);
    }

    public boolean exist(Long value) {
        for (Map.Entry<String, Long> kv : meta.entrySet()) {
            if (kv.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }
}
