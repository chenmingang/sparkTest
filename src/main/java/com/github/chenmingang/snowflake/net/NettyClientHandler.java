package com.github.chenmingang.snowflake.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<RequestInfo> {


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RequestInfo msg) throws Exception {
		System.out.println(msg);
//		ctx.close();
//		ctx.disconnect();
//		ctx.channel().eventLoop().shutdownGracefully();
	}
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelRegistered();
	}
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelInactive();
	}
}