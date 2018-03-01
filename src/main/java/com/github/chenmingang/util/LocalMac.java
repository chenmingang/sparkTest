package com.github.chenmingang.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * 物理地址是48位，别和ipv6搞错了
 */
public class LocalMac {

    public static void main(String[] args) throws UnknownHostException, SocketException {

        String localMac = getLocalMac();
        System.out.println(localMac);
    }

    private static String getLocalMac() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("获取本机localhost失败");
        }

        byte[] mac;
        try {
            mac = NetworkInterface.getByInetAddress(localhost).getHardwareAddress();
        } catch (SocketException e) {
            throw new RuntimeException("获取本机mac失败");
        }
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < mac.length; i++) {
            //字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0");
                sb.append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }
}