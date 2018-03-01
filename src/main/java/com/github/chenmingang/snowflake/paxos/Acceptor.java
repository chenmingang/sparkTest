package com.github.chenmingang.snowflake.paxos;

import com.github.chenmingang.snowflake.IdGenerator;
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
    private static String port = ConfigUtil.getProperty("paxos.server.port");

    private volatile long lastProposerVersion = -1L;

    public static void main(String[] args) {
        Acceptor.INSTANCE.start();
    }

    private RequestInfo handelResult = null;

    private Acceptor() {
    }

    public static Acceptor INSTANCE = new Acceptor();

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
        result.setType(1);
        if (requestInfo.getType() == 1) {
            if (requestInfo.getType() > lastProposerVersion) {
                lastProposerVersion = Long.valueOf(requestInfo.getBody(String.class));
                result.setBody("true");
            } else {
                result.setBody("false");
            }
        } else if (requestInfo.getType() == 2) {
            String body = requestInfo.getBody(String.class);
            String[] split = body.split(":");
            if (split[0].equals(lastProposerVersion + "") &&
                    IdGenerator.INSTANCE.getWorkerId() != Long.valueOf(split[1])) {
                result.setBody("true");
            } else {
                result.setBody("false");
            }
        }
        ctx.write(result);
        ctx.flush();
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
//        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
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

                });
            }
        });

        ChannelFuture f = bootstrap.bind(serverPort).sync();
//        f.channel().closeFuture().sync();
        if (f.isSuccess()) {
            System.out.println("long connection started success");
            return f.channel();
        } else {
            System.out.println("long connection started fail");
        }
        return null;
    }
}
