package com.github.chenmingang.snowflake.paxos;

import com.github.chenmingang.snowflake.IdGenerator;
import com.github.chenmingang.snowflake.net.MessageDecoder;
import com.github.chenmingang.snowflake.net.MessageEncoder;
import com.github.chenmingang.snowflake.net.RequestInfo;
import com.github.chenmingang.util.ConfigUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 提案者
 */
public class Proposer {

    private String port = ConfigUtil.getProperty("paxos.server.port");
    private String acceptors = ConfigUtil.getProperty("idGenerate.work.hosts");
    private String self = null;

    private static Set<String> ipPorts = new HashSet<>();
    private AtomicInteger maxAcceptorNum = new AtomicInteger(0);
    private AtomicInteger accepNum = new AtomicInteger(0);
    private volatile long prepareVersionId = -1L;

    private Map<String, SocketChannel> aliveSocket = new HashMap<>();

    static {

    }

    public static void main(String[] args) {
        long proposal = 0;
        while (proposal <= 0) {
            try {
                proposal = INSTANCE.proposal();
            } catch (Exception e) {
                System.out.println("proposal失败");
            }
        }
        System.out.println(proposal);
    }

    //test
    public Proposer(String port) {
        this.port = port;
        String[] ipPortArr = acceptors.split(";");
        for (String ipPortStr : ipPortArr) {
            final String[] ipAndPort = ipPortStr.split(":");
            if (isSelf(ipAndPort[0], ipAndPort[1])) {
                self = ipPortStr;
            }
            ipPorts.add(ipPortStr);
        }

        resetNum();
        startAll();
        this.proposal();
    }

    private Proposer() {
        String[] ipPortArr = acceptors.split(";");
        for (String ipPortStr : ipPortArr) {
            final String[] ipAndPort = ipPortStr.split(":");
            if (isSelf(ipAndPort[0], ipAndPort[1])) {
                self = ipPortStr;
            }
            ipPorts.add(ipPortStr);
        }

        resetNum();
        startAll();
    }

    public static Proposer INSTANCE = new Proposer();

    public boolean prepare() {
        long nextId = IdGenerator.currentMillis();
        RequestInfo req = new RequestInfo();
        req.setType(1);
        req.setBody(nextId + "");
        doNet(req);
        if (proposerResult()) {
            prepareVersionId = nextId;
            return true;
        } else {
            throw new RuntimeException("false");
        }
    }

    public long proposal() {
        long workId = 0;

        while (workId <= 0) {
            try {
                resetNum();
                prepare();

                resetNum();

                workId = IdGenerator.genNextWorkId();
                RequestInfo req = new RequestInfo();
                req.setType(2);
                req.setBody(prepareVersionId + "#" + self + "#" + workId);
                doNet(req);

                if (proposerResult()) {
                    resetNum();
                    learn(workId);
                    System.out.println(workId+":true");
                    return workId;
                } else {
                    throw new RuntimeException("false");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(workId+":false");
            }
        }
        return workId;
    }

    private void learn(Long workId) {
        Meta.INSTANCE.putMeta(self, workId);
        RequestInfo req = new RequestInfo();
        req.setType(3);
        req.setBody(self + "#" + workId);
        doNet(req);
    }

    private void resetNum() {
//        maxAcceptorNum = new AtomicInteger(0);
        accepNum = new AtomicInteger(0);
    }

    private void doNet(RequestInfo req) {
        aliveSocket.values().forEach(socketChannel -> {
            try {
                socketChannel.writeAndFlush(req).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }

    private synchronized boolean proposerResult() {
        return accepNum.get() * 2 > maxAcceptorNum.get();
    }

    private void startAll() {
        for (String ipPort : ipPorts) {
            final String[] ipAndPort = ipPort.split(":");
            if (ipPort.equals(self)) {
                continue;
            }
            maxAcceptorNum.incrementAndGet();
            SocketChannel socketChannel = start(ipAndPort[0], Integer.valueOf(ipAndPort[1]), new SimpleChannelInboundHandler<RequestInfo>() {
                @Override
                protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestInfo msg) throws Exception {
                    System.out.println(msg);
                    String body = msg.getBody(String.class);
                    if (body.equals("true")) {
                        accepNum.incrementAndGet();
                    }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//                    ctx.fireExceptionCaught(cause);
                    cause.printStackTrace();
                }
            });
            if (socketChannel == null) {
                continue;
            }
            aliveSocket.put(ipPort, socketChannel);
        }
    }

    private SocketChannel start(String inetHost, int inetPort, SimpleChannelInboundHandler handler) {
        ChannelFuture future = null;
        try {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(inetHost, inetPort);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), handler);
                }
            });

            future = bootstrap.connect(inetHost, inetPort).sync();
            if (future.isSuccess()) {
                return (SocketChannel) future.channel();
            }
        } catch (InterruptedException e) {
            return null;
        } finally {
        }
        return null;
    }

    private boolean isSelf(String ip, String port) {
        try {
            InetAddress ia = InetAddress.getByName(ip.split(":")[0]);
            InetAddress localhost = InetAddress.getLocalHost();
            return ia.toString().split("/")[1].equals(localhost.toString().split("/")[1]) && port.equals(this.port);
        } catch (UnknownHostException e) {
        }
        return false;
    }


}
