package com.github.chenmingang.snowflake.paxos;

import com.github.chenmingang.snowflake.net.MessageDecoder;
import com.github.chenmingang.snowflake.net.MessageEncoder;
import com.github.chenmingang.snowflake.net.RequestInfo;
import com.github.chenmingang.util.ConfigUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 批准者
 */
public class Acceptor {
    private String port = ConfigUtil.getProperty("paxos.server.port");
    Meta meta = Meta.INSTANCE;

    private volatile long lastProposerVersion = -1L;


    //test
    public Acceptor(String port) {
        this.port = port;
        start();
    }

    private Acceptor() {
    }

    private void start() {
//        while (true) {
        try {
            Channel channel = bind(Integer.valueOf(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void handle(ChannelHandlerContext ctx, RequestInfo requestInfo) {
        System.out.println(requestInfo);
        RequestInfo result = new RequestInfo();
        result.setType(requestInfo.getType());
        result.setSequence(Long.valueOf(port));
        if (requestInfo.getType() == 1) {
            //prepare
            long version = Long.valueOf(requestInfo.getBody(String.class));
            if (version > lastProposerVersion) {
                lastProposerVersion = Long.valueOf(requestInfo.getBody(String.class));
                result.setBody("true");
            } else {
                result.setBody("false");
            }
            System.out.println("prepare : " + result.getBody(String.class));

        } else if (requestInfo.getType() == 2) {
            //proposer
            String body = requestInfo.getBody(String.class);
            String[] split = body.split("#");
            String versionStr = split[0];
            String client = split[1];
            String workId = split[2];

            if (versionStr.equals(lastProposerVersion + "")) {
                if (meta.exist(Long.valueOf(workId)) && !meta.exist(client, Long.valueOf(workId))) {
                    result.setBody("false");
                } else {
                    result.setBody("true");
                }
            }
            System.out.println("proposer : " + result.getBody(String.class));

        } else if (requestInfo.getType() == 3) {
            //learner
            String body = requestInfo.getBody(String.class);
            String[] split = body.split("#");
            String client = split[0];
            String workId = split[1];
            meta.putMeta(client, Long.valueOf(workId));
            result.setBody("true");
            System.out.println("learner : " + result.getBody(String.class));
        }
        ctx.writeAndFlush(result);
    }

    private Channel bind(int serverPort) throws Exception {
        // 连接处理group
        EventLoopGroup boss = new NioEventLoopGroup();
        // 事件处理group
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 绑定处理group
        bootstrap.group(boss, worker);
        bootstrap.channel(NioServerSocketChannel.class);
        // 保持连接数
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024 * 1024);
        // 有数据立即发送
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        // 保持连接
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 处理新连接
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel sc) throws Exception {
                // 增加任务处理
                ChannelPipeline p = sc.pipeline();
                p.addLast(new MessageDecoder(), new MessageEncoder(), new SimpleChannelInboundHandler<RequestInfo>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestInfo requestInfo) throws Exception {
                        handle(channelHandlerContext, requestInfo);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        RequestInfo result = new RequestInfo();
                        result.setBody("false");
                        ctx.writeAndFlush(result);
                    }
                });
            }
        });

        ChannelFuture f = bootstrap.bind(serverPort).sync();
//        f.channel().closeFuture().sync();
        if (f.isSuccess()) {
            System.out.println("long connection started success on port:" + port);
            return f.channel();
        } else {
            System.out.println("long connection started fail");
        }
        return null;
    }
}
