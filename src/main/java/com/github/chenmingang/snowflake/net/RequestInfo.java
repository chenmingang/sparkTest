package com.github.chenmingang.snowflake.net;

import com.google.gson.Gson;

public class RequestInfo {
    public static Gson gson = new Gson();

    private long sequence;
    private long type;

    private String body;

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }

    public <T> T getBody(Class<T> c) {
        return gson.fromJson(body, c);
    }

    public void setBody(Object body) {
        this.body = gson.toJson(body);
    }


    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "sequence=" + sequence +
                ", type=" + type +
                ", body='" + body + '\'' +
                '}';
    }
}
