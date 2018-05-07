package com.github.chenmingang.snowflake.paxos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 各机器id的元数据
 */
public class Meta {

//    private Meta() {
//    }

    private String self;

    public static final Meta INSTANCE = new Meta();

    private Map<String, Long> meta = new ConcurrentHashMap<>();

    public synchronized void putMeta(String key, long value) {
//        System.out.println("设置元数据 " + key + "#" + value);
        meta.put(key, value);
//        System.out.println(">>>>");
//        for (Map.Entry<String, Long> kv : meta.entrySet()) {
//            System.out.println("meta: " + kv.getKey() + "#" + kv.getValue());
//        }
//        System.out.println(">>>>");
    }

    public synchronized boolean exist(String key) {
        return meta.containsKey(key);
    }

    public synchronized boolean exist(String key, Long value) {
        return exist(key) && meta.get(key).equals(value);
    }

    public synchronized boolean exist(Long value) {
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

    public void print() {
        for (Map.Entry<String, Long> kv : meta.entrySet()) {
            System.out.println("meta: " + kv.getKey() + "#" + kv.getValue());
        }
    }
}
