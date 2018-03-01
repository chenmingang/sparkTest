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

    private static String port = ConfigUtil.getProperty("paxos.server.port");
    private static String acceptors = ConfigUtil.getProperty("idGenerate.work.hosts");
    private static Set<String> ipPorts = new HashSet<>();
    private AtomicInteger maxAcceptorNum = new AtomicInteger(0);
    private AtomicInteger accepNum = new AtomicInteger(0);
    private volatile long prepareVersionId = -1L;

    private Map<String, SocketChannel> aliveSocket = new HashMap<>();

    static {
        String[] ipPortArr = acceptors.split(";");
        for (String ipPortStr : ipPortArr) {
            ipPorts.add(ipPortStr);
        }
    }

    public static void main(String[] args) {
        long proposal = INSTANCE.proposal();
        System.out.println(proposal);
    }

    private Proposer() {
        startAll();
    }

    public static Proposer INSTANCE = new Proposer();

    public boolean prepare() {
        long nextId = IdGenerator.INSTANCE.nextId();
        RequestInfo req = new RequestInfo();
        req.setType(1);
        req.setBody("" + nextId);
        doNet(req);
        if (proposerResult()) {
            resetNum();
            prepareVersionId = nextId;
            return true;
        } else {
            return prepare();
        }
    }

    public long proposal() {
        prepare();

        final long candidateId = IdGenerator.genNextWorkId();
        RequestInfo req = new RequestInfo();
        req.setType(2);
        req.setBody(prepareVersionId + ":" + candidateId);
        doNet(req);

        if (proposerResult()) {
            resetNum();
            return candidateId;
        } else {
            return proposal();
        }
    }

    private void resetNum() {
        maxAcceptorNum = new AtomicInteger(0);
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

    private boolean proposerResult() {
        return accepNum.get() * 2 >= maxAcceptorNum.get();
    }

    private void startAll() {
        for (String ipPort : ipPorts) {
            final String[] ipAndPort = ipPort.split(":");
            if (isSelf(ipAndPort[0], ipAndPort[1])) {
                continue;
            }
            maxAcceptorNum.incrementAndGet();
            SocketChannel socketChannel = start(ipAndPort[0], Integer.valueOf(ipAndPort[1]), new SimpleChannelInboundHandler<RequestInfo>() {
                @Override
                protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestInfo msg) throws Exception {
//                    System.out.println(msg);
                    String body = msg.getBody(String.class);
                    if (body.equals("true")) {
                        accepNum.incrementAndGet();
                    }
                }
            });
            if (socketChannel == null) {
                continue;
            }
            aliveSocket.put(ipPort, socketChannel);
        }
    }

    private SocketChannel start(String inetHost, int inetPort, SimpleChannelInboundHandler handler) {
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
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(inetHost, inetPort).sync();
        } catch (InterruptedException e) {
            return null;
        } finally {
        }
        if (future.isSuccess()) {
            return (SocketChannel) future.channel();
        }
        return null;
    }

    private boolean isSelf(String ip, String port) {
        try {
            InetAddress ia = InetAddress.getByName(ip.split(":")[0]);
            InetAddress localhost = InetAddress.getLocalHost();
            return ia.toString().split("/")[1].equals(localhost.toString().split("/")[1]) && port.equals(Proposer.port);
        } catch (UnknownHostException e) {
        }
        return false;
    }


}
