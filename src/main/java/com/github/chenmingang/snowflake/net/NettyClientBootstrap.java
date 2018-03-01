package com.github.chenmingang.snowflake.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClientBootstrap {
    private int port;
	private String host;
	private SocketChannel socketChannel;

	public static void main(String[] args) throws Exception {
		NettyClientBootstrap bootstrap = new NettyClientBootstrap(9999, "127.0.0.1");
		int i = 1;

		while (i<5) {
			RequestInfo req = new RequestInfo();
			req.setSequence(i);
			SocketChannel socketChannel = bootstrap.getSocketChannel();
			socketChannel.writeAndFlush(req);
			i++;
			Thread.sleep(1000);
		}
//		bootstrap.getSocketChannel().close();

	}

	public NettyClientBootstrap(int port, String host) throws Exception {
		this.host = host;
		this.port = port;
		start();
	}
	private void start() throws Exception {
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.group(eventLoopGroup);
		bootstrap.remoteAddress(this.host, this.port);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), new NettyClientHandler());
			}
		});
		ChannelFuture future = bootstrap.connect(this.host, this.port).sync();
		if (future.isSuccess()) {
			socketChannel = (SocketChannel) future.channel();
			System.out.println("connect server  success|");
		}
	}
	public int getPort() {
		return this.port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
}