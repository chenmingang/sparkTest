package com.github.chenmingang.snowflake;

import com.github.chenmingang.util.ConfigUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WorkIdServer {

    private static String hosts = ConfigUtil.getProperty("idGenerate.work.hosts");


    static {

    }

    public static void main(String[] args){
        new WorkIdServer().start();
    }

    public void start() {
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {

        }
    }
}
